package forms.user;

import controllers.Secured;
import helpers.DvdInfoHelper;
import helpers.GravatarHelper;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.mvc.Http;

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
  public String validate(final Http.Request request) {


    final User userToUpdate = User.getCurrentUser(request);
    if (userToUpdate == null) {
      Logger.error("No user found by the name: " + Secured.getUsernameStatic(request));
      return "msg.error";
    }

    if (StringUtils.isEmpty(password) == false && StringUtils.isEmpty(rePassword) == false) {
      Logger.debug("User: " + Secured.getUsernameStatic(request) + " wants to change the password.");
      if (StringUtils.equals(password, rePassword) == false) {
        Logger.error(Secured.getUsernameStatic(request) + " did not entered matched passwords.");
        return "msg.error.passwordsNoMatch";
      }


      userToUpdate.password = User.cryptPassword(password);
    }

    if (StringUtils.isEmpty(defaultCopyType) == false) {
      Logger.debug(Secured.getUsernameStatic(request) + " sets defaultCopyType to: " + defaultCopyType);
      List<String> copyTypes = DvdInfoHelper.getCopyTypes();
      if (copyTypes.contains(defaultCopyType) == false) {
        Logger.error("User: " + Secured.getUsernameStatic(request) + " selected a copyType: " + defaultCopyType + " which is not configured.");
        return "The selected copytype: " + defaultCopyType + " does not exists.";
      }

      userToUpdate.defaultCopyType = defaultCopyType;
    }

    userToUpdate.email = email;

    byte[] gravatarBytes = GravatarHelper.getGravatarBytes(userToUpdate.email, 16);
    userToUpdate.hasGravatar = (gravatarBytes != null);

    userToUpdate.save();

    Secured.updateHasGravatar(userToUpdate.hasGravatar, request);

    return null;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getRePassword() {
    return rePassword;
  }

  public void setRePassword(String rePassword) {
    this.rePassword = rePassword;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getDefaultCopyType() {
    return defaultCopyType;
  }

  public void setDefaultCopyType(String defaultCopyType) {
    this.defaultCopyType = defaultCopyType;
  }

  public String getRssAuthKey() {
    return rssAuthKey;
  }

  public void setRssAuthKey(String rssAuthKey) {
    this.rssAuthKey = rssAuthKey;
  }
}
