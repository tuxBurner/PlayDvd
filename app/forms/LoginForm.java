package forms;

import models.User;

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
    return null;
  }
}
