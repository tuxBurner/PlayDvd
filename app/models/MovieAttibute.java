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
public class MovieAttibute extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = -6491899975286773215L;

  /**
   * Default finder for the {@link MovieAttibute}
   */
  public static Finder<Long, MovieAttibute> finder = new Finder<Long, MovieAttibute>(Long.class, MovieAttibute.class);

  @Id
  public Long pk;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Movie> movies;

  /**
   * finds all {@link MovieAttibute} by the given values and the given
   * {@link EAttributeType}
   * 
   * @return
   */
  public static Set<MovieAttibute> findAttributesByName(final Set<String> values, final EAttributeType type) {
    final Set<MovieAttibute> findSet = MovieAttibute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<MovieAttibute> getAllByType(final EAttributeType type) {
    final List<MovieAttibute> findList = MovieAttibute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Creates a {@link MovieAttibute} by the given {@link EAttributeType} and value
   * 
   * @param type
   * @param value
   * @return
   */
  public static MovieAttibute createAttribute(final EAttributeType type, final String value) {
    final MovieAttibute dvdAttibute = new MovieAttibute();
    dvdAttibute.attributeType = type;
    dvdAttibute.value = value;

    dvdAttibute.save();

    return dvdAttibute;
  }

  /**
   * Gathers all {@link MovieAttibute} from the database and adds them if there
   * are not in the database
   * 
   * @param attributeValues
   * @param type
   * @return
   */
  public static Set<MovieAttibute> gatherAndAddAttributes(final Set<String> attributeValues, final EAttributeType type) {

    Set<MovieAttibute> dbAttributes = MovieAttibute.findAttributesByName(attributeValues, type);
    if (dbAttributes == null) {
      dbAttributes = new HashSet<MovieAttibute>();
    }

    final Set<MovieAttibute> attributes = new HashSet<MovieAttibute>();

    for (final String formAttr : attributeValues) {

      MovieAttibute dvdAttibuteToAdd = null;

      for (final MovieAttibute dvdAttibute : dbAttributes) {
        if (dvdAttibute.value.equals(formAttr) == true) {
          dvdAttibuteToAdd = dvdAttibute;
          break;
        }
      }

      // attr does not exists in the db ? we will create it
      if (dvdAttibuteToAdd == null) {
        dvdAttibuteToAdd = MovieAttibute.createAttribute(type, formAttr);
      }

      attributes.add(dvdAttibuteToAdd);
    }

    return attributes;
  }

}
