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

import com.google.gson.GsonBuilder;
import helpers.SelectAjaxContainer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

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
   * Returns all {@link MovieAttribute} as json string
   * 
   * @param type
   * @return
   * @deprecated
   */
  @Deprecated
  public static String getAllByTypeAsJson(final EDvdAttributeType type) {

    final List<DvdAttribute> allByType = DvdAttribute.getAllByType(type);
    final List<String> returnVal = new ArrayList<String>();
    if (CollectionUtils.isEmpty(allByType) == false) {
      for (final DvdAttribute attr : allByType) {

        if (StringUtils.isEmpty(attr.value)) {
          continue;
        }

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
    final DvdAttribute dvdAttribute = new DvdAttribute();
    dvdAttribute.attributeType = type;
    dvdAttribute.value = value;

    dvdAttribute.save();

    return dvdAttribute;
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

  public static String getAgeRatingAttribute(final Dvd dvd) {
    return DvdAttribute.getSingleAttrFromDvd(dvd, EDvdAttributeType.RATING);
  }

  public static String getCopyTypeAttribute(final Dvd dvd) {
    return DvdAttribute.getSingleAttrFromDvd(dvd, EDvdAttributeType.COPY_TYPE);
  }

  public static String getSingleAttrFromDvd(final Dvd dvd, final EDvdAttributeType attrType) {
    for (final DvdAttribute attribute : dvd.attributes) {
      if (attribute.attributeType.equals(attrType)) {
        return attribute.value;
      }
    }
    return null;
  }

  /**
   * Searches the available {@link DvdAttribute}s as string for the select2
   * element
   *
   * @param attributeType
   * @return
   *
   */
  public static String searchAvaibleAttributesAsJson(final EDvdAttributeType attributeType, final String searchTerm) {

    // no searchTerm ?
    if (StringUtils.isEmpty(searchTerm) == true) {
      return StringUtils.EMPTY;
    }

    final List<DvdAttribute> attributes = DvdAttribute.finder.where().eq("attributeType", attributeType).istartsWith("value", searchTerm).order("value ASC").findList();
    final List<SelectAjaxContainer> retVal = new ArrayList<SelectAjaxContainer>();
    retVal.add(new SelectAjaxContainer(searchTerm, searchTerm));

    for (final DvdAttribute dvdAttribute : attributes) {
      if (StringUtils.isEmpty(dvdAttribute.value) == true || searchTerm.equals(dvdAttribute.value) == true) {
        continue;
      }
      retVal.add(new SelectAjaxContainer(dvdAttribute.value, dvdAttribute.value));
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(retVal);

    return json;
  }

}
