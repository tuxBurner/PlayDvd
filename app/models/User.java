package models;

import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;
import controllers.Secured;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="user")
public class User extends Model {


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
   * If true this means the user has a gravatar url to display.
   */
  public boolean hasGravatar;

  /**
   * If set this will be taken when the user adds a new copy to his collection as defaultold Type.
   * Like BluRay etc ...
   */
  public String defaultCopyType;

  /**
   * If this token is set the user asked for a password reset
   */
  public String passwordResetToken;

  /**
   * If generated the user can use this token to access a rss feed
   */
  public String rssAuthKey;

  /**
   * The Finder
   */
  public static Find<Long, User> FINDER = new Find<Long,User>(){};

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
    final String md5Hex = DigestUtils.md5Hex(ConfigFactory.load().getString("play.crypto.secret") + password + ConfigFactory.load().getString("play.crypto.secret"));
    return md5Hex;
  }

  public static User authenticate(final String username, final String password) {
    try {
      final String cryptPassword = User.cryptPassword(password);
      return User.FINDER.where().ieq("userName", username).eq("password", cryptPassword).findUnique();
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
   *
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
    return User.FINDER.where().ieq("userName", username).findUnique();
  }

  /**
   * Gets a {@link User} by the given passwordResetToken
   *
   * @param passwordResetToken
   * @return
   */
  public static User getUserByResetToken(final String passwordResetToken) {
    return User.FINDER.where().ieq("passwordResetToken", passwordResetToken).findUnique();
  }

  /**
   * This is needed for the search form
   *
   * @return
   */
  public static String getUserNamesAsJson() {
    final List<User> users = User.FINDER.select("userName").orderBy("userName asc").findList();

    final List<String> result = new ArrayList<String>();

    for (final User user : users) {
      result.add(user.userName);
    }

    final Gson gson = new Gson();
    return gson.toJson(result);
  }

  /**
   * Gets all other usernames
   *
   * @return
   */
  public static List<String> getOtherUserNames() {
    final List<User> findList = User.FINDER.select("userName").where().ne("userName", Secured.getUsername()).orderBy("userName asc").findList();

    List<String> list = null;
    if (CollectionUtils.isEmpty(findList) == false) {
      list = new ArrayList<String>();
      list.add("");
      for (final User user : findList) {
        list.add(user.userName);
      }
    }
    return list;
  }

  /**
   * Gets a {@link User} by the rss auth key
   * @param rssAuthKey
   * @return
   */
  public static User getUserByRssAuthKey(final String rssAuthKey) {
    if(StringUtils.isEmpty(rssAuthKey) == true) {
      return null;
    }

    return User.FINDER.where().eq("rssAuthKey",rssAuthKey).findUnique();
  }

  /**
   * Creates a rss auth key for the current user
   * @return
   */
  public static String createUserRssAuthKey() {
    final User currentUser = getCurrentUser();
    if(currentUser == null) {
      return null;
    }
    if(StringUtils.isEmpty(currentUser.rssAuthKey) == true) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("No rssAuthKey found for the user. Generating a new one");
      }
      final String key = UUID.randomUUID().toString();
      currentUser.rssAuthKey = key;
      currentUser.update();
    }

    return currentUser.rssAuthKey;
  }

}
