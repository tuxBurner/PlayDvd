package grabbers.amazon;

/**
 * This holds the data of the {@link AmazonMovieLookuper}
 * User: tuxburner
 * Date: 2/18/13
 * Time: 8:27 PM
 */
public class AmazonResult {


  public final String title;
  public final String rating;
  public final String copyType;
  public final String asin;

  public AmazonResult(String title, String rating, String copyType, String asin) {

    this.title = title;
    this.rating = rating;
    this.copyType = copyType;
    this.asin = asin;
  }
}
