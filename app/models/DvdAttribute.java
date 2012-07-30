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

import com.google.gson.Gson;

/**
 * This is the {@link Entity} holding certain attributes
 * 
 * @author tuxburner
 * 
 */
@Entity
public class DvdAttribute extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = -6491899975286773215L;

  /**
   * Default finder for the {@link DvdAttribute}
   */
  public static Finder<Long, DvdAttribute> finder = new Finder<Long, DvdAttribute>(Long.class, DvdAttribute.class);

  @Id
  public Long id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EDvdAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Dvd> dvds;

  /**
   * finds all {@link DvdAttribute} by the given values and the given
   * {@link EMovieAttributeType}
   * 
   * @return
   */
  public static Set<DvdAttribute> findAttributesByName(final Set<String> values, final EDvdAttributeType type) {
    final Set<DvdAttribute> findSet = DvdAttribute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<DvdAttribute> getAllByType(final EDvdAttributeType type) {
    final List<DvdAttribute> findList = DvdAttribute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Gets all the {@link MovieAttribute} as a {@link List} of {@link String}
   * 
   * @param type
   * @return
   */
  public static List<String> getAllByTypeAsValue(final EDvdAttributeType type) {
    final List<DvdAttribute> allByType = DvdAttribute.getAllByType(type);
    final List<String> returnVal = new ArrayList<String>();
    returnVal.add("");
    if (CollectionUtils.isEmpty(allByType) == false) {
      for (final DvdAttribute attr : allByType) {
        returnVal.add(attr.value);
      }
    }

    return returnVal;
  }

  /**
   * Returns all {@link MovieAttribute} as json string
   * 
   * @param type
   * @return
   */
  public static String getAllByTypeAsJson(final EDvdAttributeType type) {

    final List<DvdAttribute> allByType = DvdAttribute.getAllByType(type);
    final List<String> returnVal = new ArrayList<String>();
    if (CollectionUtils.isEmpty(allByType) == false) {
      for (final DvdAttribute attr : allByType) {
        returnVal.add(attr.value);
      }
    }

    final Gson gson = new Gson();
    return gson.toJson(returnVal);
  }

  /**
   * Creates a {@link DvdAttribute} by the given {@link EMovieAttributeType} and
   * value
   * 
   * @param type
   * @param value
   * @return
   */
  public static DvdAttribute createAttribute(final EDvdAttributeType type, final String value) {
    final DvdAttribute dvdAttibute = new DvdAttribute();
    dvdAttibute.attributeType = type;
    dvdAttibute.value = value;

    dvdAttibute.save();

    return dvdAttibute;
  }

  /**
   * Gathers all {@link DvdAttribute} from the database and adds them if there
   * are not in the database
   * 
   * @param attributeValues
   * @param type
   * @return
   */
  public static Set<DvdAttribute> gatherAndAddAttributes(final Set<String> attributeValues, final EDvdAttributeType type) {

    Set<DvdAttribute> dbAttributes = DvdAttribute.findAttributesByName(attributeValues, type);
    if (dbAttributes == null) {
      dbAttributes = new HashSet<DvdAttribute>();
    }

    final Set<DvdAttribute> attributes = new HashSet<DvdAttribute>();

    for (final String formAttr : attributeValues) {

      DvdAttribute dvdAttibuteToAdd = null;

      for (final DvdAttribute dvdAttibute : dbAttributes) {
        if (dvdAttibute.value.equals(formAttr) == true) {
          dvdAttibuteToAdd = dvdAttibute;
          break;
        }
      }

      // attr does not exists in the db ? we will create it
      if (dvdAttibuteToAdd == null) {
        dvdAttibuteToAdd = DvdAttribute.createAttribute(type, formAttr);
      }

      attributes.add(dvdAttibuteToAdd);
    }

    return attributes;
  }

}
