package forms;

import play.data.validation.Constraints;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 11:26 PM
 */
public class LostPasswordForm {

  @Constraints.Required(message = "Email is required")
  @Constraints.Email(message = "The entered Email is not an email")
  public String email;

  /**
   * Checks if the user entered a valid email which is stored in the database
   *
   * @return
   */
  public String validate() {


    return  null;
  }

}
