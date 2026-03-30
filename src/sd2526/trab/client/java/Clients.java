package sd2526.trab.client.java;

import java.net.URI;

import sd2526.trab.api.java.Users;
import sd2526.trab.client.grpc.GrpcUsersClient;
import sd2526.trab.client.rest.RestUsersClient;

public class Clients {

	public static final ClientFactory<Users> UsersClient = new GenericClientFactory<Users>(Users.SERVICE_NAME,
			RestUsersClient::new, GrpcUsersClient::new);

	public static final ClientFactory<Users> UsersClientToo = new AbstractClientFactory<Users>(Users.SERVICE_NAME) {

		Users createRestClient(URI serverURI) {
			return new RestUsersClient(serverURI);
		}

		Users createGrpcClient(URI serverURI) {
			return new GrpcUsersClient(serverURI);
		}
	};
}
