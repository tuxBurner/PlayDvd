package grabbers.amazonwebcrawler;

import com.fasterxml.jackson.databind.JsonNode;
import com.typesafe.config.ConfigFactory;
import grabbers.amazon.AmazonResult;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.Play;
import play.libs.ws.*;

import javax.inject.Inject;
import java.util.List;
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

    WSClient ws = Play.current().injector().instanceOf(WSClient.class);

    final String url = AMAZON_ENDPOINT_URL + "/s";
    final WSRequest wsRequest = ws.url(url)
      .addQueryParameter("k", name)
      .addQueryParameter("i", AMAZON_CATEGOTY);

    final CompletionStage<String> completionStage = wsRequest.get().thenApply(WSResponse::getBody);
    CompletableFuture<String> completableFuture = completionStage .toCompletableFuture();
    final String bytes = completableFuture.join();


    return null;
  }
}
