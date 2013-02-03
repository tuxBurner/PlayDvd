package controllers;

import forms.UserProfileForm;
import models.User;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.user.userprofile;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 4:24 PM
 */
@Security.Authenticated(Secured.class)
public class UserProfileController extends Controller {

  /**
   * Displays the user profile mask
   *
   * @return
   */
  public static Result showProfile() {

    User currentUser = User.getCurrentUser();
    if(currentUser == null) {
      if(Logger.isErrorEnabled()) {
        Logger.error("No user was found by the username: "+Secured.getUsername());
        return internalServerError();
      }
    }

    UserProfileForm userProfileForm = new UserProfileForm();
    userProfileForm.defaultCopyType = currentUser.defaultCopyType;
    userProfileForm.email = currentUser.email;

    return ok(userprofile.render(Controller.form(UserProfileForm.class).fill(userProfileForm)));
  }

  public static Result updateProfile() {

    final Form<UserProfileForm> form = Controller.form(UserProfileForm.class).bindFromRequest();
    if(form.hasErrors()) {
      return Results.badRequest(userprofile.render(form));
    }

    Controller.flash("success", "Your profile has been updated");
    // redirect so the form gets empty and no passwords are written to the form
    return redirect(routes.UserProfileController.showProfile());
  }
}
