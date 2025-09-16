package grabbers.amazonwebcrawler;


/**
 * This holds the data of the amazon
 * User: tuxburner
 * Date: 2/18/13
 * Time: 8:27 PM
 */
public class AmazonResult {


  public final String title;
  public final String rating;
  public final String copyType;
  public String asin;
  public String ean;
  public final String audioTypes;
  public final String imageUrl;
  private final String userRating;

  public AmazonResult(String title, String rating, String userRating, String copyType, String asin, String ean, String audioTypes, String imageUrl) {

    this.title = title;
    this.rating = rating;
    this.copyType = copyType;
    this.asin = asin;
    this.ean = ean;
    this.audioTypes = audioTypes;
    this.imageUrl = imageUrl;
    this.userRating = userRating;
  }

  public void setAsin(String asin) {
    this.asin = asin;
  }

  public void setEan(String ean) {
    this.ean = ean;
  }
}
