package forms.user;

import dao.UserDao;
import helpers.DvdInfoHelper;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.data.format.Formats;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints.Validate;
import play.data.validation.ValidationError;

import java.util.Arrays;
import java.util.List;

/**
 * User: tuxburner
 * Date: 2/3/13
 * Time: 2:19 PM
 */
@Validate
public class RegisterForm implements Validatable<List<ValidationError>> {
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

  public String defaultCopyType;

  @Override
  public List<ValidationError> validate() {

    if (password.equals(repassword) == false) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY, "msg.error.passwordsNoMatch"));
    }

    // check if the username is unique
    final boolean checkIfUserExsists = UserDao.checkIfUserExsists(username);
    if (checkIfUserExsists == true) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY, "msg.error.userNameExists", List.of(username)));
    }

    // TODO: make this not in the register form it is irritating
    if (StringUtils.isEmpty(defaultCopyType) == true) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY, "No default copytype selected."));
    }
    final List<String> copyTypes = DvdInfoHelper.getCopyTypes();
    if (copyTypes.contains(defaultCopyType) == false) {
      return Arrays.asList(new ValidationError(StringUtils.EMPTY, "The selected copytype: " + defaultCopyType + " does not exists."));
    }


    final User user = new User();
    user.setEmail(email);
    user.setUserName(username);
    user.setPassword(password);
    user.setDefaultCopyType(defaultCopyType);

    UserDao.create(user);

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

  public String getDefaultCopyType() {
    return defaultCopyType;
  }

  public void setDefaultCopyType(String defaultCopyType) {
    this.defaultCopyType = defaultCopyType;
  }
}
