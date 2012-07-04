package models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import org.apache.commons.collections.CollectionUtils;

import play.db.ebean.Model;

/**
 * This is the {@link Entity} holding certain attributes
 * 
 * @author tuxburner
 * 
 */
@Entity
public class DvdAttibute extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = -6491899975286773215L;

  /**
   * Default finder for the {@link DvdAttibute}
   */
  public static Finder<Long, DvdAttibute> finder = new Finder<Long, DvdAttibute>(Long.class, DvdAttibute.class);

  @Id
  public Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EDvdAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Dvd> dvds;

  /**
   * finds all {@link DvdAttibute} by the given values and the given
   * {@link EMovieAttributeType}
   * 
   * @return
   */
  public static Set<DvdAttibute> findAttributesByName(final Set<String> values, final EDvdAttributeType type) {
    final Set<DvdAttibute> findSet = DvdAttibute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<DvdAttibute> getAllByType(final EDvdAttributeType type) {
    final List<DvdAttibute> findList = DvdAttibute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Gets all the {@link MovieAttibute} as a {@link List} of {@link String}
   * 
   * @param type
   * @return
   */
  public static List<String> getAllByTypeAsValue(final EDvdAttributeType type) {
    final List<DvdAttibute> allByType = DvdAttibute.getAllByType(type);
    final List<String> returnVal = new ArrayList<String>();
    returnVal.add("");
    if (CollectionUtils.isEmpty(allByType) == false) {
      for (final DvdAttibute attr : allByType) {
        returnVal.add(attr.value);
      }
    }

    return returnVal;
  }

  /**
   * Creates a {@link DvdAttibute} by the given {@link EMovieAttributeType} and
   * value
   * 
   * @param type
   * @param value
   * @return
   */
  public static DvdAttibute createAttribute(final EDvdAttributeType type, final String value) {
    final DvdAttibute dvdAttibute = new DvdAttibute();
    dvdAttibute.attributeType = type;
    dvdAttibute.value = value;

    dvdAttibute.save();

    return dvdAttibute;
  }

  /**
   * Gathers all {@link DvdAttibute} from the database and adds them if there
   * are not in the database
   * 
   * @param attributeValues
   * @param type
   * @return
   */
  public static Set<DvdAttibute> gatherAndAddAttributes(final Set<String> attributeValues, final EDvdAttributeType type) {

    Set<DvdAttibute> dbAttributes = DvdAttibute.findAttributesByName(attributeValues, type);
    if (dbAttributes == null) {
      dbAttributes = new HashSet<DvdAttibute>();
    }

    final Set<DvdAttibute> attributes = new HashSet<DvdAttibute>();

    for (final String formAttr : attributeValues) {

      DvdAttibute dvdAttibuteToAdd = null;

      for (final DvdAttibute dvdAttibute : dbAttributes) {
        if (dvdAttibute.value.equals(formAttr) == true) {
          dvdAttibuteToAdd = dvdAttibute;
          break;
        }
      }

      // attr does not exists in the db ? we will create it
      if (dvdAttibuteToAdd == null) {
        dvdAttibuteToAdd = DvdAttibute.createAttribute(type, formAttr);
      }

      attributes.add(dvdAttibuteToAdd);
    }

    return attributes;
  }

}
