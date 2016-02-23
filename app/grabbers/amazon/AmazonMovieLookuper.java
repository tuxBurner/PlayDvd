package grabbers.amazon;


import com.typesafe.config.ConfigFactory;
import helpers.ConfigurationHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import play.Logger;
import play.libs.XPath;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.*;

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
    copyTypeMatches = ConfigurationHelper.createValMap("dvdb.amazon.grabber.matchCopyType");
    ageRatingMatches = ConfigurationHelper.createValMap("dvdb.amazon.grabber.matchAgeRating");
  }

  /**
   * Checks the code what type it is and calls the correct method
   * @param code
   * @return
   */
  public static AmazonResult lookUp(String code) {
    if(StringUtils.isEmpty(code) == true) {
      return null;
    }


    if(StringUtils.isNumeric(code) == true && code.length() == 13) {
      return lookUpByEanNR(code);
    } else {
      return lookupByAsin(code);
    }
  }

  /**
   * Looks up a dvd/bluray by its name
   * @param name
   * @return
     */
  public static List<AmazonResult> findByName(final String name) {

          /*http://ecs.amazonaws.de/onca/xml?Service=AWSECommerceService
      &Version=2011-08-01
              &AssociateTag=aztag-20
              &Operation=ItemSearch
              &SearchIndex=DVD
              &ProductGroup=Blu-ray
              &Keywords=harry+potter*/

    final Map<String,String> params = new HashMap<>();
    params.put("Operation","ItemSearch");
    params.put("SearchIndex","DVD");
    params.put("Keywords",name);

    List<AmazonResult> search = search(params);

    return search;
  }


  /**
   * Searches the AmazonWs
   * @param type
   * @param codeNr
   * @return
   */
  public static AmazonResult lookUp(final EAmazonCodeType type, final String codeNr) {
    if(EAmazonCodeType.EAN.equals(type)) {
      return lookUpByEanNR(codeNr);
    }

    if(EAmazonCodeType.ASIN.equals(type)) {
      return lookupByAsin(codeNr);
    }

    return null;
  }

  /**
   * Lookup a movie via amazon ws and the asinnr
   * @param asinNr
   * @return
   */
  private static AmazonResult lookupByAsin(final String asinNr) {
    return searchById(asinNr,null);
  }


  /**
   * Lookup a movie via amazon ws and the ean nr
   * @param eanNr
   * @return
   */
  private static AmazonResult lookUpByEanNR(final String eanNr) {
    final Map<String,String> params = new HashMap<>();
    params.put("IdType", "EAN");
    params.put("SearchIndex","DVD");
    return searchById(eanNr, params);
  }

  /**
   * Checks if there is only one entry in the list and if so returns this one otherwise null.
   * @param results the results to check on
   * @return null or the result.
     */
  private static AmazonResult checkAndReturnSingleRes(final List<AmazonResult> results) {
    if(CollectionUtils.isEmpty(results) == true) {
      return null;
    }

    if(results.size() == 1) {
      return results.get(0);
    }

    return null;
  }

  /**
   * Searches the amazon ws by id.
   * @param id  the id ean/asin
   * @param params additional parameters to pass to the search
   * @return the result
     */
  private static AmazonResult searchById(final String id, Map<String,String> params) {

    if(params == null) {
      params = new HashedMap();
    }

    params.put("Operation", "ItemLookup");
    params.put("ItemId", id);

    List<AmazonResult> result = search(params);
    return checkAndReturnSingleRes(result);
  }

  /**
   * Searches the amazon ws for a movie by the given id.
   * @param params
   * @return
   */
  private static List<AmazonResult> search(Map<String,String> params) {

    String awsEndPoint = ConfigFactory.load().getString("dvddb.amazon.endPoint");
    if(StringUtils.isEmpty(awsEndPoint) == true) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No AWS endPoint set in the configuration.");
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
        params = new HashMap<>();
      }

      params.put("Service", "AWSECommerceService");
      params.put("Version", "2011-08-02");
      params.put("ResponseGroup", "ItemAttributes");
      params.put("AssociateTag", "aztag-20");


      String requestUrl = helper.sign(params);
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Signed AWS request: "+requestUrl);
      }

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(requestUrl);



      final List<AmazonResult> result = new ArrayList<>();
      NodeList itemNodes = XPath.selectNodes("//Items/Item", doc);
      for(int i=0; i < itemNodes.getLength(); i++) {
        Node item = itemNodes.item(i);
        final AmazonResult resultFromNode = getResultFromNode(item);
        if(resultFromNode != null) {
          result.add(resultFromNode);
        }
      }

      return result;

    } catch (Exception e) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("An error happend while looking in the aws.",e);
      }
      return null;
    }
  }

  /**
   * Reads the data from the given node and generates an AmazonResult from it.
   * @param nodeObj
   * @return
     */
  private static AmazonResult getResultFromNode(final Object nodeObj) {

    Node itemAttrNode = XPath.selectNode("./ItemAttributes", nodeObj);

    String title = getNodeContent(itemAttrNode,"./Title");

    String copyType = getNodeContent(itemAttrNode,"./Binding");
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

    String asin =  getNodeContent(nodeObj,"./ASIN");
    String ean = getNodeContent(itemAttrNode,"./EAN");
    String rating = getNodeContent(itemAttrNode, "./AudienceRating");
    if(ageRatingMatches.containsKey(rating) == false) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No agerating matching configured for amazon rating: "+rating);
      }
    } else {
      rating = ageRatingMatches.get(rating);
    }


    final Set<String> audioTypes = new HashSet<String>();

    final NodeList audioTypeNodes = XPath.selectNodes("./Languages/Language[AudioFormat and Type = 'Original']", itemAttrNode);
    for(int i = 0; i < audioTypeNodes.getLength(); i++) {
      Node langItem = audioTypeNodes.item(i);
      final Node nameNode = XPath.selectNode("./Name", langItem);
      final Node formatNode = XPath.selectNode("./AudioFormat", langItem);
      String audioType = StringUtils.EMPTY;
      if(nameNode != null && StringUtils.isEmpty(nameNode.getTextContent()) == false) {
        audioType=nameNode.getTextContent();
      }

      if(formatNode != null && StringUtils.isEmpty(formatNode.getTextContent()) == false) {
        if(StringUtils.isEmpty(audioType) == false) {
          audioType+=" - ";
        }
        audioType+=formatNode.getTextContent();
      }

      audioTypes.add(audioType);
    }

    final NodeList otherLanguageNodes = XPath.selectNodes("./Languages/Language[Type = 'Published']/Name", itemAttrNode);
    for(int i = 0; i < otherLanguageNodes.getLength(); i++) {
      String language = otherLanguageNodes.item(i).getTextContent();

      // check if the language is already known :)
      boolean alreadyKnown = false;

      for(final String audioType :  audioTypes) {
        if(StringUtils.startsWithIgnoreCase(audioType,language) == true) {
          alreadyKnown = true;
          break;
        }
      }

      if(alreadyKnown == false) {
        audioTypes.add(language);
      }
    }



    final AmazonResult result = new AmazonResult(title,rating,copyType,asin,ean,audioTypes);

    return result;
  }

  /**
   * Gets the string content of a node via xpath
   * @param nodeObj
   * @param xpath
   * @return
   */
  private static String getNodeContent(final Object nodeObj, final String xpath) {
    Node node = XPath.selectNode(xpath, nodeObj);
    if(node == null) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("Could not find node by xpath: " + xpath+ " in node: "+nodeObj.toString());
      }
      return StringUtils.EMPTY;
    }

    return StringUtils.trimToEmpty(node.getTextContent());

  }


}
