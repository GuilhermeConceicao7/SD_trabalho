package sd2526.trab.client.rest;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;

import sd2526.trab.api.rest.RestMessages;
import sd2526.trab.api.rest.RestUsers;
import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.client.Entity;
import sd2526.trab.api.Message;

public class RestMessagesClient extends RestClient implements Messages {

  private static Logger Log = Logger.getLogger(RestMessagesClient.class.getName());

  public RestMessagesClient(URI serverURI) {

    super(serverURI, Log);

    target = client.target(serverURI).path(RestMessages.PATH);
  }

  public Result<String> postMessage(String pwd, Message msg) {
    return super.reTry(() -> doPostMessage(pwd, msg));
  }

  public Result<Message> getInboxMessage(String name, String mid, String pwd) {
    return super.reTry(() -> doGetInboxMessage(name, mid, pwd));
  }

  public Result<List<String>> getAllInboxMessages(String name, String pwd) {
    return super.reTry(() -> doGetAllInboxMessages(name, pwd));
  }

  public Result<Void> removeInboxMessage(String name, String mid, String pwd) {
    return super.reTry(() -> doRemoveInboxMessage(name, mid, pwd));
  }

  public Result<Void> deleteMessage(String name, String mid, String pwd) {
    return super.reTry(() -> doDeleteMessage(name, mid, pwd));
  }

  public Result<List<String>> searchInbox(String name, String pwd, String query) {
    return super.reTry(() -> doSearchInbox(name, pwd, query));
  }

  public Result<Void> forwardMessage(String mid, Message msg) {
    return super.reTry(() -> doForwardMessage(mid, msg));
  }

  public Result<Void> deleteMessageInternal(String mid) {
    return super.reTry(() -> doDeleteMessageInternal(mid));

  }

  public Result<String> doPostMessage(String pwd, Message msg) {
    Response r = target.queryParam(RestUsers.PWD, pwd).request()
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

    return super.processResponse(r, String.class);
  }

  public Result<Message> doGetInboxMessage(String name, String mid, String pwd) {
    Response r = target.path(RestMessages.MBOX).path(name).path(mid)
        .queryParam(RestMessages.PWD, pwd).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    return super.processResponse(r, Message.class);
  }

  public Result<List<String>> doGetAllInboxMessages(String name, String pwd) {
    Response r = target.path(RestMessages.MBOX).path(name)
        .queryParam(RestMessages.PWD, pwd).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    return super.processResponse(r, new GenericType<List<String>>() {
    });
  }

  public Result<Void> doRemoveInboxMessage(String name, String mid, String pwd) {
    Response r = target.path(RestMessages.MBOX).path(name).path(mid)
        .queryParam(RestMessages.PWD, pwd).request()
        .accept(MediaType.APPLICATION_JSON)
        .delete();

    return super.processResponse(r, Void.class);
  }

  public Result<Void> doDeleteMessage(String name, String mid, String pwd) {
    Response r = target.path(name).path(mid)
        .queryParam(RestUsers.PWD, pwd).request()
        .accept(MediaType.APPLICATION_JSON)
        .delete();

    return super.processResponse(r, Void.class);
  }

  public Result<List<String>> doSearchInbox(String name, String pwd, String query) {
    Response r = target.path(RestMessages.MBOX).path(name)
        .queryParam(RestMessages.PWD, pwd)
        .queryParam(RestMessages.QUERY, query).request()
        .accept(MediaType.APPLICATION_JSON)
        .get();

    return super.processResponse(r, new GenericType<List<String>>() {
    });
  }

  public Result<Void> doForwardMessage(String mid, Message msg) {

    Response r = target
        .path(RestMessages.FORWARD)
        .path(mid)
        .request()
        .accept(MediaType.APPLICATION_JSON)
        .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

    return super.processResponse(r, Void.class);
  }

  public Result<Void> doDeleteMessageInternal(String mid) {

    Response r = target.path(RestMessages.INTERNAL).path(mid)
        .request()
        .delete();

    return super.processResponse(r, Void.class);

  }

}
