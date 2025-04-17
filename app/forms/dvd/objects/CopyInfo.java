package forms.dvd.objects;

import dao.DvdDao;
import forms.MovieForm;
import forms.dvd.CopyForm;
import models.Commentable;
import models.Dvd;
import models.EDvdAttributeType;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CopyInfo {

  public final Commentable commentable = null;

  public CopyForm copyForm;

  public List<CollectionDvd> boxDvds;

  public List<CollectionDvd> collectionDvds;

  public List<CollectionDvd> seriesDvd;

  public String borrowedBy;

  public Boolean borrowerHasGravatar = false;

  public String title;

  public Date borrowedOn;

  public MovieForm movieForm;

  public CopyInfo(final Dvd copy) {
    copyForm = CopyForm.dvdToDvdForm(copy);
    movieForm = MovieForm.movieToForm(copy.getMovie());

    if (copy.getBorrower() != null) {
      borrowedBy = copy.getBorrower().getUserName();
      borrowerHasGravatar = copy.getBorrower().isHasGravatar();
    }

    if (copy.getBorrowerName() != null) {
      borrowedBy = copy.getBorrowerName();
    }

    if (copy.getBorrowDate() != null) {
      borrowedOn = new Date(copy.getBorrowDate());
    }

    title = copy.getMovie().getTitle();
    if (StringUtils.isEmpty(copy.getAdditionalInfo()) == false) {
      title += " [" + copy.getAdditionalInfo() + "]";
    }


    final List<Long> alreadyAdded = new ArrayList<Long>();
    boxDvds = getDvdsByBoxOrCollection(copy, EDvdAttributeType.BOX, copyForm.box, alreadyAdded);
    collectionDvds = getDvdsByBoxOrCollection(copy, EDvdAttributeType.COLLECTION, copyForm.collection, alreadyAdded);
    seriesDvd = getDvdsByMovieSeries(copy, movieForm.series, alreadyAdded);
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

    final List<Dvd> boxDbDvds = DvdDao.getbyMovieSeries(attrvalue, dvd);
    List<CollectionDvd> returnList = null;
    if (CollectionUtils.isEmpty(boxDbDvds) == false) {
      returnList = new ArrayList<CollectionDvd>();
      for (final Dvd boxDvd : boxDbDvds) {
        if (alreadyAdded.contains(boxDvd.getId()) == false) {
          returnList.add(new CollectionDvd(boxDvd));
          alreadyAdded.add(boxDvd.getId());
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

    final List<Dvd> boxDbDvds = DvdDao.getDvdByBoxOrCollection(attrType, attrvalue, dvd);
    List<CollectionDvd> returnList = null;
    if (CollectionUtils.isEmpty(boxDbDvds) == false) {
      returnList = new ArrayList<CollectionDvd>();
      for (final Dvd boxDvd : boxDbDvds) {
        if (alreadyAdded.contains(boxDvd.getId()) == false) {
          alreadyAdded.add(boxDvd.getId());
          returnList.add(new CollectionDvd(boxDvd));
        }
      }
    }

    return returnList;
  }

}
