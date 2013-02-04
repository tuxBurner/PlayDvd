package controllers;

import forms.LoginForm;
import forms.LostPasswordForm;
import forms.PasswordResetForm;
import forms.RegisterForm;
import helpers.MailerHelper;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.login;
import views.html.register;
import views.html.user.lostpassword;
import views.html.user.passwordreset;

import java.util.UUID;

public class Application extends Controller {


  @Security.Authenticated(Secured.class)
  public static Result index() {
    return redirect(routes.ListDvdsController.listAlldvds());
  }

  /**
   * Login page.
   */
  public static Result login() {
    return Results.ok(login.render(Controller.form(LoginForm.class)));
  }

  public static Result logout() {
    Controller.session().clear();
    Controller.flash("success", "You've been logged out");
    return Results.redirect(routes.Application.login());
  }

  /**
   * Display the register page
   *
   * @return
   */
  public static Result register() {
    return Results.ok(register.render(Controller.form(RegisterForm.class)));
  }

  /**
   * User wants to authenticate
   *
   * @return
   */
  public static Result authenticate() {
    final Form<LoginForm> loginForm = Controller.form(LoginForm.class).bindFromRequest();
    if (loginForm.hasErrors()) {
      return Results.badRequest(login.render(loginForm));
    } else {
      Secured.writeUserToSession(loginForm.get().username);
      return Results.redirect(routes.Application.index());
    }
  }

  /**
   * Is a user wants to register validate the form and do the stuff :P
   *
   * @return
   */
  public static Result registeruser() {
    final Form<RegisterForm> registerForm = Controller.form(RegisterForm.class).bindFromRequest();
    if (registerForm.hasErrors()) {
      return Results.badRequest(register.render(registerForm));
    } else {
      Controller.flash("success", "Welcome to  the DVD-Database: " + registerForm.get().username);
      Controller.session(Secured.AUTH_SESSION, "" + registerForm.get().username);
      return Results.redirect(routes.Application.index());
    }

  }

  /**
   * Displays the user a simple form where he can insert his mail address
   *
   * @return
   */
  public static Result showPasswordForget() {
    if (MailerHelper.mailerActive() == false) {
      return Controller.internalServerError("Cannot display this form.");
    }

    return ok(lostpassword.render(Controller.form(LostPasswordForm.class)));
  }

  /**
   * Checks if the {@link User} exists and if so it sends a password reset mail to the mail the user belongs to
   * @return
   */
  public static Result sendPasswordForget() {

    Form<LostPasswordForm> form = Controller.form(LostPasswordForm.class).bindFromRequest();
    flash("success","Please check your email.");
    if (form.hasErrors() == false && form.hasGlobalErrors() == false) {


      User userByName = User.getUserByName(form.get().username);
      if (userByName == null) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("A user tries to reset his password with an username (" + form.get().username + ") which does not exists.");
          return redirect(routes.Application.showPasswordForget());
        }
      }


      userByName.passwordResetToken = UUID.randomUUID().toString();
      userByName.update();

      final StringBuffer sb = new StringBuffer("Hello ");
      sb.append(userByName.userName);
      sb.append("\n");
      sb.append("You requested to reset the password for the PlayDvd database please click the link to reset the password:");
      sb.append("\n\t");
      final String activationUrl = routes.Application.showPasswordReset(userByName.passwordResetToken).absoluteURL(request());
      sb.append(activationUrl);

      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Email send to: "+userByName.email+" with activation code: "+activationUrl);
      }

      MailerHelper.sendMail("Password reset by PlayDvd", userByName.email, sb.toString(), false);
    }

    return redirect(routes.Application.showPasswordForget());
  }

  /**
   * Displays the password reset form
   * @param token
   * @return
   */
  public static Result showPasswordReset(final String token) {


    if(StringUtils.isEmpty(token) == true) {
      return redirect(routes.Application.index());
    }

    return ok(passwordreset.render(Controller.form(PasswordResetForm.class),token));
  }

  /**
   * Checks if an {@link User} with the token exists and if so resets the password
   * @param token
   * @return
   */
  public static Result passwordReset(final String token) {

    if(StringUtils.isEmpty(token) == true) {
      return redirect(routes.Application.index());
    }

    Form<PasswordResetForm> passwordResetForm = Controller.form(PasswordResetForm.class).bindFromRequest();
    if(passwordResetForm.hasErrors()) {
      return Results.badRequest(passwordreset.render(passwordResetForm,token));
    }

    User userByResetToken = User.getUserByResetToken(token);
    if(userByResetToken != null) {
      userByResetToken.password = User.cryptPassword(passwordResetForm.get().password);
      userByResetToken.update();
    }

    flash("success", "Password was changed.");
    return redirect(routes.Application.login());
  }

  /**
   * Register the routes to certain stuff to the javascript routing so we can
   * reach it better from there
   *
   * @return
   */
  public static Result jsRoutes() {
    Controller.response().setContentType("text/javascript");
    return Results.ok(Routes.javascriptRouter(
        "jsRoutes",
        controllers.routes.javascript.InfoGrabberController.searchGrabber(),
        controllers.routes.javascript.InfoGrabberController.getMovieById(),
        controllers.routes.javascript.Dashboard.displayDvd(),
        controllers.routes.javascript.Dashboard.lendDialogContent(),
        controllers.routes.javascript.Dashboard.unLendDialogContent(),
        controllers.routes.javascript.Dashboard.lendDvd(),
        controllers.routes.javascript.Dashboard.unlendDvd(),
        controllers.routes.javascript.Dashboard.deleteDialogContent(),
        controllers.routes.javascript.Dashboard.deleteDvd(),
        controllers.routes.javascript.Dashboard.streamImage(),
        controllers.routes.javascript.MovieController.showAddMovieForm(),
        controllers.routes.javascript.MovieController.showEditMovieForm(),
        controllers.routes.javascript.MovieController.addMovieByGrabberId(),
        controllers.routes.javascript.MovieController.addOrEditMovie(),
        controllers.routes.javascript.MovieController.searchMoviesForDvdSelect(),
        controllers.routes.javascript.MovieController.searchForMovieAttribute()));
  }
}