package forms.user;

import play.data.validation.Constraints;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 11:26 PM
 */
public class LostPasswordForm {

  @Constraints.Required(message = "msg.error.noUsername")
  public String username;

}
