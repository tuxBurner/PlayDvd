package grabbers.amazonwebcrawler;

import com.typesafe.config.ConfigFactory;
import grabbers.HttpBrowserHelper;
import helpers.ConfigurationHelper;
import jodd.jerry.Jerry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

import java.util.*;

/**
 * Crawls the amazon web page.
 */
public class AmazonMovieWebCrawler {


  /**
   * Web endpoint to look on
   */
  private final static String AMAZON_ENDPOINT_URL;


  /**
   * Category to search in
   */
  private final static String AMAZON_CATEGORY;


  /**
   * Map which contains the amazon copy type string -> internal copytype
   */
  private final static Map<String, String> AMAZON_COPY_TYPE_MAP = ConfigurationHelper
    .createValMap("dvdb.amazon.grabber.matchCopyType");

  /**
   * Map which contauns the amazon age rating -> internal age rating
   */
  private final static Map<String, String> AMAZON_AGE_RATING_MAP = ConfigurationHelper
    .createValMap("dvdb.amazon.grabber.matchAgeRating");

  /**
   * List of strings which are to remove from the title
   */
  private static List<String> AMAZON_REMOVE_FROM_TITLE = ConfigFactory.load()
    .getStringList("dvdb.amazon.grabber.removeFromTitle");

  static {
    AMAZON_ENDPOINT_URL = ConfigFactory.load().getString("dvddb.amazon.webEndPoint");
    if (StringUtils.isEmpty(AMAZON_ENDPOINT_URL) == true) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No amazon web endPoint set in the configuration.");
      }
    }

    AMAZON_CATEGORY = ConfigFactory.load().getString("dvddb.amazon.webCategory");
    if (StringUtils.isEmpty(AMAZON_CATEGORY) == true) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No amazon web cytegory set in the configuration.");
      }
    }
  }


  /**
   * Finds dvd/bluray by name
   *
   * @param name the name of the dvd/bluray to look for
   * @return a list of {@link AmazonResult}
   */
  public static List<AmazonResult> findByName(final String name) {

    Logger.info("Searching for movie: " + name + " on amazon");

    final String url = AMAZON_ENDPOINT_URL + "/s";
    final Map<String, String> params = new HashMap<>();
    params.put("k", name);
    params.put("i", AMAZON_CATEGORY);
    final Jerry urlAsJerryDoc = HttpBrowserHelper.getUrlAsJerryDoc(url, params);

    final List<AmazonResult> amazonResults = extractResultsFromSearchPage(urlAsJerryDoc);

    return amazonResults;
  }

  /**
   * Extracts the results from the search page
   *
   * @param document the html content of the search page
   * @return the results
   */
  private static List<AmazonResult> extractResultsFromSearchPage(final Jerry document) {

    final List<AmazonResult> result = new ArrayList<>();

    final Jerry children = document.$("[data-component-type='s-search-results'] .s-result-list").children();

    for (final Jerry copyInfosChild : children) {
      final List<AmazonResult> amazonResults = parseInfosFromSearchHtmlPart(copyInfosChild);
      result.addAll(amazonResults);
    }

    return result;
  }

  /**
   * Checks the code what type it is and calls the correct method
   *
   * @param code the code as a String
   * @return the result
   */
  public static Optional<AmazonResult> lookUpByCode(final String code) {
    if (StringUtils.isEmpty(code) == true) {
      return null;
    }

    if (StringUtils.isNumeric(code) == true && code.length() == 13) {
      return lookupByEanCode(code);
    } else {
      return lookupByAsin(code);
    }
  }

  /**
   * Retrieves the copy informations from the find page
   *
   * @param copyInfosChild the jerry html object which represents one copy entrance
   * @return list of results
   */
  private static List<AmazonResult> parseInfosFromSearchHtmlPart(final Jerry copyInfosChild) {

    final List<AmazonResult> result = new ArrayList<>();

    final String title = cleanTitle(copyInfosChild.$("h2 span").text());

    if (StringUtils
      .contains(copyInfosChild.html(), "<span class=\"a-size-base a-color-secondary\">Gesponsert</span>")) {
      Logger.info("Skipping: " + title + " for search it is an affiliated content.");
      return result;
    }

    final String asin = copyInfosChild.attr("data-asin");
    final String imageUrl = copyInfosChild.$("[data-component-type='s-product-image'] img").attr("src");

    final String rating = extractRating("a-icon-star-small", copyInfosChild);

    // Get all the types like dvd and bluray ...
    final Jerry typeLinks = copyInfosChild.$("a.a-text-bold");
    for (final Jerry typeLink : typeLinks) {
      final String copyType = getCopyTypeFromAmazonText(typeLink.text());

      result.add(new AmazonResult(title, rating, "", copyType, asin, "", new HashSet<>(), imageUrl));
    }

    return result;
  }


  /**
   * Looks up if we can find the eannr on bookbuttler.de and if we see the asin for it
   *
   * @param eanNr the eannr
   * @return the result
   */
  private static Optional<AmazonResult> lookupByEanCode(final String eanNr) {
    final Jerry doc = searchBookButtler(eanNr);
    final String asinNr = doc.$("td[asin]").attr("asin");

    Logger.info("Found asin: " + asinNr + " for ean: " + eanNr + " on bookbuttler");

    if (StringUtils.isBlank(asinNr)) {
      return Optional.empty();
    }

    final Optional<AmazonResult> amazonResult = lookupByAsin(asinNr);
    if (amazonResult.isPresent()) {
      amazonResult.get().setEan(eanNr);
    }

    return amazonResult;
  }

  /**
   * Converts the given asin nr to an ean nr by searching on bookbuttler
   *
   * @param asinNr the asinr to look for
   * @return "" when nothing found or the ean nr
   */
  private static String asinToEanNr(final String asinNr) {
    final Jerry doc = searchBookButtler(asinNr);
    final String eanNr = doc.$("img[alt^='EAN']").attr("alt").replace("EAN ", "");
    Logger.info("Found ean: " + eanNr + " for asin: " + asinNr + " on bookbuttler");
    return eanNr;
  }

  /**
   * Searches bookButtler for the given code and returns the html response
   *
   * @param code the code to search for ean or asin
   * @return the doc as Jerry
   */
  private static Jerry searchBookButtler(final String code) {
    final String url = "http://www.bookbutler.de/movie/search";
    Logger.info("Searching for code: " + code + " on bookbuttler");
    final Jerry doc = HttpBrowserHelper.getUrlAsJerryDoc(url, "keyword", code);
    return doc;
  }

  /**
   * Looks up the {@link AmazonResult} by the given asin nr
   *
   * @param asinNr the asinNr
   * @return the {@link AmazonResult}
   */
  private static Optional<AmazonResult> lookupByAsin(final String asinNr) {
    final String url = AMAZON_ENDPOINT_URL + "/dp/" + asinNr;

    final Jerry doc = HttpBrowserHelper.getUrlAsJerryDoc(url);

    final String title = cleanTitle(doc.$("#dp-container #title").text());
    final String rating = extractRating("a-icon-star", doc.$("#dp-container"));

    final String typeString = doc.$("#dp-container #bylineInfo_feature_div span:contains('Format: ')").next().text();
    final String copyType = getCopyTypeFromAmazonText(typeString);

    final String amazonAgeRating = doc.$("#dp-container #bylineInfo_feature_div span:contains('Alterseinstufung: ')")
      .next().text();
    String ageRating = "";
    if (AMAZON_AGE_RATING_MAP.containsKey(amazonAgeRating) == false) {
      Logger.error("No age rating matching configured for amazon rating: " + amazonAgeRating);
    } else {
      ageRating = AMAZON_AGE_RATING_MAP.get(amazonAgeRating);
    }

    final String languageInformations = doc.$("#productDetailsTable b:contains('Sprache:')").parent().text().replace("Sprache:", "");
    final String[] languageSplit = StringUtils.split(languageInformations, ',');
    final Set<String> audioFormats = new HashSet<>(Arrays.asList(languageSplit));
    final String eanNr = asinToEanNr(asinNr);

    return Optional.of(new AmazonResult(title, ageRating, rating, copyType, asinNr, eanNr, audioFormats, ""));
  }

  /**
   * Gets the rating from the parent content
   *
   * @param parentClass the parent class which contains the rating
   * @param parent      the parent content containing the raiting
   */
  private static String extractRating(final String parentClass, final Jerry parent) {
    final String ratingText = parent.$("." + parentClass + " .a-icon-alt").text();
    final String rating = (StringUtils.isBlank(ratingText)) ? "" : StringUtils.split(ratingText, ' ')[0];
    return rating;
  }

  /**
   * Cleans the title
   *
   * @param originalTitle the original title from amazon
   * @return the cleaned title
   */
  private static String cleanTitle(final String originalTitle) {
    String title = originalTitle;

    if (CollectionUtils.isEmpty(AMAZON_REMOVE_FROM_TITLE) == false) {
      for (final String removeFromTitle : AMAZON_REMOVE_FROM_TITLE) {
        title = StringUtils.remove(title, removeFromTitle);
      }
    }
    title = StringUtils.trim(title);

    return title;
  }

  /**
   * Matches the amazon copy type text to an internal type
   *
   * @param amazonCopyType the text type from amazone
   * @return the matched copy type
   */
  private static String getCopyTypeFromAmazonText(final String amazonCopyType) {

    final String trimmedCopyType = StringUtils.trimToEmpty(amazonCopyType);

    if (AMAZON_COPY_TYPE_MAP.containsKey(trimmedCopyType) == false) {
      Logger.info("Skipping amazon type: " + trimmedCopyType + " the type was not configured in the type map");
      return "";
    }

    String copyType = AMAZON_COPY_TYPE_MAP.get(trimmedCopyType);
    if (copyType.equals("BLURAY") == true && StringUtils.contains(trimmedCopyType, "[Blu-ray 3D]") == true) {
      copyType = "BLURAY3D";
    }

    return copyType;
  }
}
