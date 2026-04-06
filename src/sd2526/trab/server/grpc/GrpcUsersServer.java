package sd2526.trab.server.grpc;

import java.net.InetAddress;
import java.util.logging.Logger;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.ServerCredentials;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.discovery.Discovery;

public class GrpcUsersServer {
    public static final int PORT = 8090;

    private static final String GRPC_CTX = "/grpc";
    private static final String SERVER_BASE_URI = "grpc://%s:%s%s";

    private static Logger Log = Logger.getLogger(GrpcUsersServer.class.getName());

    public static void main(String[] args) throws Exception {

        String domain = InetAddress.getLocalHost().getHostName().split("\\.")[1];
        String serverURI = String.format(SERVER_BASE_URI, InetAddress.getLocalHost().getHostAddress(), PORT, GRPC_CTX);

        GrpcUsersController stub = new GrpcUsersController(domain);
        ServerCredentials cred = InsecureServerCredentials.create();
        Server server = Grpc.newServerBuilderForPort(PORT, cred).addService(stub).build();

        server.start();

        Discovery discovery = new Discovery(Users.SERVICE_NAME, domain, serverURI);
        discovery.start();

        Log.info(String.format("Users gRPC Server ready @ %s\n", serverURI));
        server.awaitTermination();
    }
}
