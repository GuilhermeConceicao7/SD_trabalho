package sd2526.trab.server.rest;

import java.util.List;

import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.client.java.MessagesClientFactory;

@Singleton
public class RestGatewayMessagesResource implements RestMessages {

  private String getDomain(String userId) {
    if (userId != null && userId.contains("@"))
      return userId.split("@")[1];
    return "ourorg";
  }

  @Override
  public String postMessage(String pwd, Message msg) {
    String domain = getDomain(msg.getSender());
    Result<String> result = MessagesClientFactory.get(domain).postMessage(pwd, msg);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
    return result.value();
  }

  @Override
  public Message getMessage(String name, String mid, String pwd) {
    String domain = getDomain(name);
    Result<Message> result = MessagesClientFactory.get(domain).getInboxMessage(name, mid, pwd);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
    return result.value();
  }

  @Override
  public List<String> getMessages(String name, String pwd, String query) {
    String domain = getDomain(name);
    Result<List<String>> result;
    if (query == null || query.isEmpty())
      result = MessagesClientFactory.get(domain).getAllInboxMessages(name, pwd);
    else
      result = MessagesClientFactory.get(domain).searchInbox(name, pwd, query);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
    return result.value();
  }

  @Override
  public void removeFromUserInbox(String name, String mid, String pwd) {
    String domain = getDomain(name);
    Result<Void> result = MessagesClientFactory.get(domain).removeInboxMessage(name, mid, pwd);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
  }

  @Override
  public void deleteMessage(String name, String mid, String pwd) {
    String domain = getDomain(name);
    Result<Void> result = MessagesClientFactory.get(domain).deleteMessage(name, mid, pwd);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
  }

  @Override
  public void forwardMessage(String mid, Message msg) {
    // forward vai para o domínio do destinatário
    String domain = getDomain(msg.getSender());
    Result<Void> result = MessagesClientFactory.get(domain).forwardMessage(mid, msg);
    if (!result.isOK())
      throw new WebApplicationException(result.error().ordinal());
  }
}