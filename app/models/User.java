package models;

import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;
import controllers.Secured;
import org.apache.commons.codec.digest.DigestUtils;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;
import play.mvc.Controller;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Entity
public class User extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = 3236069629608718953L;

  @Id
  public Long id;

  @Required
  @Formats.NonEmpty
  @Column(unique = true)
  public String userName;

  @Required
  @Formats.NonEmpty
  public String password;

  @Required
  @Formats.NonEmpty
  public String email;

  /**
   * If set this will be taken when the user adds a new copy to his collection as default Type.
   * Like BluRay etc ...
   */
  public String defaultCopyType;

  public static Model.Finder<String, User> find = new Model.Finder<String, User>(String.class, User.class);

  /**
   * Saves the user to the database
   * 
   * @param user
   */
  public static void create(final User user) {

    try {
      user.password = User.cryptPassword(user.password);
      user.save();
    } catch (final Exception e) {
      Logger.error("An error happend while creating the new user.", e);
    }
  }

  /**
   * No plain password please in the dataBase :)
   * 
   * @param password
   * @return
   * @throws NoSuchAlgorithmException
   * @throws UnsupportedEncodingException
   */
  public static String cryptPassword(final String password) {
    final String md5Hex = DigestUtils.md5Hex(ConfigFactory.load().getString("application.secret") + password + ConfigFactory.load().getString("application.secret"));
    return md5Hex;
  }

  public static User authenticate(final String username, final String password) {
    try {
      final String cryptPassword = User.cryptPassword(password);
      return User.find.where().ieq("userName", username).eq("password", cryptPassword).findUnique();
    } catch (final Exception e) {
      Logger.error("Error while creating the password.", e);
    }

    return null;
  }

  /**
   * Checks if a {@link User} wit the given username exists
   * 
   * @param username
   * @return
   */
  public static boolean checkIfUserExsists(final String username) {
    return User.getUserByName(username) != null;
  }

  /**
   * Gets the current loggedin user from the database
   * @return
   */
  public static User getCurrentUser() {
    return getUserByName(Secured.getUsername());
  }

  /**
   * Gets a {@link User} by the given username
   * 
   * @param username
   * @return
   */
  public static User getUserByName(final String username) {
    return User.find.where().ieq("userName", username).findUnique();
  }

  /**
   * This is needed for the search form
   * 
   * @return
   */
  public static String getUserNamesAsJson() {
    final List<User> users = User.find.select("userName").orderBy("userName asc").findList();

    final List<String> result = new ArrayList<String>();

    for (final User user : users) {
      result.add(user.userName);
    }

    final Gson gson = new Gson();
    return gson.toJson(result);
  }

}
