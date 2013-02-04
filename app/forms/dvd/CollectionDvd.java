package forms.dvd;

import org.apache.commons.lang.StringUtils;

import models.Dvd;
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

  public CollectionDvd(final Dvd dvd) {
    hasPoster = dvd.movie.hasPoster;
    title = dvd.movie.title;
    movieId = dvd.movie.id;
    id = dvd.id;
    hullNr = dvd.hullNr;

    copyTypeAttribute = DvdAttribute.getCopyTypeAttribute(dvd);
    ageRating = DvdAttribute.getAgeRatingAttribute(dvd);

    if (dvd.borrowDate != null && dvd.borrower != null) {
      borrowerName = dvd.borrower.userName;
    }
    if (dvd.borrowDate != null && StringUtils.isEmpty(dvd.borrowerName) == false) {
      borrowerName = dvd.borrowerName;
    }
  }

}
