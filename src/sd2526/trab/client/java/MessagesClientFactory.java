package sd2526.trab.client.java;

import java.net.URI;

import sd2526.trab.api.Message;

import sd2526.trab.api.java.Messages;
import sd2526.trab.api.java.Result;

import sd2526.trab.client.grpc.GrpcMessagesClient;
import sd2526.trab.client.rest.RestMessagesClient;
import sd2526.trab.server.discovery.Discovery;

public class MessagesClientFactory {

  private static final String REST = "/rest";
  private static final String GRPC = "/grpc";
  private static final Object DOMAIN_DELIMITER = "@";

  static public Messages get(String domain) {
    var sn = "%s%s%s".formatted(Messages.SERVICE_NAME, DOMAIN_DELIMITER, domain);
    return newClient(Discovery.knownUrisOf(sn, 1)[0]);
  }

  static private Messages newClient(URI serverURI) {
    var path = serverURI.getPath();
    if (path.endsWith(REST))
      return new RestMessagesClient(serverURI);
    if (path.endsWith(GRPC))
      return (Messages) new GrpcMessagesClient(serverURI);
    throw new RuntimeException("Unknown service type..." + serverURI);
  }

  public static void main(String[] args) {

    Messages message = MessagesClientFactory.get("some_domain");

    Result<Message> u = message.getInboxMessage("name", "mid", "pwd");

    if (u.isOK())
      System.out.println("Got message: " + u.value());
    else
      System.out.println("Got error:" + u.error());
  }

}
