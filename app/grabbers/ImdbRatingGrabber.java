package grabbers;

import com.typesafe.config.ConfigFactory;
import forms.MovieForm;
import jodd.jerry.Jerry;
import org.apache.commons.lang3.StringUtils;
import play.Logger;

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
   *
   * @param movieForm
   */
  public void grabImdbRating(final MovieForm movieForm) {

    boolean imdbDataUrl = ConfigFactory.defaultApplication().hasPath(IMDB_URL_CFG);
    if (imdbDataUrl == false) {
      if (Logger.isDebugEnabled() == true) {
        Logger.debug("No url set at: " + IMDB_URL_CFG);
      }
      return;
    }

    final String urlString = ConfigFactory.defaultApplication().getString(IMDB_URL_CFG);

    // do we have a imdbId set?
    if (StringUtils.isBlank(movieForm.imdbId) == true) {
      if (Logger.isDebugEnabled() == true) {
        Logger.debug("No imdbId set at movie: " + movieForm.title);
      }
      return;
    }


    final String urlWithId = urlString + movieForm.imdbId;
    if (Logger.isDebugEnabled() == true) {
      Logger.debug("Going to get imdbRating for movie: " + movieForm.title + " via url: " + urlWithId);
    }


    final Jerry imdbDoc = HttpBrowserHelper.getUrlAsJerryDoc(urlWithId);
    final String imdbRating = imdbDoc.$(".imdbRating span[itemprop='ratingValue']").text();

    movieForm.imdbRating = imdbRating;

    if (Logger.isDebugEnabled() == true) {
      Logger.debug("Found IMDB rating for movie: " + movieForm.title + " (" + movieForm.imdbRating + ")");
    }

  }

}
