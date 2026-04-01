package sd2526.trab.server.rest;

import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;

import sd2526.trab.api.User;
import sd2526.trab.api.rest.RestUsers;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.java.JavaUsers;

@Singleton
public class RestUsersResource extends RestResource implements RestUsers {

  private static Logger Log = Logger.getLogger(RestUsersResource.class.getName());

  final Users impl;

  public RestUsersResource() {
    impl = new JavaUsers();
  }

  @Override
  public String postUser(User user) {
    Log.info("postUser : " + user);

    return super.unwrapResultOrThrow(impl.postUser(user));
  }

  @Override
  public User getUser(String name, String pwd) {
    Log.info("getUser : name = " + name + "; pwd = " + pwd);

    return super.unwrapResultOrThrow(impl.getUser(name, pwd));
  }

  @Override
  public User updateUser(String name, String pwd, User info) {
    Log.info("updateUser : name = " + name + "; pwd = " + pwd + " ; info = " + info);

    return super.unwrapResultOrThrow(impl.updateUser(name, pwd, info));
  }

  @Override
  public User deleteUser(String name, String pwd) {
    Log.info("deleteUser : name = " + name + "; pwd = " + pwd);

    return super.unwrapResultOrThrow(impl.deleteUser(name, pwd));
  }

  @Override
  public List<User> searchUsers(String name, String pwd, String query) {
    Log.info("searchUsers : name = " + name + "; pwd = " + pwd + "; query = " + query);

    return super.unwrapResultOrThrow(impl.searchUsers(name, pwd, query));
  }

  @Override
  public User authenticateUser(String name, String pwd) {
    Log.info("authenticateUser : name = " + name + "; pwd = " + pwd);

    return super.unwrapResultOrThrow(impl.authenticateUser(name, pwd));
  }
}
