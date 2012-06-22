package models;

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
  public Long pk;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Movie> movies;

  /**
   * finds all {@link DvdAttibute} by the given values and the given
   * {@link EAttributeType}
   * 
   * @return
   */
  public static Set<DvdAttibute> findAttributesByName(final Set<String> values, final EAttributeType type) {
    final Set<DvdAttibute> findSet = DvdAttibute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<DvdAttibute> getAllByType(final EAttributeType type) {
    final List<DvdAttibute> findList = DvdAttibute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Creates a {@link DvdAttibute} by the given {@link EAttributeType} and value
   * 
   * @param type
   * @param value
   * @return
   */
  public static DvdAttibute createAttribute(final EAttributeType type, final String value) {
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
  public static Set<DvdAttibute> gatherAndAddAttributes(final Set<String> attributeValues, final EAttributeType type) {

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

      // attr does not exists in the db ? we will create id
      if (dvdAttibuteToAdd == null) {
        dvdAttibuteToAdd = DvdAttibute.createAttribute(type, formAttr);
      }

      attributes.add(dvdAttibuteToAdd);
    }

    return attributes;
  }

}
