package controllers;

import dao.UserDao;
import forms.user.LostPasswordForm;
import forms.user.PasswordResetForm;
import helpers.MailerHelper;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
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
  public Result showPasswordForget(final Http.Request request) {
    if (mailerHelper.mailerActive() == false) {
      return Controller.internalServerError("Cannot display this form.");
    }

    final Messages messages = this.messagesApi.preferred(request);

    return ok(views.html.user.lostpassword.render(formFactory.form(LostPasswordForm.class), request, messages));
  }

  /**
   * Checks if the {@link models.User} exists and if so it sends a password reset mail to the mail the user belongs to
   *
   * @return
   */
  public Result sendPasswordForget(final Http.Request request) {

    Form<LostPasswordForm> form = formFactory.form(LostPasswordForm.class).bindFromRequest(request);
    if (form.hasErrors() == false && form.hasGlobalErrors() == false) {


      User userByName = UserDao.findUserByName(form.get().username);
      if (userByName == null) {
        if (Logger.isErrorEnabled() == true) {
          Logger.error("A user tries to reset his password with an username (" + form.get().username + ") which does not exists.");
        }
        return redirect(routes.PasswordResetController.showPasswordForget());
      }


      userByName.setPasswordResetToken(UUID.randomUUID().toString());
      userByName.update();

      final String activationUrl = routes.PasswordResetController.showPasswordReset(userByName.getPasswordResetToken()).absoluteURL(request);

      final String content = messagesApi.preferred(request).at("email.passwordreset.content", userByName.getUserName(), activationUrl);

      if (Logger.isDebugEnabled() == true) {
        Logger.debug("Email send to: " + userByName.getEmail() + " with activation code: " + activationUrl);
      }

      mailerHelper.sendMail(messagesApi.preferred(request).at("email.passwordreset.subject"), userByName.getEmail(), content, false);

      request.flash().adding("success", messagesApi.preferred(request).at("msg.success.passwordMailSend"));
    }

    return redirect(routes.PasswordResetController.showPasswordForget());
  }

  /**
   * Displays the password reset form
   *
   * @param token
   * @return
   */
  public Result showPasswordReset(final String token, final Http.Request request) {
    if (StringUtils.isEmpty(token) == true) {
      return redirect(routes.ApplicationController.index());
    }

    final Messages messages = this.messagesApi.preferred(request);

    return ok(views.html.user.passwordreset.render(formFactory.form(PasswordResetForm.class), token, request, messages));
  }

  /**
   * Checks if an {@link User} with the token exists and if so resets the password
   *
   * @param token
   * @return
   */
  public Result passwordReset(final String token, final Http.Request request) {

    if (StringUtils.isEmpty(token) == true) {
      return redirect(routes.ApplicationController.index());
    }

    Form<PasswordResetForm> passwordResetForm = formFactory.form(PasswordResetForm.class).bindFromRequest(request);

    final Messages messages = this.messagesApi.preferred(request);

    if (passwordResetForm.hasErrors()) {
      return Results.badRequest(views.html.user.passwordreset.render(passwordResetForm, token, request, messages));
    }

    User userByResetToken = UserDao.findUserByResetToken(token);
    if (userByResetToken != null) {
      userByResetToken.setPassword(UserDao.cryptPassword(passwordResetForm.get().password));
      userByResetToken.update();
    }


    return redirect(routes.RegisterLoginController.login()).flashing("success", messagesApi.preferred(request).at("msg.success.passwordChanged"));
  }
}
