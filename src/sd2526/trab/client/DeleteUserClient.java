package sd2526.trab.client;

import java.util.logging.Logger;

import sd2526.trab.client.rest.RestUsersClient;

import java.io.IOException;
import java.net.URI;

public class DeleteUserClient {

  private static Logger Log = Logger.getLogger(DeleteUserClient.class.getName());

  public static void main(String[] args) throws IOException {

    if (args.length != 3) {
      System.err.println("Use: java " + DeleteUserClient.class.getCanonicalName() + " url name password");
      return;
    }

    String serverUrl = args[0];
    String name = args[1];
    String password = args[2];

    var client = new RestUsersClient(URI.create(serverUrl));

    var result = client.deleteUser(name, password);
    if (result.isOK())
      Log.info("Deleted user:" + result.value());
    else
      Log.info("Deleted user failed with error: " + result.error());
  }
}
