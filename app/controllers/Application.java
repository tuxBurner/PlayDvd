package controllers;

import forms.LoginForm;
import forms.RegisterForm;
import helpers.MailerHeler;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.login;
import views.html.register;
import views.html.user.lostpassword;

public class Application extends Controller {


  @Security.Authenticated(Secured.class)
  public static Result index() {
    return Results.redirect(routes.ListDvds.listAlldvds());
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
      return Results.redirect(routes.ListDvds.listdvds(0));
    }

  }

  /**
   * Displays the user a simple form where he can insert his mail address
   * @return
   */
  public static Result showPasswordForget() {
    if(MailerHeler.mailerActive() == false) {
      return Controller.internalServerError("Cannot display this form.");
    }

    return ok(lostpassword.render(null));
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
        // controllers.routes.javascript.ListDvds.listdvds(),
        controllers.routes.javascript.MovieController.showAddMovieForm(),
        controllers.routes.javascript.MovieController.showEditMovieForm(),
        controllers.routes.javascript.MovieController.addMovieByGrabberId(),
        controllers.routes.javascript.MovieController.addOrEditMovie(),
        controllers.routes.javascript.MovieController.searchMoviesForDvdSelect(),
        controllers.routes.javascript.MovieController.searchForMovieAttribute()));
  }
}