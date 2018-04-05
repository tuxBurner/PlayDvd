package grabbers;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.jetbrains.annotations.NotNull;

public class GrabberSearchMovie implements Comparable<GrabberSearchMovie> {

  /**
   * Id of the movie which is used by the grabber
   */
  public final String systemId;

  /**
   * Url to the poster
   */
  public final String posterUrl;

  /**
   * Which grabber was used
   */
  public final EGrabberType grabber;

  /**
   * Name of the movie
   */
  public final String movieTitle;

  public GrabberSearchMovie(final String systemId, final String movieTitle, final String posterUrl, final EGrabberType grabber) {
    this.systemId = systemId;
    this.movieTitle = movieTitle;
    this.posterUrl = posterUrl;
    this.grabber = grabber;

  }

  @Override
  public int compareTo(@NotNull final GrabberSearchMovie o) {
    return movieTitle.compareTo(o.movieTitle);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
