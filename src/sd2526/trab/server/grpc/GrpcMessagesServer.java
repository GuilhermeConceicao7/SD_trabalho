package sd2526.trab.server.grpc;

import java.net.InetAddress;
import java.util.logging.Logger;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import sd2526.trab.server.discovery.Discovery;

public class GrpcMessagesServer {

    private static Logger Log = Logger.getLogger(GrpcMessagesServer.class.getName());

    public static final int PORT = 8092;
    public static final String SERVICE = "Messages";

    static {
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.logging.SimpleFormatter.format", "%4$s: %5$s\n");
    }

    public static void main(String[] args) throws Exception {

        String hostname = InetAddress.getLocalHost().getHostName();
        String domain = hostname.substring(hostname.indexOf('.') + 1);
        String serverURI = "grpc://" + hostname + ":" + PORT + "/grpc";

        Server server = ServerBuilder.forPort(PORT)
                .addService(new GrpcMessagesResource())
                .build()
                .start();

        Log.info(String.format("gRPC %s Server ready @ %s\n", SERVICE, serverURI));

        Discovery discovery = new Discovery(SERVICE, domain, serverURI);
        discovery.start();

        server.awaitTermination();
    }
}