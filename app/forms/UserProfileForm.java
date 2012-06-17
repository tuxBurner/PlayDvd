package forms;

import models.User;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import controllers.Secured;

public class UserProfileForm {

  @Formats.NonEmpty
  @Required(message = "Current Password is needed")
  public String currentPassword;

  @Formats.NonEmpty
  @Required(message = "Password is needed")
  @MaxLength(value = 10)
  @MinLength(value = 5)
  public String password;

  public String rePassword;

  /**
   * Checks if the user change the password
   * 
   * @return
   */
  public String validate() {

    final String username = Secured.getUsername();
    final User userByName = User.getUserByName(username);
    if (userByName == null) {
      Logger.error("No user found by the name" + username);
      return "An error happend.";
    }

    return null;
  }
}
