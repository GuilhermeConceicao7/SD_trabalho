package sd2526.trab.server.rest;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

public class RestUsersServer extends AbstractRestServer {

  private static Logger Log = Logger.getLogger(RestUsersServer.class.getName());

  public static final int PORT = 8081;
  public static final String SERVICE = "Users";

  RestUsersServer() throws UnknownHostException {
    super(Log, SERVICE, PORT);
  }

  @Override
  void registerResources(ResourceConfig config) {
    config.register(RestUsersResource.class);
  }

  public static void main(String[] args) throws Exception {
    new RestUsersServer().start();
  }

}
