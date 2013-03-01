package forms.dvd;

import com.google.gson.Gson;
import controllers.MovieSelect2Value;
import grabbers.amazon.AmazonResult;
import models.Dvd;
import models.DvdAttribute;
import models.EDvdAttributeType;
import models.Movie;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Constraints.Required;

import java.util.Set;

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

  public String eanNr;

  public String asinNr;

  /**
   * Transforms an {@link AmazonResult} and movieId to a dvdForm
   * @param amazonResult
   * @param movieId
   */
  public static DvdForm eanAndMovieToDvdForm(final AmazonResult amazonResult, final Long movieId, final String eanNr) {
    final DvdForm dvdForm = new DvdForm();
    dvdForm.ageRating = amazonResult.rating;
    dvdForm.copyType = amazonResult.copyType;
    dvdForm.eanNr = eanNr;
    dvdForm.asinNr = amazonResult.asin;
    dvdForm.movieId = movieId;

    return  dvdForm;
  }

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
    dvdForm.eanNr = dvd.eanNr;
    dvdForm.asinNr =  dvd.asinNr;

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



  /**
   * This returns the selected {@link Movie} for the {@link Dvd} as a json
   * representation
   * 
   * @param movieId
   * @return
   */
  public static String getSelectedMovieAsJson(final String movieId) {

    final Gson gson = new Gson();
    if (StringUtils.isEmpty(movieId) == true) {
      return gson.toJson(null);
    }

    final Movie byId = Movie.find.select("id, hasPoster,title").where().eq("id", movieId).findUnique();

    return gson.toJson(new MovieSelect2Value(byId));
  }
}
