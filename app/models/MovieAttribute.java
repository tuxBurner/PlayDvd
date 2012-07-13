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
import com.google.gson.GsonBuilder;

/**
 * This is the {@link Entity} holding certain attributes
 * 
 * @author tuxburner
 * 
 */
@Entity
public class MovieAttribute extends Model {

  /**
	 * 
	 */
  private static final long serialVersionUID = -6491899975286773215L;

  /**
   * Default finder for the {@link MovieAttribute}
   */
  public static Finder<Long, MovieAttribute> finder = new Finder<Long, MovieAttribute>(Long.class, MovieAttribute.class);

  @Id
  public Long pk;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EMovieAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Movie> movies;

  /**
   * finds all {@link MovieAttribute} by the given values and the given
   * {@link EMovieAttributeType}
   * 
   * @return
   */
  public static Set<MovieAttribute> findAttributesByName(final Set<String> values, final EMovieAttributeType type) {
    final Set<MovieAttribute> findSet = MovieAttribute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<MovieAttribute> getAllByType(final EMovieAttributeType type) {
    final List<MovieAttribute> findList = MovieAttribute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Gets all the {@link MovieAttribute} as a {@link List} of {@link String}
   * 
   * @param type
   * @return
   */
  public static List<String> getAllByTypeAsValue(final EMovieAttributeType type) {
    final List<MovieAttribute> allByType = MovieAttribute.getAllByType(type);
    final List<String> returnVal = new ArrayList<String>();
    returnVal.add("");
    if (CollectionUtils.isEmpty(allByType) == false) {
      for (final MovieAttribute attr : allByType) {
        returnVal.add(attr.value);
      }
    }

    return returnVal;
  }

  /**
   * Creates a {@link MovieAttribute} by the given {@link EMovieAttributeType}
   * and value
   * 
   * @param type
   * @param value
   * @return
   */
  public static MovieAttribute createAttribute(final EMovieAttributeType type, final String value) {
    final MovieAttribute dvdAttibute = new MovieAttribute();
    dvdAttibute.attributeType = type;
    dvdAttibute.value = value;

    dvdAttibute.save();

    return dvdAttibute;
  }

  /**
   * Gathers all {@link MovieAttribute} from the database and adds them if there
   * are not in the database
   * 
   * @param attributeValues
   * @param type
   * @return
   */
  public static Set<MovieAttribute> gatherAndAddAttributes(final Set<String> attributeValues, final EMovieAttributeType type) {

    Set<MovieAttribute> dbAttributes = MovieAttribute.findAttributesByName(attributeValues, type);
    if (dbAttributes == null) {
      dbAttributes = new HashSet<MovieAttribute>();
    }

    final Set<MovieAttribute> attributes = new HashSet<MovieAttribute>();

    for (final String formAttr : attributeValues) {

      MovieAttribute dvdAttibuteToAdd = null;

      for (final MovieAttribute dvdAttibute : dbAttributes) {
        if (dvdAttibute.value.equals(formAttr) == true) {
          dvdAttibuteToAdd = dvdAttibute;
          break;
        }
      }

      // attr does not exists in the db ? we will create it
      if (dvdAttibuteToAdd == null) {
        dvdAttibuteToAdd = MovieAttribute.createAttribute(type, formAttr);
      }

      attributes.add(dvdAttibuteToAdd);
    }

    return attributes;
  }

  /**
   * Gets the avaible {@link MovieAttribute}s as string for the tagit element
   * 
   * @param attributeType
   * @return
   */
  public static String getAvaibleAttributesAsJson(final EMovieAttributeType attributeType) {

    final List<MovieAttribute> attributes = MovieAttribute.getAllByType(attributeType);
    final List<String> attrMap = new ArrayList<String>();
    for (final MovieAttribute dvdAttibute : attributes) {
      attrMap.add(dvdAttibute.value);
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(attrMap);

    return json;
  }

}