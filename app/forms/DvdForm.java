package forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Dvd;
import models.DvdAttibute;
import models.EDvdAttributeType;
import models.EMovieAttributeType;
import models.MovieAttibute;
import play.data.validation.Constraints.Required;

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

    final Set<DvdAttibute> dvdAttrs = dvd.attributes;
    for (final DvdAttibute dvdAttibute : dvdAttrs) {
      if (EDvdAttributeType.BOX.equals(dvdAttibute.attributeType)) {
        dvdForm.box = dvdAttibute.value;
      }

      if (EDvdAttributeType.COLLECTION.equals(dvdAttibute.attributeType)) {
        dvdForm.collection = dvdAttibute.value;
      }
    }

    return dvdForm;
  }

}
