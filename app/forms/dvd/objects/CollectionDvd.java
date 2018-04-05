package forms.dvd.objects;

import models.Dvd;
import org.apache.commons.lang.StringUtils;

import models.DvdAttribute;

/**
 * Holds a dvd for displaying it in the info panel for viewing dvds in the same
 * box
 * 
 * @author tuxburner
 * 
 */
public class CollectionDvd {

  public Boolean hasPoster;
  public String title;
  public Long id;
  public Long movieId;
  public String copyTypeAttribute;
  public String borrowerName;
  public String ageRating;
  public Integer hullNr;

  public CollectionDvd(final Dvd copy) {
    hasPoster = copy.movie.hasPoster;
    title = copy.movie.title;
    movieId = copy.movie.id;
    id = copy.id;
    hullNr = copy.hullNr;

    copyTypeAttribute = DvdAttribute.getCopyTypeAttribute(copy);
    ageRating = DvdAttribute.getAgeRatingAttribute(copy);

    if (copy.borrowDate != null && copy.borrower != null) {
      borrowerName = copy.borrower.userName;
    }
    if (copy.borrowDate != null && StringUtils.isEmpty(copy.borrowerName) == false) {
      borrowerName = copy.borrowerName;
    }
  }

}
