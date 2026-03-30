package sd2526.trab.client;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.client.rest.RestUsersClient;

public class UpdateUserClient {

  private static Logger Log = Logger.getLogger(UpdateUserClient.class.getName());

  public static void main(String[] args) throws IOException {

    if (args.length < 5) {
      System.err.println("Use: java " + UpdateUserClient.class.getCanonicalName()
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

    Result<User> result = client.updateUser(name, pwd, usr);
    if (result.isOK())
      Log.info("Updated user:" + result.value());
    else
      Log.info("Updated user failed with error: " + result.error());

  }

}