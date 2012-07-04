package forms;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import models.Dvd;
import models.EDvdAttributeType;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

public class InfoDvd {

  public DvdForm dvdForm;

  public List<CollectionDvd> boxDvds;

  public List<CollectionDvd> collectionDvds;

  public String borrowedBy;

  public Date borrowedOn;

  public InfoDvd(final Dvd dvd) {
    dvdForm = DvdForm.dvdToDvdForm(dvd);

    if (dvd.borrower != null) {
      borrowedBy = dvd.borrower.userName;
    }

    if (dvd.borrowerName != null) {
      borrowedBy = dvd.borrowerName;
    }

    if (dvd.borrowDate != null) {
      borrowedOn = new Date(dvd.borrowDate);
    }

    boxDvds = getDvdsByBoxOrCollection(dvd, EDvdAttributeType.BOX, dvdForm.box);
    collectionDvds = getDvdsByBoxOrCollection(dvd, EDvdAttributeType.COLLECTION, dvdForm.collection);

  }

  /**
   * Collects all Dvds which have the attribute and the same owner
   * 
   * @param dvd
   * @param attributeType
   * @param attrvalue
   * @return
   */
  private List<CollectionDvd> getDvdsByBoxOrCollection(final Dvd dvd, final EDvdAttributeType attrType, final String attrvalue) {

    if (StringUtils.isEmpty(attrvalue) == true) {
      return null;
    }

    final List<Dvd> boxDbDvds = Dvd.getDvdByBoxOrCollection(attrType, attrvalue, dvd);
    List<CollectionDvd> returnList = null;
    if (CollectionUtils.isEmpty(boxDbDvds) == false) {
      returnList = new ArrayList<CollectionDvd>();
      for (final Dvd boxDvd : boxDbDvds) {
        returnList.add(new CollectionDvd(boxDvd));
      }
    }

    return returnList;
  }

}
