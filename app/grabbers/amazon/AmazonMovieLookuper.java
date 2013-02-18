package grabbers.amazon;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import play.Logger;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * User: tuxburner
 * Date: 2/11/13
 * Time: 5:48 PM
 */
public class AmazonMovieLookuper {

  private static List<String> removeFromTitleList  = ConfigFactory.load().getStringList("dvdb.amazon.grabber.removeFromTitle");

  private static Map<String,String> copyTypeMatches = new HashMap<String, String>();

  private static Map<String,String> ageRatingMatches  = new HashMap<String, String>();

  static {
    for (Map.Entry<String, Object> stringObjectEntry : ConfigFactory.load().getObject("dvdb.amazon.grabber.matchCopyType").unwrapped().entrySet()) {
     copyTypeMatches.put(stringObjectEntry.getKey(), (String) stringObjectEntry.getValue());
    }

    for (Map.Entry<String, Object> stringObjectEntry : ConfigFactory.load().getObject("dvdb.amazon.grabber.matchAgeRating").unwrapped().entrySet()) {
      ageRatingMatches.put(stringObjectEntry.getKey(), (String) stringObjectEntry.getValue());
    }



  }

  /**
   * Looksup a movie via amaton ws
   * @param eanNr
   * @return
   */
  public static AmazonResult lookUpByEanNR(final String eanNr) {
    String awsEndPoint = ConfigFactory.load().getString("dvddb.amazon.endpoint");
    if(StringUtils.isEmpty(awsEndPoint) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS endpoint set in the configuration.");
      }
      return null;
    }
    String awsKeyId = ConfigFactory.load().getString("dvddb.amazon.aws.keyid");
    if(StringUtils.isEmpty(awsKeyId) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS keyID set in the configuration.");
      }
      return null;
    }
    String awsSecretKey = ConfigFactory.load().getString("dvddb.amazon.aws.secretkey");
    if(StringUtils.isEmpty(awsSecretKey) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS keySecretKey set in the configuration.");
      }
      return null;
    }


    try {
      SignedRequestsHelper helper = SignedRequestsHelper.getInstance(awsEndPoint, awsKeyId, awsSecretKey);


      Map<String, String> params = new HashMap<String, String>();
      params.put("Service", "AWSECommerceService");
      params.put("Version", "2011-08-02");
      params.put("Operation", "ItemLookup");
      params.put("ItemId", eanNr);
      params.put("ResponseGroup", "ItemAttributes");
      params.put("AssociateTag", "aztag-20");

      // EAN SPECIFIC
      params.put("IdType", "EAN");
      params.put("SearchIndex","DVD");




      String requestUrl = helper.sign(params);
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Signed AWS request: "+requestUrl);
      }


      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(requestUrl);

      String title = XPathAPI.selectSingleNode(doc, "//Title").getTextContent();

      String copyType = XPathAPI.selectSingleNode(doc, "//Binding").getTextContent().trim();
      if(copyTypeMatches.containsKey(copyType) == false) {
        if(Logger.isErrorEnabled() == true) {
          Logger.error("No copytype matching configured for amazon copytype: "+copyType);
        }
        return null;
      }
      copyType  = copyTypeMatches.get(copyType);
      if(copyType.equals("BLURAY") == true && StringUtils.contains(title,"[Blu-ray 3D]") == true) {
        copyType = "BLURAY3D";
      }




      title = StringUtils.substringBefore(title,"(");

      if(CollectionUtils.isEmpty(removeFromTitleList) == false) {
        for(String removeFromTitle : removeFromTitleList) {
          title = StringUtils.remove(title,removeFromTitle);
        }
      }
      title = StringUtils.trim(title);


      String asin = XPathAPI.selectSingleNode(doc, "//ASIN").getTextContent().trim();
      String rating = XPathAPI.selectSingleNode(doc, "//AudienceRating").getTextContent().trim();
      if(ageRatingMatches.containsKey(rating) == false) {
        if(Logger.isErrorEnabled() == true) {
         Logger.error("No agerating matching configured for amazon rating: "+rating);
        }
        return null;
      }
      rating  = ageRatingMatches.get(rating);



      /*NodeList nodeList = XPathAPI.selectNodeList(doc, "//Languages/Language[AudioFormat]");
      for(int i = 0; i < nodeList.getLength(); i++) {
        Node langItem = nodeList.item(i);
        Logger.debug(langItem.getTextContent());
      } */


      final AmazonResult result = new AmazonResult(title,rating,copyType,asin);

      return result;



      // TODO: do this in the play way to fetch the data from the signed url
      /*Document document = WS.url(requestUrl).get().map(new F.Function<WS.Response, Document>() {
        @Override
        public Document apply(WS.Response response) throws Throwable {
          return response.asXml();
        }
      }).get();*/




    } catch (Exception e) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("An error happend while looking in the aws.",e);
      }
      return null;
    }



  }

}
