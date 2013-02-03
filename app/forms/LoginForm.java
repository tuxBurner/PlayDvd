package forms;

import models.User;
import org.apache.commons.lang.StringUtils;
import play.Logger;

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
      return "Invalid user or password";
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
}
