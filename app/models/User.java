package models;

import io.ebean.Finder;
import io.ebean.Model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import play.data.format.Formats;
import play.data.validation.Constraints.Required;

@Entity
@Table(name = "user")
public class User extends Model {

  /**
   * The Finder
   */
  public static final Finder<Long, User> FINDER = new Finder<>(User.class);

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


  public void setRssAuthKey(String rssAuthKey) {
    this.rssAuthKey = rssAuthKey;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public void setHasGravatar(boolean hasGravatar) {
    this.hasGravatar = hasGravatar;
  }

  public void setDefaultCopyType(String defaultCopyType) {
    this.defaultCopyType = defaultCopyType;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }
}
