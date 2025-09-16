package forms.copy.objects;

import models.Dvd;
import models.DvdAttribute;
import org.apache.commons.lang3.StringUtils;

/**
 * Holds a dvd for displaying it in the info panel for viewing dvds in the same
 * box
 *
 * @author tuxburner
 */
public class CollectionCopy {

  public Boolean hasPoster;
  public String title;
  public Long id;
  public Long movieId;
  public String copyTypeAttribute;
  public String borrowerName;
  public String ageRating;
  public Integer hullNr;

  public CollectionCopy(final Dvd copy) {
    hasPoster = copy.getMovie().getHasPoster();
    title = copy.getMovie().getTitle();
    movieId = copy.getMovie().getId();
    id = copy.getId();
    hullNr = copy.getHullNr();

    copyTypeAttribute = DvdAttribute.getCopyTypeAttribute(copy);
    ageRating = DvdAttribute.getAgeRatingAttribute(copy);

    if (copy.getBorrowDate() != null && copy.getBorrower() != null) {
      borrowerName = copy.getBorrower().getUserName();
    }
    if (copy.getBorrowDate() != null && StringUtils.isEmpty(copy.getBorrowerName()) == false) {
      borrowerName = copy.getBorrowerName();
    }
  }

}
