package grabbers;

import org.apache.commons.lang.builder.ToStringBuilder;

public class GrabberImage implements Comparable<GrabberImage> {

  /**
   * Unique id of the image in the grabber system
   */
  public final String id;

  /**
   * Url to the image to display it
   */
  public final String url;

  public GrabberImage(final String id, final String url) {

    this.id = id;

    this.url = url;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  @Override
  public int compareTo(final GrabberImage o) {
    return id.compareTo(o.id);
  }

}
