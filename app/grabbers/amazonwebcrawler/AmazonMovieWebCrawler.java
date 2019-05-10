package grabbers.amazonwebcrawler;

import com.typesafe.config.ConfigFactory;
import grabbers.amazon.AmazonResult;
import helpers.ConfigurationHelper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    for (Jerry child : children) {

      String title = child.$("h2 span").text();
      if (CollectionUtils.isEmpty(AMAZON_REMOVE_FROM_TITLE) == false) {
        for (final String removeFromTitle : AMAZON_REMOVE_FROM_TITLE) {
          title = StringUtils.remove(title, removeFromTitle);
        }
      }
      title = StringUtils.trim(title);

      if (StringUtils.contains(child.html(), "<span class=\"a-size-base a-color-secondary\">Gesponsert</span>")) {
        Logger.info("Skipping: " + title + " for search it is an affiliated content.");
        continue;
      }

      final String asin = child.attr("data-asin");
      final String imageUrl = child.$("[data-component-type='s-product-image'] img").attr("src");

      final String ratingText = child.$(".a-icon-star-small .a-icon-alt").text();
      final String rating = (StringUtils.isBlank(ratingText)) ? "" : StringUtils.split(ratingText, ' ')[0];

      // Gett all the types like dvd and bluray ...
      final Jerry typeLinks = child.$("a.a-text-bold");
      for (final Jerry typeLink : typeLinks) {

        final String typeText = typeLink.text().trim();
        if (AMAZON_COPY_TYPE_MAP.containsKey(typeText) == false) {
          Logger.info("Skipping amazon type: " + typeText + " for asin: " + asin
              + " the type was not configured in the type map");
          continue;
        }

        String copyType = AMAZON_COPY_TYPE_MAP.get(typeText);
        if (copyType.equals("BLURAY") == true && StringUtils.contains(title, "[Blu-ray 3D]") == true) {
          copyType = "BLURAY3D";
        }

        Logger.info("---------------------------------------------------------");
        Logger.info(asin);
        Logger.info(imageUrl);
        Logger.info(copyType);
        Logger.info(rating);
        Logger.info(title);
        Logger.info("---------------------------------------------------------");

        result.add(new AmazonResult(title, rating, copyType, asin, "Ean", new HashSet<>(), imageUrl));
      }

    }

    return result;
  }
}
