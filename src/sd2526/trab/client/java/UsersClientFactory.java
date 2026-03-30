package sd2526.trab.client.java;

import java.net.URI;

import sd2526.trab.server.discovery.Discovery;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Users;
import sd2526.trab.client.grpc.GrpcUsersClient;
import sd2526.trab.client.rest.RestUsersClient;

/* 
 * This class exemplifies a non-generic client factory for a specific service.
 * 
*/
public class UsersClientFactory {

	private static final String REST = "/rest";
	private static final String GRPC = "/grpc";
	private static final Object DOMAIN_DELIMITER = "@";

	static public Users get(String domain) {
		var sn = "%s%s%s".formatted(Users.SERVICE_NAME, DOMAIN_DELIMITER, domain);
		return newClient(Discovery.knownUrisOf(sn, 1)[0]);
	}

	static private Users newClient(URI serverURI) {
		var path = serverURI.getPath();
		if (path.endsWith(REST))
			return new RestUsersClient(serverURI);
		if (path.endsWith(GRPC))
			return (Users) new GrpcUsersClient(serverURI);
		throw new RuntimeException("Unknown service type..." + serverURI);
	}

	public static void main(String[] args) {

		Users client = UsersClientFactory.get("some_domain");

		Result<User> u = client.getUser("name", "pwd");

		if (u.isOK())
			System.out.println("Got user: " + u.value());
		else
			System.out.println("Got error:" + u.error());
	}

}
