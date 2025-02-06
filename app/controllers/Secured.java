package controllers;

import models.User;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

/**
 * This handles the security stuff for the page
 *
 * @author tuxburner
 */
public class Secured extends Security.Authenticator {

  /**
   * Key of the attribute of the user in the session.
   */
  public static final String AUTH_SESSION = "email";

  /**
   * Key of the attribute if the user has a gravatar or not. D
   */
  public static final String AUTH_HAS_GRAVATAR = "has_gravatar";

  /**
   * Gets the name of the user in the session.
   *
   * @return the name of the user
   */
  public static String getUsernameStatic(final Http.Request request) {
    return request.session().get(Secured.AUTH_SESSION).get();
  }


  /**
   * Gets the boolean if the user has a gravatar or not from the session.
   *
   * @return true when the user has a gravatar false when not.
   */
  public static boolean getUserHasGravatar(final Http.Request request) {
    return Boolean.parseBoolean(request.session().get(Secured.AUTH_HAS_GRAVATAR).get());
  }


  /**
   * Sets the user to the session
   *
   * @param username
   * @return
   */
  public static void writeUserToSession(final String username, final Http.Request request) {
    final User userByName = User.getUserByName(username);
    request.session().adding(Secured.AUTH_SESSION, userByName.userName);
    request.session().adding(Secured.AUTH_HAS_GRAVATAR, String.valueOf(userByName.hasGravatar));
  }

  /**
   * Updates the state of the gravatar of the current user in its session.
   * Is used when the user changes his email address.
   *
   * @param hasGravatar tru when the user has a gravatar false when not.
   */
  public static void updateHasGravatar(final Boolean hasGravatar, final Http.Request request) {
    request.session().adding(Secured.AUTH_HAS_GRAVATAR, String.valueOf(hasGravatar));
  }

  /*@Override
  public Optional<String> getUsername(final Http.Request req) {
    final String username = Secured.getUsername(req);

    if (username != null) {
      final boolean checkIfUserExsists = User.checkIfUserExsists(username);
      if (checkIfUserExsists == false) {
        return null;
        req.with.
      }
    }

    return username;
  }*/

  @Override
  public Result onUnauthorized(final Http.Request request) {
    return Results.redirect(routes.RegisterLoginController.login());
  }
}
