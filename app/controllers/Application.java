package controllers;

import models.User;
import play.Routes;
import play.data.Form;
import play.data.format.Formats;
import play.data.validation.Constraints.Email;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.login;
import views.html.register;

public class Application extends Controller {

  // TODO: move to own class
  // -- Authentication
  public static class Login {

    public String username;

    public String password;

    public String validate() {

      final User user = User.authenticate(username, password);
      if (user == null) {
        return "Invalid user or password";
      }
      return null;
    }
  }

  // TODO: move to own class
  public static class Register {

    @Formats.NonEmpty
    @Required(message = "Username is needed")
    @MaxLength(value = 10)
    @MinLength(value = 5)
    public String username;

    @Formats.NonEmpty
    @Required(message = "Password is needed")
    @MaxLength(value = 10)
    @MinLength(value = 5)
    public String password;

    public String repassword;

    @Required(message = "Email is required")
    @Email(message = "The entered Email is not an email")
    public String email;

    public String validate() {

      if (password.equals(repassword) == false) {
        return "Passwords dont match";
      }

      // check if the username is unique
      final boolean checkIfUserExsists = User.checkIfUserExsists(username);
      if (checkIfUserExsists == true) {
        return "User: " + username + " already exists";
      }

      final User user = new User();
      user.email = email;
      user.userName = username;
      user.password = password;
      User.create(user);

      return null;
    }
  }

  @Security.Authenticated(Secured.class)
  public static Result index() {
    return Results.redirect(routes.ListDvds.listAlldvds());
  }

  /**
   * Login page.
   */
  public static Result login() {
    return Results.ok(login.render(Controller.form(Login.class)));
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
    return Results.ok(register.render(Controller.form(Register.class)));
  }

  /**
   * User wants to authenticate
   * 
   * @return
   */
  public static Result authenticate() {
    final Form<Login> loginForm = Controller.form(Login.class).bindFromRequest();
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
    final Form<Register> registerForm = Controller.form(Register.class).bindFromRequest();
    if (registerForm.hasErrors()) {
      return Results.badRequest(register.render(registerForm));
    } else {
      Controller.flash("success", "Welcome to  the DVD-Database: " + registerForm.get().username);
      Controller.session(Secured.AUTH_SESSION, "" + registerForm.get().username);
      return Results.redirect(routes.ListDvds.listdvds(0));
    }

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
        controllers.routes.javascript.InfoGrabberController.searchTmdb(),
        controllers.routes.javascript.InfoGrabberController.getMovieById(),
        controllers.routes.javascript.Dashboard.displayDvd(),
        controllers.routes.javascript.Dashboard.menuGenres(),
        controllers.routes.javascript.Dashboard.lendDialogContent(),
        controllers.routes.javascript.Dashboard.lendDvd(),
        controllers.routes.javascript.MovieController.showAddMovieForm(),
        controllers.routes.javascript.MovieController.showEditMovieForm(),
        controllers.routes.javascript.MovieController.addMovieByTmdbId(),
        controllers.routes.javascript.MovieController.addOrEditMovie()));

  }
}