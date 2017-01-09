package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import forms.user.LoginForm;
import forms.user.RegisterForm;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.user.login;
import views.html.user.register;

/**
 * Controller which manages the register process for the application.
 * User: tuxburner
 * Date: 2/4/13
 * Time: 1:51 AM
 */
@Singleton
public class RegisterLoginController extends Controller {


  private final FormFactory formFactory;

  @Inject
  public RegisterLoginController(final FormFactory formFactory) {

    this.formFactory = formFactory;
  }

  /**
   * Display the register page
   *
   * @return
   */
  public Result showRegister() {
    Form<RegisterForm> form = formFactory.form(RegisterForm.class);
    return Results.ok(register.render(form));
  }

  /**
   * Is a user wants to register validate the form and do the stuff :P
   *
   * @return
   */
  public Result register() {
    final Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class).bindFromRequest();
    if (registerForm.hasErrors()) {
      return Results.badRequest(register.render(registerForm));
    } else {

      final String message = Messages.get("msg.success.login", registerForm.get().username);
      Controller.flash("success", message);
      Controller.session(Secured.AUTH_SESSION, "" + registerForm.get().username);
      return Results.redirect(routes.ApplicationController.index());
    }
  }

  /**
   * Login page.
   */
  public Result showLogin() {
    return Results.ok(login.render(formFactory.form(LoginForm.class)));
  }

  /**
   * User wants to authenticate
   *
   * @return
   */
  public Result login() {
    final Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest();
    if (loginForm.hasErrors()) {
      return Results.badRequest(login.render(loginForm));
    } else {
      Secured.writeUserToSession(loginForm.get().username);
      final String msg = Messages.get("msg.success.login", loginForm.get().username);
      Controller.flash("success", msg);
      return Results.redirect(routes.ApplicationController.index());
    }
  }


  public Result logout() {
    Controller.session().clear();
    Controller.flash("success", Messages.get("msg.success.logout"));
    return Results.redirect(routes.RegisterLoginController.login());
  }


}
