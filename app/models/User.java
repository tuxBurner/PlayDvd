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
  private Long id;

  @Required
  @Formats.NonEmpty
  @Column(unique = true)
  private String userName;

  @Required
  @Formats.NonEmpty
  private String password;

  @Required
  @Formats.NonEmpty
  private String email;


  /**
   * If true this means the user has a gravatar url to display.
   */
  private boolean hasGravatar;

  /**
   * If set this will be taken when the user adds a new copy to his collection as defaultold Type.
   * Like BluRay etc ...
   */
  private String defaultCopyType;

  /**
   * If this token is set the user asked for a password reset
   */
  private String passwordResetToken;

  /**
   * If generated the user can use this token to access a rss feed
   */
  private String rssAuthKey;


  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public boolean isHasGravatar() {
    return hasGravatar;
  }

  public void setHasGravatar(boolean hasGravatar) {
    this.hasGravatar = hasGravatar;
  }

  public String getDefaultCopyType() {
    return defaultCopyType;
  }

  public void setDefaultCopyType(String defaultCopyType) {
    this.defaultCopyType = defaultCopyType;
  }

  public String getPasswordResetToken() {
    return passwordResetToken;
  }

  public void setPasswordResetToken(String passwordResetToken) {
    this.passwordResetToken = passwordResetToken;
  }

  public String getRssAuthKey() {
    return rssAuthKey;
  }

  public void setRssAuthKey(String rssAuthKey) {
    this.rssAuthKey = rssAuthKey;
  }
}
