package helpers;

import com.typesafe.config.ConfigFactory;

import java.util.List;

/**
 * User: tuxburner
 * Date: 2/4/13
 * Time: 1:01 AM
 */
public class DvdInfoHelper {

  public static List<String> getAgeRatings() {
    final List<String> ratings = ConfigFactory.load().getStringList("dvddb.ageratings");
    return ratings;
  }

  public static List<String> getCopyTypes() {
    final List<String> ratings = ConfigFactory.load().getStringList("dvddb.copytypes");
    return ratings;
  }
}
