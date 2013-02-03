package forms;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.data.validation.Constraints.Required;
import controllers.Secured;

import java.util.List;

public class UserProfileForm {

  @MaxLength(value = 10)
  @MinLength(value = 5)
  public String password;

  public String rePassword;

  @Constraints.Required(message = "Email is required")
  @Constraints.Email(message = "The entered Email is not an email")
  public String email;

  public String defaultCopyType;

  /**
   * Checks if the user change the password
   * 
   * @return
   */
  public String validate() {


    final User userToUpdate = User.getCurrentUser();
    if (userToUpdate == null) {
      Logger.error("No user found by the name: " + Secured.getUsername());
      return "An error happend.";
    }



    if(StringUtils.isEmpty(password) == false && StringUtils.isEmpty(rePassword) == false) {
      Logger.debug("User: "+Secured.getUsername()+" wants to change the password.");
      if(StringUtils.equals(password,rePassword) == false) {
        Logger.error(Secured.getUsername()+" did not entered matched passwords.");
        return "Password do not match";
      }


      userToUpdate.password = User.cryptPassword(password);
    }

    if(StringUtils.isEmpty(defaultCopyType) == false) {
      Logger.debug(Secured.getUsername()+" sets defaultCopyType to: "+defaultCopyType);
      List<String> copyTypes = DvdForm.getCopyTypes();
      if(copyTypes.contains(defaultCopyType) == false) {
        Logger.error("User: "+Secured.getUsername()+" selected a copyType: "+defaultCopyType+" which is not configured.");
        return "The selected copytype: "+defaultCopyType+" does not exists.";
      }

      userToUpdate.defaultCopyType = defaultCopyType;
    }

    userToUpdate.email = email;

    userToUpdate.save();

    return null;
  }
}
