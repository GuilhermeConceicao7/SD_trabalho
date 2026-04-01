package sd2526.trab.server.java;

import java.net.InetAddress;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.hsqldb.persist.Log;

import sd2526.trab.api.User;

import sd2526.trab.api.Message;
import sd2526.trab.api.ServiceInfo;
import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Result.ErrorCode;
import sd2526.trab.api.java.Users;
import sd2526.trab.client.java.MessagesClientFactory;
import sd2526.trab.client.java.UsersClientFactory;

import sd2526.trab.server.persistence.Hibernate;

public class JavaMessages implements Messages {

  private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

  // Guarda todas as mensagens: msgId -> Message
  private final Map<String, Message> allMessages = new ConcurrentHashMap<>();

  // Guarda as inboxes: userName -> Set de msgIds
  private final Map<String, Set<String>> inboxes = new ConcurrentHashMap<>();

  private Hibernate hibernate;

  public JavaMessages() {
    hibernate = Hibernate.getInstance();
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

    for (String destination : msg.getDestination()) {
      String destDomain = extractDomain(destination);
      String destUser = extractUser(destination);

      if (destDomain.equals(getLocalDomain())) {
        try {

          Users usersClient = UsersClientFactory.get(getLocalDomain());
          Result<User> destCheck = usersClient.authenticateUser(destUser, "___invalid___");

          if (!destCheck.isOK() && destCheck.error() == ErrorCode.NOT_FOUND) {
            // user não existe — notifica sender
            String errorMid = msgId + "." + destination;
            String errorSubject = "FAILED TO SEND " + msgId + " TO " + destination + ": UNKNOWN USER";
            Message errorMsg = new Message(errorMid, msg.getSender(),
                user.getName() + "@" + getLocalDomain(), errorSubject, msg.getContents());
            errorMsg.setCreationTime(msg.getCreationTime());
            allMessages.put(errorMid, errorMsg);
            inboxes.computeIfAbsent(user.getName(), k -> ConcurrentHashMap.newKeySet()).add(errorMid);
          } else {
            // FORBIDDEN = user existe mas pwd errada → entrega normal
            storeMessageForUser(destUser, msgId);
          }
        } catch (Exception e) {
          Log.warning("Excepção ao verificar destinatário " + destUser + ": " + e.getMessage());
          // fallback: entrega mesmo assim
          storeMessageForUser(destUser, msgId);
        }
      } else {
        // entrega remota ASSÍNCRONA
        final String finalMsgId = msgId;
        final Message finalMsg = msg;
        final String finalDestDomain = destDomain;
        new Thread(() -> {
          try {
            Messages remoteClient = MessagesClientFactory.get(finalDestDomain);
            Result<Void> result = remoteClient.forwardMessage(finalMsgId, finalMsg);
            if (!result.isOK()) {
              Log.warning("Falha ao reencaminhar para " + finalDestDomain + ": " + result.error());
            }
          } catch (Exception e) {
            Log.warning("Erro ao contactar domínio remoto " + finalDestDomain + ": " + e.getMessage());
          }
        }).start();
      }
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

    String senderEmail = extractEmailFromSender(msg.getSender());
    String senderName = extractUser(senderEmail);
    if (!senderName.equals(name)) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    // remove de todas as inboxes
    allMessages.remove(mid);
    hibernate.delete(msg);
    inboxes.values().forEach(inbox -> inbox.remove(mid));

    return Result.ok();
  }

  @Override
  public Result<Void> forwardMessage(String mid, Message msg) {
    // evita duplicados
    if (allMessages.containsKey(mid)) {
      return Result.ok();
    }

    msg.setId(mid);
    allMessages.put(mid, msg);
    hibernate.persist(msg);

    // entrega local se for para o mesmo domínio
    for (String destination : msg.getDestination()) {
      String destUser = extractUser(destination);
      String destDomain = extractDomain(destination);

      if (destDomain.equals(getLocalDomain())) {
        // verifica se o destinatário existe no domínio remoto
        Users usersClient = UsersClientFactory.get(getLocalDomain());
        Result<User> destCheck = usersClient.getUser(destUser, "___invalid___");
        if (destCheck.error() != ErrorCode.NOT_FOUND) {
          storeMessageForUser(destUser, mid);
        }
        // se não existe, ignora (o sender já foi notificado pelo seu servidor)
      }
    }

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

  /** Persiste a mensagem no inbox do usuário local */
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

}
