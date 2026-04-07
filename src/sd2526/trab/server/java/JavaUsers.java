package sd2526.trab.server.java;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import sd2526.trab.api.java.Result;
import sd2526.trab.api.java.Result.ErrorCode;

import sd2526.trab.api.User;
import sd2526.trab.api.java.Users;
import sd2526.trab.server.persistence.Hibernate;

public class JavaUsers implements Users {

  private static Logger Log = Logger.getLogger(JavaUsers.class.getName());

  private final String localDomain;
  private Hibernate hibernate;

  public JavaUsers(String domain) {
    this.localDomain = domain;
    hibernate = Hibernate.getInstance();
  }

  @Override
  public Result<String> postUser(User user) {
    Log.info("postUser : " + user);

    // Check if user data is valid
    if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
      Log.info("User object invalid.");
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    if (!user.getDomain().equals(localDomain)) {
      Log.info("Domain mismatch: " + user.getDomain() + " vs " + localDomain);
      return Result.error(ErrorCode.FORBIDDEN);
    }

    try {
      // verificamos se o user existe
      User exists = hibernate.get(User.class, user.getName());

      if (exists != null) {
        if (exists.getPwd().equals(user.getPwd()) &&
            exists.getDisplayName().equals(user.getDisplayName()) &&
            exists.getDomain().equals(user.getDomain())) {

          return Result.ok(exists.getName() + "@" + exists.getDomain());
        }

        return Result.error(ErrorCode.CONFLICT);
      }
      // criamos user novo
      hibernate.persist(user);
      return Result.ok(user.getName() + "@" + user.getDomain());

    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }
  }

  @Override
  public Result<User> getUser(String name, String pwd) {
    Log.info("getUser : name = " + name + "; pwd = " + pwd);

    // Check if parameters are valid
    if (name == null || pwd == null) {
      Log.info("Name or password null.");
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    if (user == null) {
    return Result.error(ErrorCode.FORBIDDEN); 
    }
    if (!user.getPwd().equals(pwd)) {
    return Result.error(ErrorCode.FORBIDDEN);
    }

    return Result.ok(user);
  }

  @Override
  public Result<User> updateUser(String name, String pwd, User info) {
    Log.info("updateUser : name = " + name + "; pwd = " + pwd + " ; info = " + info);

    if (name == null || pwd == null || info == null) {
      Log.info("Dados em falta.");
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    if (info.getName() != null) {
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);

      if (user == null || !user.getPwd().equals(pwd)) {
        Log.info("User does not exist or password is incorrect.");
        return Result.error(ErrorCode.FORBIDDEN);
      }

      if (info.getPwd() != null)
        user.setPwd(info.getPwd());

      if (info.getDisplayName() != null)
        user.setDisplayName(info.getDisplayName());

      if (info.getDomain() != null)
        user.setDomain(info.getDomain());

      hibernate.update(user);

      return Result.ok(user);

    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

  }

  @Override
  public Result<User> deleteUser(String name, String pwd) {
    Log.info("deleteUser : name = " + name + "; pwd = " + pwd);

    if (name == null || pwd == null) {
      Log.info("Name or password null.");
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    if (user == null || !user.getPwd().equals(pwd)) {
      Log.info("User does not exist or password is incorrect.");
      return Result.error(ErrorCode.FORBIDDEN);
    }

    hibernate.delete(user);
    Log.info("User deleted: " + user);
    return Result.ok(user);
  }

  @Override
  public Result<List<User>> searchUsers(String name, String pwd, String query) {
    Log.info("searchUsers : name = " + name + "; pwd = " + pwd + "; query = " + query);

    if (name == null || pwd == null || query == null) {
      Log.info("Dados em falta.");
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);

    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    if (user == null || !user.getPwd().equals(pwd)) {
      Log.info("User nao existe ou password errada");
      return Result.error(ErrorCode.FORBIDDEN);
    }

    List<User> allUsers;

    try {
      allUsers = hibernate.jpql("FROM User", User.class);
    } catch (Exception x) {
      x.printStackTrace();
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    List<User> usersList = new ArrayList<>();
    String queryLower = query.toLowerCase();

    for (User u : allUsers) {
      if (u.getName().toLowerCase().contains(queryLower)) {
        String displayName = u.getDisplayName() != null ? u.getDisplayName() : "";
        String domain = u.getDomain() != null ? u.getDomain() : "";
        User result = new User(u.getName(), "", displayName, domain);
        usersList.add(result);
      }
    }

    return Result.ok(usersList);

  }

  @Override
  public Result<User> authenticateUser(String sender, String pwd) {
    Log.info("authenticateUser : sender = " + sender + "; pwd = " + pwd);

    if (sender == null || pwd == null) {
      return Result.error(ErrorCode.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, sender);
    } catch (Exception x) {
      x.printStackTrace();
      return Result.error(ErrorCode.INTERNAL_ERROR);
    }

    if (user == null) {
      return Result.error(ErrorCode.NOT_FOUND);
    }
    if (!user.getPwd().equals(pwd)) {

      return Result.error(ErrorCode.FORBIDDEN);
    }

    return Result.ok(user);
  }
}
