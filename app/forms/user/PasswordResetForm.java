package forms.user;

import play.data.validation.Constraints;
import play.data.validation.Constraints.MinLength;

public class PasswordResetForm {

  @Constraints.Required(message = "msg.error.noPassword")
  @MinLength(value = 5)
  public String password;

  public String rePassword;


  /**
   * Checks if the user change the password
   * 
   * @return
   */
  public String validate() {
    if (password.equals(rePassword) == false) {
      return "msg.error.passwordsNoMatch";
    }
    return null;
  }
}
