package sd2526.trab.client;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Logger;

import sd2526.trab.client.rest.RestUsersClient;

public class SearchUsersClient {

  private static Logger Log = Logger.getLogger(SearchUsersClient.class.getName());

  public static void main(String[] args) throws IOException {

    if (args.length != 4) {
      System.err.println("Use: java " + SearchUsersClient.class.getCanonicalName() + " url name password query");
      return;
    }

    String serverUrl = args[0];
    String name = args[1];
    String password = args[2];
    String query = args[3];

    var client = new RestUsersClient(URI.create(serverUrl));

    var result = client.searchUsers(name, password, query);
    if (result.isOK())
      Log.info("Search users:" + result.value());
    else
      Log.info("Search users failed with error: " + result.error());

  }
}
