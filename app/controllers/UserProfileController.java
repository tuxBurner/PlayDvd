package controllers;

import dao.UserDao;
import forms.user.UserProfileForm;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.user.userprofile;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * User: tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class UserProfileController extends Controller {

  private final FormFactory formFactory;

  private final MessagesApi messagesApi;

  @Inject
  UserProfileController(final FormFactory formFactory, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
  }

  /**
   * Displays the user profile mask
   *
   * @return
   */
  public Result showProfile(final Http.Request request) {

    User currentUser = UserDao.findCurrentUser(request);
    if (currentUser == null) {
      if (Logger.isErrorEnabled()) {
        Logger.error("No user was found by the username: " + Secured.getUsernameStatic(request));
      }
      return internalServerError();
    }

    if (StringUtils.isEmpty(currentUser.getRssAuthKey()) == true) {
      currentUser = UserDao.createUserRssAuthKey(currentUser);
    }

    UserProfileForm userProfileForm = new UserProfileForm();
    userProfileForm.defaultCopyType = currentUser.getDefaultCopyType();
    userProfileForm.email = currentUser.getEmail();
    userProfileForm.rssAuthKey = currentUser.getRssAuthKey();

    final Messages messages = this.messagesApi.preferred(request);


    return ok(userprofile.render(formFactory.form(UserProfileForm.class).fill(userProfileForm), request, messages));
  }

  public Result updateProfile(final Http.Request request) {
    final Form<UserProfileForm> form = formFactory.form(UserProfileForm.class).bindFromRequest(request);

    final Messages messages = this.messagesApi.preferred(request);

    if (form.hasErrors()) {
      return Results.badRequest(userprofile.render(form, request, messages));
    }

    // redirect so the form gets empty and no passwords are written to the form
    return redirect(routes.UserProfileController.showProfile()).flashing("success", messagesApi.preferred(request).at("msg.success.profileUpdated"));
  }
}
