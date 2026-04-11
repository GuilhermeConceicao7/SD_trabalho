package sd2526.trab.server.java;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import sd2526.trab.api.User;

import sd2526.trab.api.Message;

import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Result.ErrorCode;
import sd2526.trab.api.java.Users;
import sd2526.trab.client.java.MessagesClientFactory;
import sd2526.trab.client.java.UsersClientFactory;

import sd2526.trab.server.persistence.Hibernate;

public class JavaMessages implements Messages {

  private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

  private static final long DELETE_WINDOW_MS = 30_000;
  private static final long TIMEOUT_MS = 55_000L;

  private final String localDomain;

  private final Map<String, BlockingQueue<Runnable[]>> domainQueues = new ConcurrentHashMap<>();

  // Guarda todas as mensagens: msgId -> Message
  private final Map<String, Message> allMessages = new ConcurrentHashMap<>();

  // Guarda as inboxes: userName -> Set de msgIds
  private final Map<String, Set<String>> inboxes = new ConcurrentHashMap<>();

  private Hibernate hibernate;

  public JavaMessages(String domain) {
    this.localDomain = domain;
    hibernate = Hibernate.getInstance();

    try {
      List<Message> persisted = hibernate.jpql("FROM Message", Message.class);
      for (Message m : persisted) {
        allMessages.put(m.getId(), m);
      }
    } catch (Exception e) {
      Log.warning("Erro ao recarregar mensagens: " + e.getMessage());
    }
  }

  @Override
public Result<String> postMessage(String pwd, Message msg) {

  if (pwd == null || msg == null || msg.getContents() == null || msg.getCreationTime() == 0
      || msg.getDestination() == null
      || msg.getSender() == null || msg.getSubject() == null) {
    return Result.error(ErrorCode.BAD_REQUEST);
  }

  String senderName = extractUser(msg.getSender());
  String senderDomain = extractDomain(msg.getSender());

  Users userClient = UsersClientFactory.get(senderDomain);
  Result<User> auth = userClient.getUser(senderName, pwd);

  if (!auth.isOK()) {
    return Result.error(ErrorCode.FORBIDDEN);
  }

  User user = auth.value();

  String msgId = (msg.getId() != null && !msg.getId().isBlank())
      ? msg.getId()
      : UUID.randomUUID().toString();

  msg.setId(msgId);
  msg.setSender(user.getDisplayName() + " <" + user.getName() + "@" + user.getDomain() + ">");

  if (allMessages.containsKey(msgId)) {
    return Result.ok(msgId);
  }

  allMessages.put(msgId, msg);
  hibernate.persist(msg);

  Map<String, List<String>> remoteByDomain = new HashMap<>();

  // Destinos locais
  for (String destination : msg.getDestination()) {
    String destDomain = extractDomain(destination);
    String destUser = extractUser(destination);

    if (destDomain.equals(this.localDomain)) {
    try {
        Users usersClient = UsersClientFactory.get(getLocalDomain());
        Result<User> destCheck = usersClient.authenticateUser(destUser, "");
        

        if (destCheck.error() == ErrorCode.NOT_FOUND) {
          String errorMid = msgId + "." + destination;
          String errorSubject = "FAILED TO SEND " + msgId + " TO " + destination + ": UNKNOWN USER";
          Message errorMsg = new Message(errorMid, msg.getSender(),
              user.getName() + "@" + this.localDomain, errorSubject, msg.getContents());
          errorMsg.setCreationTime(msg.getCreationTime());
          allMessages.put(errorMid, errorMsg);
          try { hibernate.persist(errorMsg); } catch (Exception ex) {}
          storeMessageForUser(user.getName(), errorMid);
        } else {
          storeMessageForUser(destUser, msgId);
        }
      } catch (Exception e) {
        Log.warning("Excepção ao verificar destinatário " + destUser + ": " + e.getMessage());
        storeMessageForUser(destUser, msgId);
      }
    } else {
      remoteByDomain.computeIfAbsent(destDomain, k -> new java.util.ArrayList<>()).add(destination);
    }
  }

  // Destinos remotos
  for (Map.Entry<String, List<String>> entry : remoteByDomain.entrySet()) {
    String destDomain = entry.getKey();
    List<String> domainDests = entry.getValue();

    String senderNameLocal = user.getName();
    Message finalMsg = msg;
    String finalMsgId = msgId;

    // Callback de timeout: envia TIMEOUT ao remetente 
    Runnable onTimeout = () -> {
      for (String dest : domainDests) {
        String errorMid = finalMsgId + "." + dest;
        String errorSubject = "FAILED TO SEND " + finalMsgId + " TO " + dest + ": TIMEOUT";
        Message errorMsg = new Message(
            errorMid, finalMsg.getSender(),
            senderNameLocal + "@" + localDomain,
            errorSubject, finalMsg.getContents()
        );
        errorMsg.setCreationTime(System.currentTimeMillis());
        allMessages.put(errorMid, errorMsg);
        try { hibernate.persist(errorMsg); } catch (Exception ex) {}
        storeMessageForUser(senderNameLocal, errorMid);
      }
    };

    // Task principal: verifica users remotos e faz forward 
    dispatchToDomain(destDomain, () -> {
      try {
        Users remoteUsers = UsersClientFactory.get(destDomain);
        boolean anyValid = false;

        for (String dest : domainDests) {
          String destUser = extractUser(dest);
          Result<User> destCheck = remoteUsers.authenticateUser(destUser, "");

          if (destCheck.error() == ErrorCode.NOT_FOUND) {
            String errorMid = finalMsgId + "." + dest;
            String errorSubject = "FAILED TO SEND " + finalMsgId + " TO " + dest + ": UNKNOWN USER";
            Message errorMsg = new Message(
                errorMid, finalMsg.getSender(),
                senderNameLocal + "@" + localDomain,
                errorSubject, finalMsg.getContents()
            );
            errorMsg.setCreationTime(finalMsg.getCreationTime());
            allMessages.put(errorMid, errorMsg);
            try { hibernate.persist(errorMsg); } catch (Exception ex) {}
            storeMessageForUser(senderNameLocal, errorMid);
          } else {
            anyValid = true;
          }
        }

        if (anyValid) {
          Messages remoteClient = MessagesClientFactory.get(destDomain);
          Result<Void> result = remoteClient.forwardMessage(finalMsgId, finalMsg);
          if (!result.isOK()) {
            throw new RuntimeException("Falha ao reencaminhar: " + result.error());
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }, onTimeout);
  }

  return Result.ok(msg.getId());
}

  @Override
  public Result<Message> getInboxMessage(String name, String mid, String pwd) {
    // valida utilizador
    Users userClient = UsersClientFactory.get(getLocalDomain());
    Result<User> auth = userClient.getUser(name, pwd);
    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    // verifica se o mid está na inbox do user
    Set<String> inbox = inboxes.get(name);
    if (inbox == null || !inbox.contains(mid)) {
      return Result.error(ErrorCode.NOT_FOUND);
    }

    // busca a mensagem
    Message msg = allMessages.get(mid);
    if (msg == null) {
      return Result.error(ErrorCode.NOT_FOUND);
    }

    return Result.ok(msg);
  }

  @Override
  public Result<List<String>> getAllInboxMessages(String name, String pwd) {
    Users userClient = UsersClientFactory.get(getLocalDomain());
    Result<User> auth = userClient.getUser(name, pwd);
    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    Set<String> inbox = inboxes.getOrDefault(name, ConcurrentHashMap.newKeySet());
    return Result.ok(new java.util.ArrayList<>(inbox));
  }

  @Override
  public Result<List<String>> searchInbox(String name, String query, String pwd) {
    Users userClient = UsersClientFactory.get(getLocalDomain());
    Result<User> auth = userClient.getUser(name, pwd);
    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    Set<String> inbox = inboxes.getOrDefault(name, ConcurrentHashMap.newKeySet());

    String queryLower = query.toLowerCase();

    List<String> filtered = inbox.stream()
        .filter(mid -> {
          Message msg = allMessages.get(mid);
          if (msg == null)
            return mid.toLowerCase().contains(queryLower);
          return msg != null && (msg.getSubject().toLowerCase().contains(queryLower) ||
              msg.getContents().toLowerCase().contains(queryLower) ||
              msg.getSender().contains(queryLower));
        })
        .collect(java.util.stream.Collectors.toList());

    return Result.ok(filtered);
  }

  @Override
  public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
    Users userClient = UsersClientFactory.get(getLocalDomain());
    Result<User> auth = userClient.getUser(name, pwd);
    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    Set<String> inbox = inboxes.get(name);
    if (inbox == null || !inbox.contains(mid)) {
      return Result.error(ErrorCode.NOT_FOUND);
    }

    inbox.remove(mid);
    return Result.ok();
  }

  @Override
  public Result<Void> deleteMessage(String name, String mid, String pwd) {
    // autentica o remetente
    Users userClient = UsersClientFactory.get(getLocalDomain());
    Result<User> auth = userClient.getUser(name, pwd);
    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    Message msg = allMessages.get(mid);
    if (msg == null) {
      return Result.error(ErrorCode.NOT_FOUND);
    }

    if (System.currentTimeMillis() - msg.getCreationTime() > DELETE_WINDOW_MS) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    String senderEmail = extractEmailFromSender(msg.getSender());
    String senderName = extractUser(senderEmail);
    if (!senderName.equals(name)) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    for (String destination : msg.getDestination()) {
      String destDomain = extractDomain(destination);

      if (!destDomain.equals(getLocalDomain())) {
        dispatchToDomain(destDomain, () -> {
          try {
            Messages remoteClient = MessagesClientFactory.get(destDomain);
            Result<Void> r = remoteClient.deleteMessageInternal(mid);
            if (!r.isOK()) {
              throw new RuntimeException("deleteInternal failed: " + r.error());
            }
          } catch (RuntimeException e) {
            throw e;
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }, null);
      }
    }
    allMessages.remove(mid);
    hibernate.delete(msg);
    inboxes.values().forEach(inbox -> inbox.remove(mid));

    return Result.ok();
  }

  @Override
  public Result<Void> forwardMessage(String mid, Message msg) {
    msg.setId(mid);

    if (allMessages.putIfAbsent(mid, msg) != null) {
      return Result.ok();
    }

    msg.setId(mid);
    allMessages.put(mid, msg);
    try {
      hibernate.persist(msg);
    } catch (Exception e) {
      Log.warning("forwardMessage: persist failed for " + mid + ": " + e.getMessage());

    }

    // entrega local se for para o mesmo domínio
    for (String destination : msg.getDestination()) {
      String destUser = extractUser(destination);
      String destDomain = extractDomain(destination);

      if (destDomain.equals(getLocalDomain())) {
        storeMessageForUser(destUser, mid);
      }
    }

    return Result.ok();
  }

  @Override
  public Result<Void> deleteMessageInternal(String mid) {

    Message msg = allMessages.get(mid);
    if (msg == null)
      return Result.ok();

    allMessages.remove(mid);
    hibernate.delete(msg);
    inboxes.values().forEach(inbox -> inbox.remove(mid));

    return Result.ok();
  }

  private String extractDomain(String user) {
    if (user.contains("@"))
      return user.split("@")[1];
    return getLocalDomain();
  }

  private String getLocalDomain() {
    try {
      String hostname = InetAddress.getLocalHost().getHostName(); // "messages0.ourorg"
      String[] parts = hostname.split("\\.");
      return parts[parts.length - 1]; // "ourorg"
    } catch (Exception e) {
      return "local";
    }
  }

  private String extractUser(String user) {
    if (user.contains("@"))
      return user.split("@")[0];
    return user;
  }

  // Persiste a mensagem no inbox do usuário local
  private void storeMessageForUser(String username, String msgId) {
    inboxes.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet())
        .add(msgId);
  }

  private String extractEmailFromSender(String sender) {
    if (sender.contains("<") && sender.contains(">")) {
      return sender.substring(sender.indexOf('<') + 1, sender.indexOf('>'));
    }
    return sender;
  }

  
  private void dispatchToDomain(String destDomain, Runnable task, Runnable onTimeout) {
  domainQueues.computeIfAbsent(destDomain, d -> {
    BlockingQueue<Runnable[]> q = new LinkedBlockingQueue<>();
    Thread worker = new Thread(() -> {
      while (true) {
        try {
          Runnable[] pair = q.take();
          Runnable r = pair[0];
          Runnable timeoutCb = pair[1];
          final long startTime = System.currentTimeMillis();
          boolean succeeded = false;

          while (!succeeded) {
            long elapsed = System.currentTimeMillis() - startTime;
            if (elapsed >= TIMEOUT_MS) {
              if (timeoutCb != null) {
                try { timeoutCb.run(); } catch (Exception ex) {}
              }
              break;
            }
            try {
              r.run();
              succeeded = true;
            } catch (Exception e) {
              elapsed = System.currentTimeMillis() - startTime;
              if (elapsed >= TIMEOUT_MS) {
                if (timeoutCb != null) {
                  try { timeoutCb.run(); } catch (Exception ex) {}
                }
                break;
              }
              try { Thread.sleep(1000); } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
              }
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break;
        }
      }
    });
    worker.setDaemon(true);
    worker.start();
    return q;
  }).add(new Runnable[]{task, onTimeout});
}

}