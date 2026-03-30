package sd2526.trab.server.rest;

import java.net.UnknownHostException;

import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

public class RestMessagesServer extends AbstractRestServer {

  private static Logger Log = Logger.getLogger(RestMessagesServer.class.getName());

  public static final int PORT = 8082;
  public static final String SERVICE = "Messages";

  RestMessagesServer() throws UnknownHostException {
    super(Log, SERVICE, PORT);
  }

  @Override
  void registerResources(ResourceConfig config) {
    config.register(RestMessagesResource.class);
  }

  public static void main(String[] args) throws Exception {
    new RestMessagesServer().start();
  }
}