package controllers;

import forms.user.LoginForm;
import forms.user.RegisterForm;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.user.login;
import views.html.user.register;

/**
 * User: tuxburner
 * Date: 2/4/13
 * Time: 1:51 AM
 */
public class RegisterLoginController extends Controller {

  /**
   * Display the register page
   *
   * @return
   */
  public static Result showRegister() {
    return Results.ok(register.render(Form.form(RegisterForm.class)));
  }

  /**
   * Is a user wants to register validate the form and do the stuff :P
   *
   * @return
   */
  public static Result register() {
    final Form<RegisterForm> registerForm = Form.form(RegisterForm.class).bindFromRequest();
    if (registerForm.hasErrors()) {
      return Results.badRequest(register.render(registerForm));
    } else {
      Controller.flash("success", "Welcome to  the DVD-Database: " + registerForm.get().username);
      Controller.session(Secured.AUTH_SESSION, "" + registerForm.get().username);
      return Results.redirect(routes.Application.index());
    }
  }

  /**
   * Login page.
   */
  public static Result showLogin() {
    return Results.ok(login.render(Form.form(LoginForm.class)));
  }

  /**
   * User wants to authenticate
   *
   * @return
   */
  public static Result login() {
    final Form<LoginForm> loginForm = Form.form(LoginForm.class).bindFromRequest();
    if (loginForm.hasErrors()) {
      return Results.badRequest(login.render(loginForm));
    } else {
      Secured.writeUserToSession(loginForm.get().username);
      return Results.redirect(routes.Application.index());
    }
  }


  public static Result logout() {
    Controller.session().clear();
    Controller.flash("success", "You've been logged out");
    return Results.redirect(routes.RegisterLoginController.login());
  }


}
