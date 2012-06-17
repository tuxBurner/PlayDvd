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

  public static final String AUTH_SESSION = "email";

  public static String getUsername() {
    return Controller.ctx().session().get(Secured.AUTH_SESSION);
  }

  @Override
  public String getUsername(final Context ctx) {
    final String username = ctx.session().get(Secured.AUTH_SESSION);

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
    return Results.redirect(routes.Application.login());
  }
}
