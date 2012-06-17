package models;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import org.apache.commons.codec.digest.DigestUtils;

import com.typesafe.config.ConfigFactory;

import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

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
      return User.find.where().eq("userName", username).eq("password", cryptPassword).findUnique();
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
   * Gets a {@link User} by the given username
   * 
   * @param username
   * @return
   */
  public static User getUserByName(final String username) {
    return User.find.where().eq("userName", username).findUnique();
  }

}
