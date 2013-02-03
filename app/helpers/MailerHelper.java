package helpers;

import com.typesafe.config.ConfigFactory;
import com.typesafe.plugin.MailerAPI;
import com.typesafe.plugin.MailerPlugin;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.validation.Constraints;

/**
 * Simple helper for sending mails
 * configure it in the mail config block in the application.config
 *
 * User: tuxburner
 * Date: 2/3/13
 * Time: 10:49 PM
 */
public class MailerHelper {

  /**
   * Sends an email
   * @param subject
   * @param receiver
   * @param content
   * @param htmlContent
   */
  public static void sendMail(final String subject, final String receiver, final String content, final boolean htmlContent) {

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


    MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
    mail.setSubject(subject);
    mail.addRecipient(receiver);
    mail.addFrom(mailFrom);
    if(htmlContent == true) {
      mail.sendHtml(content);
    } else {
      mail.send(content);
    }
  }

  /**
   * Checks if the mailer is active
   * @return
   */
  public static boolean mailerActive() {
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
