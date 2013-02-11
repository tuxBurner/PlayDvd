package grabbers.amazon;

import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * User: tuxburner
 * Date: 2/11/13
 * Time: 5:48 PM
 */
public class AmazonMovieLookuper {

  public static void lookUpByEanNR(final String eanNr) {
    String awsEndPoint = ConfigFactory.load().getString("dvddb.amazon.endpoint");
    if(StringUtils.isEmpty(awsEndPoint) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS endpoint set in the configuration.");
      }
      return;
    }
    String awsKeyId = ConfigFactory.load().getString("dvddb.amazon.aws.keyid");
    if(StringUtils.isEmpty(awsKeyId) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS keyID set in the configuration.");
      }
      return;
    }
    String awsSecretKey = ConfigFactory.load().getString("dvddb.amazon.aws.secretkey");
    if(StringUtils.isEmpty(awsSecretKey) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS keySecretKey set in the configuration.");
      }
      return;
    }


    try {
      SignedRequestsHelper helper = SignedRequestsHelper.getInstance(awsEndPoint, awsKeyId, awsSecretKey);


      Map<String, String> params = new HashMap<String, String>();
      params.put("Service", "AWSECommerceService");
      params.put("Version", "2011-08-02");
      params.put("Operation", "ItemLookup");
      params.put("ItemId", "5050582277647");
      params.put("ResponseGroup", "ItemAttributes");
      params.put("AssociateTag", "aztag-20");

      // EAN SPECIFIC
      params.put("IdType", "EAN");
      params.put("SearchIndex","DVD");


      String requestUrl = helper.sign(params);
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Signed AWS request: "+requestUrl);
      }

    } catch (Exception e) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("An error happend while looking in the aws.");
      }
      return;
    }



  }

}
