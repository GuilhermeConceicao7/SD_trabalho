package sd2526.trab.server;

import java.util.logging.Logger;

public class GatewayServer {

  private static Logger Log = Logger.getLogger(GatewayServer.class.getName());

  static {
    System.setProperty("java.net.preferIPv4Stack", "true");
    System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
  }

  public static final int PORT = 8082;
  public static final String SERVICE = "GatewayService";

}