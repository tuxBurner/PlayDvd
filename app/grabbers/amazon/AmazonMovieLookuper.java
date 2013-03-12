package grabbers.amazon;


import com.typesafe.config.ConfigFactory;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import play.Logger;
import play.libs.XPath;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   * Looksup a movie via amazon ws and the asinnr
   * @param asinNr
   * @return
   */
  public static AmazonResult lookupByAsin(final String asinNr) {
    return searchByID(asinNr,null);
  }


  /**
   * Looksup a movie via amazon ws and the ean nr
   * @param eanNr
   * @return
   */
  public static AmazonResult lookUpByEanNR(final String eanNr) {
    final Map<String,String> params = new HashMap<String, String>();
    params.put("IdType", "EAN");
    return searchByID(eanNr,params);
  }

  /**
   * Searches the amazon ws for a movie by the given id.
   * @param id
   * @param params
   * @return
   */
  private static AmazonResult searchByID(final String id,Map<String,String> params) {
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

      if(params == null) {
        params = new HashMap<String, String>();
      }

      params.put("Service", "AWSECommerceService");
      params.put("Version", "2011-08-02");
      params.put("Operation", "ItemLookup");
      params.put("ItemId", id);
      params.put("ResponseGroup", "ItemAttributes");
      params.put("AssociateTag", "aztag-20");
      params.put("SearchIndex","DVD");




      String requestUrl = helper.sign(params);
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Signed AWS request: "+requestUrl);
      }


      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(requestUrl);


      String title = getNodeContent(doc,"//Title");

      String copyType = getNodeContent(doc,"//Binding");
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

      String asin =  getNodeContent(doc,"//ASIN");
      String rating = getNodeContent(doc, "//AudienceRating");
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

  /**
   * Gets the string content of a node via xpath
   * @param doc
   * @param xpath
   * @return
   */
  private static String getNodeContent(final Document doc, final String xpath) {
    Node node = XPath.selectNode(xpath, doc);
    if(node == null) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Could not find node by xpath: " + xpath+ " in document: "+doc.toString());
      }
      return StringUtils.EMPTY;
    }

    return StringUtils.trimToEmpty(node.getTextContent());

  }

}
