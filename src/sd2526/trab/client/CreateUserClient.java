package sd2526.trab.client;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.client.rest.RestUsersClient;

public class CreateUserClient {

	private static Logger Log = Logger.getLogger(CreateUserClient.class.getName());

	public static void main(String[] args) throws IOException {

		if (args.length < 5) {
			System.err.println("Use: java " + CreateUserClient.class.getCanonicalName()
					+ " url name pwd displayName domain");
			return;
		}
		String serverUrl = args[0];
		String name = args[1];
		String pwd = args[2];
		String displayName = args[3];
		String domain = args[4];

		User usr = new User(name, pwd, displayName, domain);

		RestUsersClient client = new RestUsersClient(URI.create(serverUrl));

		Result<String> result = client.postUser(usr);
		if (result.isOK())
			Log.info("Created user:" + result.value());
		else
			Log.info("Create user failed with error: " + result.error());

	}

}
