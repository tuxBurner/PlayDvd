package grabbers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.typesafe.config.ConfigFactory;
import forms.MovieForm;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.Play;
import play.libs.ws.WSClient;
import play.libs.ws.WSResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * A simple grabber for imdbRating
 * Created by tuxburner on 08.02.17.
 */
public class ImdbRatingGrabber {

  /**
   * The cfg key for the url
   */
  private static final String IMDB_URL_CFG = "dvddb.imdbInfoUrl";

  /**
   * Tries to get the rating for this movie from imdb and sets it at the movieform.
   * @param movieForm
   */
  public void grabImdbRating(final MovieForm movieForm) {

    boolean imdbDataUrl = ConfigFactory.defaultApplication().hasPath(IMDB_URL_CFG);
    if(imdbDataUrl == false) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("No url set at: "+IMDB_URL_CFG);
      }
      return;
    }

    final String urlString = ConfigFactory.defaultApplication().getString(IMDB_URL_CFG);

    // do we have a imdbId set?
    if(StringUtils.isBlank(movieForm.imdbId) == true) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("No imdbId set at movie: "+movieForm.title);
      }
      return;
    }


    final String urlWithId = urlString+movieForm.imdbId;
    if(Logger.isDebugEnabled() == true) {
      Logger.debug("Going to get imdbRating for movie: "+movieForm.title+" via url: "+urlWithId);
    }

    WSClient ws = Play.current().injector().instanceOf(WSClient.class);
    CompletionStage<JsonNode> jsonNodeCompletionStage = ws.url(urlWithId).setHeader("Content-Type", "application/json").setRequestTimeout(1000).get().thenApply(WSResponse::asJson);

    CompletableFuture<JsonNode> completableFuture = jsonNodeCompletionStage .toCompletableFuture();
    JsonNode response = completableFuture.join();

    if(response.has("error") == true) {
      if(Logger.isInfoEnabled() == true) {
        Logger.info("Could not find imdbData for movie: "+movieForm.title+" via url: "+urlWithId+" message: "+response.findPath("error").toString());
      }
      return;
    }

    JsonNode ratingNode = response.path("data").path("rating");
    if(ratingNode instanceof MissingNode == true) {
      if(Logger.isInfoEnabled() == true) {
        Logger.info("Could not find rating in response for movie: "+movieForm.title+" via url: "+urlWithId);
      }
      return;
    }

    movieForm.imdbRating = ratingNode.toString();
    if(Logger.isDebugEnabled() == true) {
      Logger.debug("Found IMDB rating for movie: "+movieForm.title+" ("+movieForm.imdbRating+")");
    }
  }

}
