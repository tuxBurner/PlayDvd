package forms.user;

import controllers.Secured;
import helpers.DvdInfoHelper;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.data.validation.Constraints.MinLength;
import play.i18n.Messages;

import java.util.List;

public class UserProfileForm {

  @MaxLength(value = 10)
  public String password;

  public String rePassword;

  @Constraints.Required(message = "msg.error.noEmail")
  @Constraints.Email(message = "msg.error.invalidEmail")
  public String email;

  public String defaultCopyType;

  public String rssAuthKey;

  /**
   * Checks if the user change the password
   * 
   * @return
   */
  public String validate() {


    final User userToUpdate = User.getCurrentUser();
    if (userToUpdate == null) {
      Logger.error("No user found by the name: " + Secured.getUsername());
      return Messages.get("msg.error");
    }



    if(StringUtils.isEmpty(password) == false && StringUtils.isEmpty(rePassword) == false) {
      Logger.debug("User: "+Secured.getUsername()+" wants to change the password.");
      if(StringUtils.equals(password,rePassword) == false) {
        Logger.error(Secured.getUsername()+" did not entered matched passwords.");
        return Messages.get("msg.error.passwordsNoMatch");
      }


      userToUpdate.password = User.cryptPassword(password);
    }

    if(StringUtils.isEmpty(defaultCopyType) == false) {
      Logger.debug(Secured.getUsername()+" sets defaultCopyType to: "+defaultCopyType);
      List<String> copyTypes = DvdInfoHelper.getCopyTypes();
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
