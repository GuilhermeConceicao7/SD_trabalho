package sd2526.trab.server.rest;

import java.net.UnknownHostException;
import java.util.logging.Logger;

import org.glassfish.jersey.server.ResourceConfig;

public class GatewayServer extends AbstractRestServer {

  private static Logger Log = Logger.getLogger(GatewayServer.class.getName());

  public static final int PORT = 8080;
  public static final String SERVICE = "Gateway";

  GatewayServer() throws UnknownHostException {
    super(Log, SERVICE, PORT);
  }

  @Override
  void registerResources(ResourceConfig config) {
    config.register(RestGatewayUsersResource.class);
    config.register(RestGatewayMessagesResource.class);
  }

  public static void main(String[] args) throws Exception {
    new GatewayServer().start();
  }
}