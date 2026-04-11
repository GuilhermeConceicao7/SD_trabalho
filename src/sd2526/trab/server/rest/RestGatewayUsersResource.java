package sd2526.trab.server.rest;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import jakarta.ws.rs.*;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Result;
import sd2526.trab.api.rest.RestUsers;
import sd2526.trab.client.java.UsersClientFactory;

@Singleton
public class RestGatewayUsersResource implements RestUsers {

  private static Logger Log = Logger.getLogger(RestGatewayUsersResource.class.getName());

  private String getDomain(String userId) {
    if (userId.contains("@")) {
      return userId.split("@")[1];
    } else {
      return "ourorg";
    }
  }

  @Override
  public String postUser(User user) {
    try {
      String domain = getDomain(user.getName());
      System.out.println("Posting user to domain: " + domain + " user: " + user.getName());
      return UsersClientFactory.get(domain).postUser(user).value();
    } catch (Exception e) {
      e.printStackTrace();
      throw new WebApplicationException("Erro ao criar user: " + e.getMessage(), 500);
    }
  }

  @Override
  public User getUser(String name, String pwd) {
    String domain = getDomain(name);
    return UsersClientFactory.get(domain).getUser(name, pwd).value();
  }

  @Override
  public User updateUser(String name, String pwd, User info) {
    try {
      info.setName(null);
      info.setDomain(null);
      String domain = getDomain(name);
      return UsersClientFactory.get(domain).updateUser(name, pwd, info).value();
    } catch (Exception e) {
      e.printStackTrace();
      throw new WebApplicationException("Erro ao atualizar user: " + e.getMessage(), 543);
    }
  }

  @Override
  public User deleteUser(String name, String pwd) {
    String domain = getDomain(name);
    return UsersClientFactory.get(domain).deleteUser(name, pwd).value();
  }

  @Override
  public List<User> searchUsers(String name, String pwd, String pattern) {
    String domain = getDomain(name);
    Result<List<User>> result = UsersClientFactory.get(domain).searchUsers(name, pwd, pattern);

    if (!result.isOK()) {
      throw new WebApplicationException("Users service error", 543);
    }

    List<User> users = result.value();
    if (users == null)
      users = Collections.emptyList();

    for (User u : users) {
      if (u != null)
        u.setPwd("");
    }
    return users;
  }

  @Override
  public User authenticateUser(String name, String pwd) {
    String domain = getDomain(name);
    return UsersClientFactory.get(domain).authenticateUser(name, pwd).value();
  }
}