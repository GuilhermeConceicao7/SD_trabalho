package sd2526.trab.client.grpc;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.grpc.GrpcUsersGrpc;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.grpc.util.DataModelAdaptor;
import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Result.ErrorCode;

public class GrpcUsersClient extends GrpcClient implements Users {

  private static Logger Log = Logger.getLogger(GrpcUsersClient.class.getName());

  final GrpcUsersGrpc.GrpcUsersBlockingStub stub;

  public GrpcUsersClient(URI serverURI) {
    super(serverURI, Log);

    stub = GrpcUsersGrpc.newBlockingStub(channel);
  }

  @Override
  public Result<String> postUser(User user) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<User> getUser(String userId, String password) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<User> updateUser(String userId, String pwd, User user) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<User> deleteUser(String userId, String pwd) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<List<User>> searchUsers(String name, String pwd, String query) {
    throw new RuntimeException(ErrorCode.NOT_IMPLEMENTED.toString());
  }

  @Override
  public Result<User> authenticateUser(String sender, String pwd) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'authenticateUser'");
  }

}