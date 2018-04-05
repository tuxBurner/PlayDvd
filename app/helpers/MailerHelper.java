package helpers;

import com.google.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Constraints;
import play.libs.mailer.Email;
import play.libs.mailer.MailerClient;

import javax.inject.Inject;


/**
 * Simple helper for sending mails.
 * configure it in the mail config block in the application.config
 * See conf/email.conf
 *
 */
@Singleton
public class MailerHelper {


  /**
   * The mailer client to use in this Application
   */
  final MailerClient mailerClient;

  @Inject
  public MailerHelper(final MailerClient mailerClient) {
    this.mailerClient = mailerClient;
  }


  /**
   * Sends an email
   * @param subject
   * @param receiver
   * @param content
   * @param htmlContent
   */
  public void sendMail(final String subject, final String receiver, final String content, final boolean htmlContent) {

    if(mailerActive() == false) {
      return;
    }

    if(StringUtils.isEmpty(content) == true) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Cannot send mail no content is given.");
      }
      return;
    }

    if(StringUtils.isEmpty(subject) == true) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Cannot send mail no subject is given.");
      }
      return;
    }

    if(StringUtils.isEmpty(receiver) == true) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Cannot send mail no receiver is given.");
      }
      return;
    }

    Constraints.EmailValidator validator = new Constraints.EmailValidator();
    boolean receiverMailValid = validator.isValid(receiver);
    if(receiverMailValid == false) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Cannot send mail: "+receiver+" is not a valid email.");
      }
      return;
    }



    // check the from configuration
    String mailFrom = ConfigFactory.load().getString("dvddb.mailer.from");
    if(StringUtils.isEmpty(mailFrom) == true) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Cannot send mail because dvddb.mailer.from is not set in the configuration");
      }
      return;
    }

    Email email = new Email()
        .setSubject(subject)
        .addTo(receiver)
        .setFrom(mailFrom);

    if(htmlContent == true) {
      email.setBodyHtml(content);
    } else {
      email.setBodyText(content);
    }
    mailerClient.send(email);
  }

  /**
   * Checks if the mailer is active
   * @return true when the mailer is active or false when not.
   */
  public  boolean mailerActive() {
    // check the configs
    boolean mailerActive = ConfigFactory.load().getBoolean("dvddb.mailer.active");
    if(mailerActive == false) {
      if(Logger.isInfoEnabled() == true) {
        Logger.info("Mailer is not set to true via dvddb.mailer.active.");
      }
    }
    return mailerActive;
  }
}
