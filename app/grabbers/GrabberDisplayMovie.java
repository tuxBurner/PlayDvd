package grabbers;

import java.util.List;

/**
 * This displays the movie where the user can select the backdrops and posters
 * 
 * @author tuxburner
 * 
 */
public class GrabberDisplayMovie {

  public final String systemId;
  public final String movieTitle;
  public final List<String> posterUrls;
  public final List<String> backDropUrls;
  public final EGrabberType grabber;
  public final String plot;

  public GrabberDisplayMovie(final String systemId, final String movieTitle, final String plot, final List<String> posterUrls, final List<String> backDropUrls, final EGrabberType grabber) {
    this.systemId = systemId;
    this.movieTitle = movieTitle;
    this.plot = plot;
    this.posterUrls = posterUrls;
    this.backDropUrls = backDropUrls;
    this.grabber = grabber;

  }

}
