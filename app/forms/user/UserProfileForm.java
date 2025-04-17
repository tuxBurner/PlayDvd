package forms.user;

import dao.UserDao;
import helpers.DvdInfoHelper;
import helpers.GravatarHelper;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.validation.Constraints;
import play.data.validation.Constraints.MaxLength;
import play.mvc.Security;

import java.util.List;

@Constraints.ValidateWithPayload
public class UserProfileForm implements Constraints.ValidatableWithPayload<String> {

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
  @Override
  public String validate(Constraints.ValidationPayload payload) {
    // just a marker to show that we are in the validate method
    //RequestAttrKey.Session();

    final var username = payload.getAttrs().get(Security.USERNAME);

    if (StringUtils.isEmpty(username)) {
      Logger.error("No username found in the session.");
      return "msg.error";
    }


    final User userToUpdate = UserDao.findUserByName(username);
    if (userToUpdate == null) {
      Logger.error("No user found by the name: " + username);
      return "msg.error";
    }

    if (StringUtils.isEmpty(password) == false && StringUtils.isEmpty(rePassword) == false) {
      Logger.debug("User: " + username + " wants to change the password.");
      if (StringUtils.equals(password, rePassword) == false) {
        Logger.error(username + " did not entered matched passwords.");
        return "msg.error.passwordsNoMatch";
      }

      userToUpdate.setPassword(UserDao.cryptPassword(password));
    }

    if (StringUtils.isEmpty(defaultCopyType) == false) {
      Logger.debug(username + " sets defaultCopyType to: " + defaultCopyType);
      List<String> copyTypes = DvdInfoHelper.getCopyTypes();
      if (copyTypes.contains(defaultCopyType) == false) {
        Logger.error("User: " + username + " selected a copyType: " + defaultCopyType + " which is not configured.");
        return "The selected copytype: " + defaultCopyType + " does not exists.";
      }

      userToUpdate.setDefaultCopyType(defaultCopyType);
    }

    userToUpdate.setEmail(email);

    byte[] gravatarBytes = GravatarHelper.getGravatarBytes(userToUpdate.getEmail(), 16);
    userToUpdate.setHasGravatar(gravatarBytes != null);

    userToUpdate.save();

    // TODO: LIFT can we add it to the session here  or should we do it afterwards actually this is not a validate thing?
    //Secured.updateHasGravatar(userToUpdate.hasGravatar, request);

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
