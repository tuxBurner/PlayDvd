package grabbers;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * This displays the movie where the user can select the backdrops and posters
 * 
 * @author tuxburner
 * 
 */
public class GrabberDisplayMovie {

  public final String systemId;
  public final String imdbId;
  public final String movieTitle;
  public final List<GrabberImage> posterUrls;
  public final List<GrabberImage> backDropUrls;
  public final EGrabberType grabber;
  public final String plot;
  public final List<String> trailerUrls;

  public GrabberDisplayMovie(final String systemId, final String movieTitle, final String plot, final List<GrabberImage> posterUrls, final List<GrabberImage> backDropUrls, final List<String> trailerUrls, final EGrabberType grabber, final String imdbId) {
    this.systemId = systemId;
    this.movieTitle = movieTitle;
    this.plot = plot;
    this.posterUrls = posterUrls;
    this.backDropUrls = backDropUrls;
    this.trailerUrls = trailerUrls;
    this.grabber = grabber;
    this.imdbId = imdbId;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);

  }
}
