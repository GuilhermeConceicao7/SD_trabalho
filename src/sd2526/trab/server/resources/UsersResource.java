package sd2526.trab.server.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.hibernate.exception.ConstraintViolationException;

import jakarta.inject.Singleton;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

import sd2526.trab.api.User;
import sd2526.trab.api.rest.RestUsers;
import sd2526.trab.server.persistence.Hibernate;

@Singleton
public class UsersResource implements RestUsers {

  private final Hibernate hibernate;

  private static Logger Log = Logger.getLogger(UsersResource.class.getName());

  public UsersResource() {
    hibernate = Hibernate.getInstance();
  }

  @Override
  public String postUser(User user) {
    Log.info("postUser : " + user);

    // Check if user data is valid
    if (user.getName() == null || user.getPwd() == null || user.getDisplayName() == null || user.getDomain() == null) {
      Log.info("User object invalid.");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    try {
      // verificamos se o user existe
      User exists = hibernate.get(User.class, user.getName());

      if (exists != null) {
        if (exists.getPwd().equals(user.getPwd()) &&
            exists.getDisplayName().equals(user.getDisplayName()) &&
            exists.getDomain().equals(user.getDomain())) {

          return exists.getName() + "@" + exists.getDomain();
        }

        throw new WebApplicationException(Status.CONFLICT);
      }
      // criamos user novo
      hibernate.persist(user);
      return user.getName() + "@" + user.getDomain();

    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  public User getUser(String name, String pwd) {
    Log.info("getUser : name = " + name + "; pwd = " + pwd);

    // Check if parameters are valid
    if (name == null || pwd == null) {
      Log.info("Name or password null.");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check if user exists and password matches
    if (user == null || !user.getPwd().equals(pwd)) {
      Log.info("User does not exist or password is incorrect.");
      throw new WebApplicationException(Status.FORBIDDEN);
    }

    return user;
  }

  @Override
  public User updateUser(String name, String pwd, User info) {
    Log.info("updateUser : name = " + name + "; pwd = " + pwd + " ; info = " + info);

    if (name == null || pwd == null || info == null) {
      Log.info("Dados em falta.");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    if (info.getName() != null) {
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);

      if (user == null || !user.getPwd().equals(pwd)) {
        Log.info("User nao existe ou password errada");
        throw new WebApplicationException(Status.FORBIDDEN);
      }

      if (info.getPwd() != null)
        user.setPwd(info.getPwd());

      if (info.getDisplayName() != null)
        user.setDisplayName(info.getDisplayName());

      if (info.getDomain() != null)
        user.setDomain(info.getDomain());

      hibernate.update(user);

      return user;

    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

  }

  @Override
  public User deleteUser(String name, String pwd) {
    Log.info("deleteUser : name = " + name + "; pwd = " + pwd);

    if (name == null || pwd == null) {
      Log.info("Name or password null.");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    // Check if user exists and password matches
    if (user == null || !user.getPwd().equals(pwd)) {
      Log.info("User does not exist or password is incorrect.");
      throw new WebApplicationException(Status.FORBIDDEN);
    }

    hibernate.delete(user);
    Log.info("User deleted: " + user);
    return user;
  }

  @Override
  public List<User> searchUsers(String name, String pwd, String query) {
    Log.info("searchUsers : name = " + name + "; pwd = " + pwd + "; query = " + query);

    if (name == null || pwd == null) {
      Log.info("Dados em falta.");
      throw new WebApplicationException(Status.BAD_REQUEST);
    }

    User user = null;
    try {
      user = hibernate.get(User.class, name);

    } catch (WebApplicationException e) {
      throw e;
    } catch (Exception x) {
      x.printStackTrace(); // Un-expected exception. Signal internal server error.
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    if (user == null || !user.getPwd().equals(pwd)) {
      Log.info("User nao existe ou password errada");
      throw new WebApplicationException(Status.FORBIDDEN);
    }

    List<User> allUsers;

    try {
      allUsers = hibernate.jpql("FROM User", User.class);
    } catch (Exception x) {
      x.printStackTrace();
      throw new WebApplicationException(Status.INTERNAL_SERVER_ERROR);
    }

    List<User> usersList = new ArrayList<>();

    for (User u : allUsers) {
      if (u.getName().toLowerCase().contains(query.toLowerCase())) {
        User result = new User(u.getName(), "", u.getDisplayName(), u.getDomain());
        usersList.add(result);
      }
    }

    return usersList;

  }
}
