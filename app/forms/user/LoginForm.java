package forms.user;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.i18n.Messages;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 2:21 PM
 */
public class LoginForm {
  public String username;

  public String password;

  public String validate() {
    final User user = User.authenticate(username, password);
    if (user == null) {
      return Messages.get("msg.error.login");
    }

    if(StringUtils.isEmpty(user.passwordResetToken) == false) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("User: "+username+" has a password reset token set setting it to empty.");
      }

      user.passwordResetToken = null;
      user.update();
    }
    return null;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
