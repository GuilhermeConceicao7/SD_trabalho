package sd2526.trab.client;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import sd2526.trab.api.Message;
import sd2526.trab.api.java.Result;
import sd2526.trab.client.rest.RestMessagesClient;

public class PostMessageClient {

  private static Logger Log = Logger.getLogger(PostMessageClient.class.getName());

  public static void main(String[] args) throws IOException {

    if (args.length < 6) {
      System.err.println("Usa os parametros");
      return;
    }

    String serverUrl = args[0];
    String pwd = args[1];
    String sender = args[2];
    Set<String> destinations = new HashSet<>(Arrays.asList(args[3].split(",")));
    String subjects = args[4];
    String contentes = args[5];

    Message msg = new Message(null, sender, destinations, subjects, contentes);

    RestMessagesClient client = new RestMessagesClient(URI.create(serverUrl));

    Result<String> result = client.postMessage(pwd, msg);
    if (result.isOK())
      Log.info("Post message:" + result.value());
    else
      Log.info("Post msg failed with error: " + result.error());

  }
}
