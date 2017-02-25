package forms.user;

import helpers.DvdInfoHelper;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;

import java.util.Arrays;
import java.util.List;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 2:19 PM
 */
public class RegisterForm {
  @Formats.NonEmpty
  @Constraints.Required(message = "msg.error.noUsername")
  @Constraints.MaxLength(value = 10)
  @Constraints.MinLength(value = 5)
  public String username;

  @Formats.NonEmpty
  @Constraints.Required(message = "msg.error.noPassword")
  @Constraints.MinLength(value = 5)
  public String password;

  public String repassword;

  @Constraints.Required(message = "msg.error.noEmail")
  @Constraints.Email(message = "msg.error.invalidEmail")
  public String email;



  public List<ValidationError> validate() {

    if (password.equals(repassword) == false) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY,"msg.error.passwordsNoMatch"));
    }

    // check if the username is unique
    final boolean checkIfUserExsists = User.checkIfUserExsists(username);
    if (checkIfUserExsists == true) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY,"msg.error.userNameExists"));
    }


    final User user = new User();
    user.email = email;
    user.userName = username;
    user.password = password;
    user.defaultCopyType = DvdInfoHelper.getCopyTypes().get(0);

    User.create(user);

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

  public String getRepassword() {
    return repassword;
  }

  public void setRepassword(String repassword) {
    this.repassword = repassword;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }
}
