package sd2526.trab.server.java;

import java.net.InetAddress;
import java.util.List;

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

  private Hibernate hibernate;

  public JavaMessages() {
    hibernate = Hibernate.getInstance();
  }

  @Override
  public Result<String> postMessage(String pwd, Message msg) {

    if (msg.getContents() == null || msg.getCreationTime() == 0 || msg.getDestination() == null
        || msg.getSender() == null || msg.getSubject() == null) {
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    String senderDomain = extractDomain(msg.getSender());
    Users userClient = UsersClientFactory.get(senderDomain);
    Result<User> auth = userClient.authenticateUser(msg.getSender(), pwd);

    if (!auth.isOK()) {
      return Result.error(ErrorCode.FORBIDDEN);
    }

    User user = auth.value();
    msg.setId(msg.getSender() + "-" + System.currentTimeMillis());
    msg.setSender(user.getName() + "@" + user.getDomain());

    hibernate.persist(msg);

    for (String destination : msg.getDestination()) {
      new Thread(() -> {
        try {
          String destDomain = extractDomain(destination);
          var destMessagesClient = MessagesClientFactory.get(destDomain);

          if (destDomain.equals(getLocalDomain())) {
            storeMessageForUser(destination, msg);
          } else {

            destMessagesClient.postMessage(pwd, msg);
          }
        } catch (Exception e) {
          Log.warning("Falha ao propagar mensagem para " + destination + ": " + e.getMessage());
        }
      }, "MsgThread-" + destination).start();
    }

    return Result.ok(msg.getId());

  }

  @Override
  public Result<Message> getInboxMessage(String name, String mid, String pwd) {
    return Result.error(ErrorCode.NOT_IMPLEMENTED);
  }

  @Override
  public Result<List<String>> getAllInboxMessages(String name, String pwd) {
    return Result.error(ErrorCode.NOT_IMPLEMENTED);
  }

  @Override
  public Result<List<String>> searchInbox(String name, String query, String pwd) {
    return Result.error(ErrorCode.NOT_IMPLEMENTED);
  }

  @Override
  public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
    return Result.error(ErrorCode.NOT_IMPLEMENTED);
  }

  @Override
  public Result<Void> deleteMessage(String name, String mid, String pwd) {
    return Result.error(ErrorCode.NOT_IMPLEMENTED);
  }

  private String extractDomain(String user) {
    if (user.contains("@")) {
      return user.split("@")[1];
    } else {
      return user;
    }
  }

  private String getLocalDomain() {
    try {
      return InetAddress.getLocalHost().getHostName().split("\\.")[0];
    } catch (Exception e) {
      return "local"; // fallback
    }
  }

  /** Persiste a mensagem no inbox do usuário local */
  private void storeMessageForUser(String username, Message msg) {
    // aqui assumimos que o Hibernate persiste a mensagem
    // no banco com uma relação "username -> Message"
    hibernate.persist(msg);
    // se quiseres, podes criar uma tabela Inbox com username + mensagem
  }

}
