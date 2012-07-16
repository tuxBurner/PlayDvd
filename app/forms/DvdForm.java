package forms;

import java.util.List;
import java.util.Set;

import com.typesafe.config.ConfigFactory;

import play.data.validation.Constraints.Required;

import models.Dvd;
import models.DvdAttribute;
import models.EDvdAttributeType;
import models.EMovieAttributeType;
import models.Movie;
import models.MovieAttribute;

/**
 * Form which handles all the inputs for adding, editing a dvd
 * 
 * @author tuxburner
 * 
 */
public class DvdForm {

  public Long dvdId;

  /**
   * The id if the movie the dvd references to
   */
  @Required(message = "No Movie was selected")
  public Long movieId;

  /**
   * This describes the box in which the DVD is in for example Arnolds Ultimate
   * Collectors Box
   */
  public String box;

  /**
   * This describes a collection of boxses for example
   */
  public String collection;

  public String ownerName;

  public String ageRating;

  public String copyType;

  public Integer hullNr;

  /**
   * Transforms a {@link Dvd} to a {@link DvdForm} for editing the dvd in the
   * frontend
   * 
   * @param dvd
   * @return
   */
  public static DvdForm dvdToDvdForm(final Dvd dvd) {

    final DvdForm dvdForm = new DvdForm();

    dvdForm.movieId = dvd.movie.id;
    dvdForm.dvdId = dvd.id;
    dvdForm.ownerName = dvd.owner.userName;
    dvdForm.hullNr = dvd.hullNr;

    final Set<DvdAttribute> dvdAttrs = dvd.attributes;
    for (final DvdAttribute dvdAttibute : dvdAttrs) {
      if (EDvdAttributeType.BOX.equals(dvdAttibute.attributeType)) {
        dvdForm.box = dvdAttibute.value;
      }

      if (EDvdAttributeType.COLLECTION.equals(dvdAttibute.attributeType)) {
        dvdForm.collection = dvdAttibute.value;
      }

      if (EDvdAttributeType.RATING.equals(dvdAttibute.attributeType)) {
        dvdForm.ageRating = dvdAttibute.value;
      }

      if (EDvdAttributeType.COPY_TYPE.equals(dvdAttibute.attributeType)) {
        dvdForm.copyType = dvdAttibute.value;
      }
    }

    return dvdForm;
  }

  public static List<String> getAgeRatings() {
    final List<String> ratings = ConfigFactory.load().getStringList("dvddb.ageratings");
    return ratings;
  }

  public static List<String> getCopyTypes() {
    final List<String> ratings = ConfigFactory.load().getStringList("dvddb.copytypes");
    return ratings;
  }
}
