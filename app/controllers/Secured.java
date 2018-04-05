package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import play.mvc.Http.Context;

/**
 * This handles the security stuff for the page
 * 
 * @author tuxburner
 * 
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
   * @return the name of the user
   */
  public static String getUsername() {
    return Controller.session(Secured.AUTH_SESSION);
  }


  /**
   * Gets the boolean if the user has a gravatar or not from the session.
   * @return true when the user has a gravatar false when not.
   */
  public  static boolean getUserHasGravatar() {
    return Boolean.parseBoolean(Controller.session(Secured.AUTH_HAS_GRAVATAR));
  }



  /**
   * Sets the user to the session
   * 
   * @param username
   * @return
   */
  public static void writeUserToSession(final String username) {
    final User userByName = User.getUserByName(username);
    Controller.session(Secured.AUTH_SESSION, userByName.userName);
    Controller.session(Secured.AUTH_HAS_GRAVATAR,String.valueOf(userByName.hasGravatar));
  }

  /**
   * Updates the state of the gravatar of the current user in its session.
   * Is used when the user changes his email address.
   * @param hasGravatar tru when the user has a gravatar false when not.
   */
  public static void updateHasGravatar(final Boolean hasGravatar) {
    Controller.session(Secured.AUTH_HAS_GRAVATAR,String.valueOf(hasGravatar));
  }

  @Override
  public String getUsername(final Context ctx) {
    final String username = Secured.getUsername();

    if (username != null) {
      final boolean checkIfUserExsists = User.checkIfUserExsists(username);
      if (checkIfUserExsists == false) {
        ctx.session().clear();
        return null;
      }
    }

    return username;
  }

  @Override
  public Result onUnauthorized(final Context arg0) {
    return Results.redirect(routes.RegisterLoginController.login());
  }
}
