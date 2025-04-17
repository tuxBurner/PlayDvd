package dao;

import com.google.gson.Gson;
import com.typesafe.config.ConfigFactory;
import controllers.Secured;
import models.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.mvc.Http;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserDao {

  /**
   * Saves the user to the database
   *
   * @param user
   */
  public static void create(final User user) {

    try {
      user.password = UserDao.cryptPassword(user.password);
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
  // TODO: LIFT remove md5 and make all passwords empty for reset
  public static String cryptPassword(final String password) {
    final String md5Hex = DigestUtils.md5Hex(ConfigFactory.load().getString("play.http.secret.key") + password + ConfigFactory.load().getString("play.http.secret.key"));
    return md5Hex;
  }

  public static User authenticate(final String username, final String password) {
    try {
      final String cryptPassword = UserDao.cryptPassword(password);
      return User.FINDER.query()
          .where()
          .ieq("userName", username)
          .eq("password", cryptPassword)
          .findOne();
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
    return UserDao.findUserByName(username) != null;
  }

  /**
   * Gets the current loggedin user from the database
   *
   * @return
   */
  public static User findCurrentUser(final Http.Request request) {
    return findUserByName(Secured.getUsernameStatic(request));
  }

  /**
   * Gets a {@link User} by the given username
   *
   * @param username
   * @return
   */
  public static User findUserByName(final String username) {
    return User.FINDER.query()
        .where()
        .ieq("userName", username)
        .findOne();
  }

  /**
   * Gets a {@link User} by the given passwordResetToken
   *
   * @param passwordResetToken
   * @return
   */
  public static User findUserByResetToken(final String passwordResetToken) {
    return User.FINDER.query()
        .where()
        .ieq("passwordResetToken", passwordResetToken)
        .findOne();
  }

  /**
   * This is needed for the search form
   *
   * @return
   */
  public static String loadUserNamesAsJson() {
    final List<User> users = User.FINDER.query()
        .select("userName")
        .orderBy("userName asc")
        .findList();

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
  public static List<String> loadOtherUserNames(final Http.Request request) {
    final List<User> findList = User.FINDER.query()
        .select("userName")
        .where()
        .ne("userName", Secured.getUsernameStatic(request))
        .orderBy("userName asc")
        .findList();

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
  public static User findUserByRssAuthKey(final String rssAuthKey) {
    if(StringUtils.isEmpty(rssAuthKey) == true) {
      return null;
    }

    return User.FINDER.query()
        .where()
        .eq("rssAuthKey",rssAuthKey)
        .findOne();
  }

  /**
   * Creates a rss auth key for the current user
   * @return
   */
  public static User createUserRssAuthKey(final User currentUser) {
    if(currentUser == null) {
      return null;
    }

    if(StringUtils.isEmpty(currentUser.rssAuthKey) == true) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("No rssAuthKey found for the user. Generating a new one");
      }

      final String key = UUID.randomUUID().toString();
      //currentUser.rssAuthKey = key;
      currentUser.setRssAuthKey(key);
      currentUser.update();
    }

    return currentUser;
  }

}
