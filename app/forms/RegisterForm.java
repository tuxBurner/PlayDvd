package forms;

import models.User;
import play.data.format.Formats;
import play.data.validation.Constraints;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 2:19 PM
 */
public class RegisterForm {
  @Formats.NonEmpty
  @Constraints.Required(message = "Username is needed")
  @Constraints.MaxLength(value = 10)
  @Constraints.MinLength(value = 5)
  public String username;

  @Formats.NonEmpty
  @Constraints.Required(message = "Password is needed")
  @Constraints.MaxLength(value = 10)
  @Constraints.MinLength(value = 5)
  public String password;

  public String repassword;

  @Constraints.Required(message = "Email is required")
  @Constraints.Email(message = "The entered Email is not an email")
  public String email;

  public String validate() {

    if (password.equals(repassword) == false) {
      return "Passwords dont match";
    }

    // check if the username is unique
    final boolean checkIfUserExsists = User.checkIfUserExsists(username);
    if (checkIfUserExsists == true) {
      return "User: " + username + " already exists";
    }

    final User user = new User();
    user.email = email;
    user.userName = username;
    user.password = password;
    User.create(user);

    return null;
  }
}
