package sd2526.trab.client.rest;

import java.net.URI;
import java.util.logging.Logger;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import sd2526.trab.api.Result;
import sd2526.trab.api.Result.ErrorCode;
import sd2526.trab.api.User;
import sd2526.trab.api.rest.RestMessages;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.client.Entity;
import sd2526.trab.api.Message;
import sd2526.trab.client.rest.RestUsersClient;

public class RestMessagesClient {
  private static Logger Log = Logger.getLogger(RestMessagesClient.class.getName());

  protected static final int READ_TIMEOUT = 5000;
  protected static final int CONNECT_TIMEOUT = 5000;

  protected static final int MAX_RETRIES = 3;
  protected static final int RETRY_SLEEP = 5000;

  final URI serverURI;
  final Client client;
  final ClientConfig config;

  final WebTarget target;

  public RestMessagesClient(URI serverURI) {
    this.serverURI = serverURI;
    this.config = new ClientConfig();

    config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
    config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);

    this.client = ClientBuilder.newClient(config);

    target = client.target(serverURI).path(RestMessages.PATH);

  }

  public Result<String> postMessage(String pwd, Message msg) {

    if (pwd == null || msg == null)
      return Result.error(ErrorCode.BAD_REQUEST);

    String userName = msg.getSender();
    if(userName.contains("@")){
       String[] sender = userName.split("@");
       userName = sender[0];
    } 
    
    Result<User> user = RestUsersClient.getUser(userName, pwd);

    

    if()

    for (int i = 0; i < MAX_RETRIES; i++) {
      try {
        Response r = target.request()
            .accept(MediaType.APPLICATION_JSON)
            .post(Entity.entity(msg, MediaType.APPLICATION_JSON));

        int status = r.getStatus();
        if (status != Status.OK.getStatusCode())
          return Result.error(getErrorCodeFrom(status));
        else
          return Result.ok(r.readEntity(String.class));

      } catch (ProcessingException x) {
        Log.info(x.getMessage());

        try {
          Thread.sleep(RETRY_SLEEP);
        } catch (InterruptedException e) {
          // Nothing to be done here.
        }
      } catch (Exception x) {
        x.printStackTrace();
      }
    }
    return Result.error(ErrorCode.TIMEOUT);
  }

  public static ErrorCode getErrorCodeFrom(int status) {
    return switch (status) {
      case 200, 209 -> ErrorCode.OK;
      case 409 -> ErrorCode.CONFLICT;
      case 403 -> ErrorCode.FORBIDDEN;
      case 404 -> ErrorCode.NOT_FOUND;
      case 400 -> ErrorCode.BAD_REQUEST;
      case 500 -> ErrorCode.INTERNAL_ERROR;
      case 501 -> ErrorCode.NOT_IMPLEMENTED;
      default -> ErrorCode.INTERNAL_ERROR;
    };
  }
}
