package grabbers.amazonwebcrawler;

import com.typesafe.config.ConfigFactory;
import helpers.ConfigurationHelper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import jodd.http.HttpBrowser;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

/**
 * Crawls the amazon web page. Normally we used the {@link grabbers.amazon.AmazonMovieLookuper} but there are now
 * restriction how to use the partnernet api
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

    final String url = AMAZON_ENDPOINT_URL + "/s";
    final HttpBrowser browser = new HttpBrowser();
    final Map<String, String> params = new HashMap<>();
    params.put("k", name);
    params.put("i", AMAZON_CATEGORY);
    final HttpRequest request = HttpRequest.get(url)
        .query(params)
        .charset(StandardCharsets.UTF_8.name());
    final HttpResponse httpResponse = browser.sendRequest(request);
    httpResponse.charset(StandardCharsets.UTF_8.name());
    final List<AmazonResult> amazonResults = extractResultsFromSearchPage(httpResponse.bodyText());

    return amazonResults;
  }

  /**
   * Extracts the results from the search page
   *
   * @param pageContent the html content of the search page
   * @return the results
   */
  private static List<AmazonResult> extractResultsFromSearchPage(final String pageContent) {

    final List<AmazonResult> result = new ArrayList<>();

    final Jerry doc = Jerry.jerry(pageContent);

    final Jerry children = doc.$("[data-component-type='s-search-results'] .s-result-list").children();

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
      //return lookUpByEanNR(code);
      return Optional.empty();
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
   * Looks up the {@link AmazonResult} by the given asin nr
   *
   * @param asinNr the asinNr
   * @return the {@link AmazonResult}
   */
  private static Optional<AmazonResult> lookupByAsin(final String asinNr) {
    final String url = AMAZON_ENDPOINT_URL + "/dp/" + asinNr;
    final HttpBrowser browser = new HttpBrowser();
    final HttpRequest request = HttpRequest.get(url)
        .charset(StandardCharsets.UTF_8.name());
    final HttpResponse httpResponse = browser.sendRequest(request);
    httpResponse.charset(StandardCharsets.UTF_8.name());

    final String bodyText = httpResponse.bodyText();

    final Jerry doc = Jerry.jerry(bodyText);
    final String title = cleanTitle(doc.$("#dp-container #title").text());
    final String rating = extractRating("a-icon-star", doc.$("#dp-container"));

    final String typeString = doc.$("#dp-container #bylineInfo_feature_div span:contains('Format: ')").next().text();
    final String copyType = getCopyTypeFromAmazonText(typeString);

    final String amazonAgeRating = doc.$("#dp-container #bylineInfo_feature_div span:contains('Alterseinstufung: ')")
        .next().text();
    String ageRating = "";
    if (AMAZON_AGE_RATING_MAP.containsKey(amazonAgeRating) == false) {
      Logger.error("No agerating matching configured for amazon rating: " + amazonAgeRating);
    } else {
      ageRating = AMAZON_AGE_RATING_MAP.get(amazonAgeRating);
    }

    final String languageInformations = doc.$("#productDetailsTable b:contains('Sprache:')").parent().text().replace("Sprache:","");
    final String[] languageSplit = StringUtils.split(languageInformations, ',');
    final Set<String> audioFormats = new HashSet<>(Arrays.asList(languageSplit));
    
    return Optional.of(new AmazonResult(title, ageRating, rating, copyType, asinNr, "", audioFormats, ""));
  }

  /**
   * Gets the rating from the parent content
   *
   * @param parentClass the parent class which contains the rating
   * @param parent the parent content containing the raiting
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
