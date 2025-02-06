package controllers;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import forms.user.LoginForm;
import forms.user.RegisterForm;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;


/**
 * Controller which manages the register process for the application.
 * User: tuxburner
 * Date: 2/4/13
 * Time: 1:51 AM
 */
@Singleton
public class RegisterLoginController extends Controller {


  private final FormFactory formFactory;

  private final MessagesApi messagesApi;

  @Inject
  public RegisterLoginController(final FormFactory formFactory, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
  }

  /**
   * Display the register page
   *
   * @return
   */
  public Result showRegister(final Http.Request request) {
    Form<RegisterForm> form = formFactory.form(RegisterForm.class);
    final Messages messages = this.messagesApi.preferred(request);
    return Results.ok(views.html.user.register.render(form, request, messages));
  }

  /**
   * Is a user wants to register validate the form and do the stuff :P
   *
   * @return
   */
  public Result register(final Http.Request request) {
    final Form<RegisterForm> registerForm = formFactory.form(RegisterForm.class).bindFromRequest(request);
    if (registerForm.hasErrors()) {
      final Messages messages = this.messagesApi.preferred(request);
      return Results.badRequest(views.html.user.register.render(registerForm, request, messages));
    } else {

      final String message = messagesApi.preferred(request).at("msg.success.login", registerForm.get().username);
      request.flash().adding("success", message);
      request.session().adding(Secured.AUTH_SESSION, "" + registerForm.get().username);
      return Results.redirect(routes.ApplicationController.index());
    }
  }

  /**
   * Login page.
   */
  public Result showLogin(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return Results.ok(views.html.user.login.render(formFactory.form(LoginForm.class), request, messages));
  }

  /**
   * User wants to authenticate
   *
   * @return
   */
  public Result login(final Http.Request request) {
    final Form<LoginForm> loginForm = formFactory.form(LoginForm.class).bindFromRequest(request);
    final Messages messages = this.messagesApi.preferred(request);
    if (loginForm.hasErrors()) {
      return Results.badRequest(views.html.user.login.render(loginForm, request, messages));
    } else {
      Secured.writeUserToSession(loginForm.get().username, request);
      final String msg = messagesApi.preferred(request).at("msg.success.login", loginForm.get().username);
      return Results.redirect(routes.ApplicationController.index()).flashing("success", msg);
    }
  }


  public Result logout(final Http.Request request) {
    return Results.redirect(routes.RegisterLoginController.login()).withNewSession().flashing("success", messagesApi.preferred(request).at("msg.success.logout"));
  }


}
