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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Form which handles all the inputs for adding, editing a dvd
 *
 * @author tuxburner
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

  public List<String> audioTypes = new ArrayList<String>();

  /**
   * Transforms an {@link AmazonResult} and movieId to a dvdForm
   *
   * @param amazonResult
   * @param movieId
   * @param copy if not null the form will be filled with the informations from the copy
   */
  public static DvdForm amazonAndMovieToDvdForm(final AmazonResult amazonResult, final Long movieId, final Dvd copy) {

    final DvdForm dvdForm = (copy == null) ? new DvdForm() : dvdToDvdForm(copy);
    dvdForm.ageRating = amazonResult.rating;
    dvdForm.copyType = amazonResult.copyType;
    dvdForm.audioTypes = amazonResult.audioTypes;
    dvdForm.asinNr = amazonResult.asin;
    dvdForm.eanNr = amazonResult.ean;
    dvdForm.movieId = movieId;

    Collections.sort(dvdForm.audioTypes);

    return dvdForm;
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
    dvdForm.asinNr = dvd.asinNr;


    final Set<DvdAttribute> dvdAttrs = dvd.attributes;
    for (final DvdAttribute dvdAttribute : dvdAttrs) {

      switch (dvdAttribute.attributeType) {
        case BOX:
          dvdForm.box = dvdAttribute.value;
          break;
        case COLLECTION:
          dvdForm.collection = dvdAttribute.value;
          break;
        case RATING:
          dvdForm.ageRating = dvdAttribute.value;
          break;
        case COPY_TYPE:
          dvdForm.copyType = dvdAttribute.value;
          break;
        case AUDIO_TYPE:
          dvdForm.audioTypes.add(dvdAttribute.value);
          break;
      }

      Collections.sort(dvdForm.audioTypes);

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
