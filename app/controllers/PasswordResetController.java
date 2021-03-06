package controllers;

import forms.user.LostPasswordForm;
import forms.user.PasswordResetForm;
import helpers.MailerHelper;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Controller which handles the password reset process.
 * User: tuxburner
 */
@Singleton
public class PasswordResetController extends Controller {


  /**
   * Factory handling forms.
   */
  private final FormFactory formFactory;

  /**
   * Helper for sending emails.
   */
  private final MailerHelper mailerHelper;

  /**
   * Handles messages
   */
  private final MessagesApi messagesApi;

  @Inject
  PasswordResetController(final FormFactory formFactory, final MailerHelper mailerHelper, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.mailerHelper = mailerHelper;
    this.messagesApi = messagesApi;
  }

  /**
   * Displays the user a simple form where he can insert his mail address
   *
   * @return
   */
  public Result showPasswordForget() {
    if (mailerHelper.mailerActive() == false) {
      return Controller.internalServerError("Cannot display this form.");
    }

    return ok(views.html.user.lostpassword.render(formFactory.form(LostPasswordForm.class)));
  }

  /**
   * Checks if the {@link models.User} exists and if so it sends a password reset mail to the mail the user belongs to
   *
   * @return
   */
  public Result sendPasswordForget() {

    Form<LostPasswordForm> form = formFactory.form(LostPasswordForm.class).bindFromRequest();
    if (form.hasErrors() == false && form.hasGlobalErrors() == false) {


      User userByName = User.getUserByName(form.get().username);
      if (userByName == null) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("A user tries to reset his password with an username (" + form.get().username + ") which does not exists.");
        }
        return redirect(routes.PasswordResetController.showPasswordForget());
      }


      userByName.passwordResetToken = UUID.randomUUID().toString();
      userByName.update();

      final String activationUrl = routes.PasswordResetController.showPasswordReset(userByName.passwordResetToken).absoluteURL(request());

      final String content = messagesApi.preferred(request()).at("email.passwordreset.content",userByName.userName,activationUrl);

      if (Logger.isDebugEnabled() == true) {
        Logger.debug("Email send to: " + userByName.email + " with activation code: " + activationUrl);
      }

      mailerHelper.sendMail(messagesApi.preferred(request()).at("email.passwordreset.subject"), userByName.email, content, false);

      flash("success", messagesApi.preferred(request()).at("msg.success.passwordMailSend"));
    }

    return redirect(routes.PasswordResetController.showPasswordForget());
  }

  /**
   * Displays the password reset form
   *
   * @param token
   * @return
   */
  public Result showPasswordReset(final String token) {
    if (StringUtils.isEmpty(token) == true) {
      return redirect(routes.ApplicationController.index());
    }

    return ok(views.html.user.passwordreset.render(formFactory.form(PasswordResetForm.class), token));
  }

  /**
   * Checks if an {@link User} with the token exists and if so resets the password
   *
   * @param token
   * @return
   */
  public Result passwordReset(final String token) {

    if (StringUtils.isEmpty(token) == true) {
      return redirect(routes.ApplicationController.index());
    }

    Form<PasswordResetForm> passwordResetForm = formFactory.form(PasswordResetForm.class).bindFromRequest();
    if (passwordResetForm.hasErrors()) {
      return Results.badRequest(views.html.user.passwordreset.render(passwordResetForm, token));
    }

    User userByResetToken = User.getUserByResetToken(token);
    if (userByResetToken != null) {
      userByResetToken.password = User.cryptPassword(passwordResetForm.get().password);
      userByResetToken.update();
    }

    flash("success", messagesApi.preferred(request()).at("msg.success.passwordChanged"));
    return redirect(routes.RegisterLoginController.login());
  }
}
