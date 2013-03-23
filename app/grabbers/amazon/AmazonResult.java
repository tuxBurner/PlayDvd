package grabbers.amazon;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
  public final String ean;
  public final List<String> audioTypes;

  public AmazonResult(String title, String rating, String copyType, String asin, String ean, Set<String> audioTypes) {

    this.title = title;
    this.rating = rating;
    this.copyType = copyType;
    this.asin = asin;
    this.ean = ean;
    this.audioTypes = new ArrayList<String>(audioTypes);
  }
}
