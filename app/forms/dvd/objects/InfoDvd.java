package forms.dvd.objects;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import forms.MovieForm;
import forms.dvd.DvdForm;
import forms.dvd.objects.CollectionDvd;
import models.Dvd;
import models.EDvdAttributeType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class InfoDvd {

  public DvdForm dvdForm;

  public List<CollectionDvd> boxDvds;

  public List<CollectionDvd> collectionDvds;

  public List<CollectionDvd> seriesDvd;

  public String borrowedBy;

  public String title;

  public Date borrowedOn;

  public MovieForm movieForm;

  public InfoDvd(final Dvd dvd) {
    dvdForm = DvdForm.dvdToDvdForm(dvd);
    movieForm = MovieForm.movieToForm(dvd.movie);

    if (dvd.borrower != null) {
      borrowedBy = dvd.borrower.userName;
    }

    if (dvd.borrowerName != null) {
      borrowedBy = dvd.borrowerName;
    }

    if (dvd.borrowDate != null) {
      borrowedOn = new Date(dvd.borrowDate);
    }

    title = dvd.movie.title;
    if(StringUtils.isEmpty(dvd.additionalInfo) == false) {
      title += " ["+dvd.additionalInfo+"]";
    }

    final List<Long> alreadyAdded = new ArrayList<Long>();
    boxDvds = getDvdsByBoxOrCollection(dvd, EDvdAttributeType.BOX, dvdForm.box, alreadyAdded);
    collectionDvds = getDvdsByBoxOrCollection(dvd, EDvdAttributeType.COLLECTION, dvdForm.collection, alreadyAdded);
    seriesDvd = getDvdsByMovieSeries(dvd, movieForm.series, alreadyAdded);
  }

  /**
   * Collects all Dvds which have the attribute and the same owner
   * 
   * @param dvd
   * @param attrvalue
   * @param attrvalue
   * @param alreadyAdded
   * @return
   */
  private List<CollectionDvd> getDvdsByMovieSeries(final Dvd dvd, final String attrvalue, final List<Long> alreadyAdded) {

    if (StringUtils.isEmpty(attrvalue) == true) {
      return null;
    }

    final List<Dvd> boxDbDvds = Dvd.getbyMovieSeries(attrvalue, dvd);
    List<CollectionDvd> returnList = null;
    if (CollectionUtils.isEmpty(boxDbDvds) == false) {
      returnList = new ArrayList<CollectionDvd>();
      for (final Dvd boxDvd : boxDbDvds) {
        if (alreadyAdded.contains(boxDvd.id) == false) {
          returnList.add(new CollectionDvd(boxDvd));
          alreadyAdded.add(boxDvd.id);
        }
      }
    }

    return returnList;
  }

  /**
   * Collects all Dvds which have the attribute and the same owner
   * 
   * @param dvd
   * @param attrType
   * @param attrvalue
   * @return
   */
  private List<CollectionDvd> getDvdsByBoxOrCollection(final Dvd dvd, final EDvdAttributeType attrType, final String attrvalue, final List<Long> alreadyAdded) {

    if (StringUtils.isEmpty(attrvalue) == true) {
      return null;
    }

    final List<Dvd> boxDbDvds = Dvd.getDvdByBoxOrCollection(attrType, attrvalue, dvd);
    List<CollectionDvd> returnList = null;
    if (CollectionUtils.isEmpty(boxDbDvds) == false) {
      returnList = new ArrayList<CollectionDvd>();
      for (final Dvd boxDvd : boxDbDvds) {
        if (alreadyAdded.contains(boxDvd.id) == false) {
          alreadyAdded.add(boxDvd.id);
          returnList.add(new CollectionDvd(boxDvd));
        }
      }
    }

    return returnList;
  }

}
