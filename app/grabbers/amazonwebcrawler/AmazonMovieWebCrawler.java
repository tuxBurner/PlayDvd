package grabbers.amazonwebcrawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import grabbers.amazon.AmazonResult;
import jodd.http.HttpBrowser;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.jerry.Jerry;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.Play;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Crawls the amazon web page.
 * Normally we used the {@link grabbers.amazon.AmazonMovieLookuper} but there are now restriction how to use the partnernet api
 */
public class AmazonMovieWebCrawler  {


  /**
   * Web endpoint to look on
   */
  private final static String AMAZON_ENDPOINT_URL;


  /**
   * Category to search in
   */
  private final static String AMAZON_CATEGOTY;

  static {
    AMAZON_ENDPOINT_URL = ConfigFactory.load().getString("dvddb.amazon.webEndPoint");
    if (StringUtils.isEmpty(AMAZON_ENDPOINT_URL) == true) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No amazon web endPoint set in the configuration.");
      }
    }

    AMAZON_CATEGOTY = ConfigFactory.load().getString("dvddb.amazon.webCategory");
    if (StringUtils.isEmpty(AMAZON_CATEGOTY) == true) {
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
    HttpBrowser browser = new HttpBrowser();
    final Map<String,String> params = new HashMap<>();
    params.put("k",name);
    params.put("i",AMAZON_CATEGOTY);
    HttpRequest request = HttpRequest.get(url)
      .query(params);
    final HttpResponse httpResponse = browser.sendRequest(request);

    final List<AmazonResult> amazonResults = extractResultsFromSearchPage(httpResponse.body());

    return amazonResults;
  }

  /**
   * Extracts the results from the search page
   * @param pageContent the html content of the search page
   * @return the results
   */
  private static List<AmazonResult> extractResultsFromSearchPage(final String pageContent) {

    //Logger.debug(pageContent);

    final Jerry doc = Jerry.jerry(pageContent);

    final Jerry children = doc.$("[data-component-type='s-search-results'] .s-result-list").children();

    final List<AmazonResult> result = new ArrayList<>();

    for (Jerry child : children) {

      final String asin = child.attr("data-asin");
      final String imageUrl = child.$("[data-component-type='s-product-image'] img").attr("src");
      final String title = child.$("h2 span").text();


      // DVD Blu-ray lookup

      Logger.info("---------------------------------------------------------");
      Logger.info(asin);
      Logger.info(imageUrl);
      Logger.info("---------------------------------------------------------");

      result.add(new AmazonResult(title,"4711","Type",asin,"Ean",new HashSet<>(),imageUrl));

    }



    return result;
  }
}
