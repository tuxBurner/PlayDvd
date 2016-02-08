package models;

import helpers.SelectAjaxContainer;

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

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Model;

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

      final String trimedFormAttr = StringUtils.trim(formAttr);

      if (StringUtils.isEmpty(trimedFormAttr) == true) {
        continue;
      }

      MovieAttribute movieAttributeToAdd = null;

      for (final MovieAttribute dbAttribute : dbAttributes) {
        if (dbAttribute.value.equals(trimedFormAttr) == true) {
          movieAttributeToAdd = dbAttribute;
          break;
        }
      }

      // attr does not exists in the db ? we will create it
      if (movieAttributeToAdd == null) {
        movieAttributeToAdd = MovieAttribute.createAttribute(type, trimedFormAttr);
      }

      attributes.add(movieAttributeToAdd);
    }

    return attributes;
  }

  /**
   * Gets the avaible {@link MovieAttribute}s as string for the tagit element
   * 
   * @param attributeType
   * @return
   * @deprecated is way to slow in the frontend when having 1000 of those we
   *             will use ajax here
   */
  @Deprecated
  public static String getAvaibleAttributesAsJson(final EMovieAttributeType attributeType) {

    final List<MovieAttribute> attributes = MovieAttribute.getAllByType(attributeType);
    final List<String> attrMap = new ArrayList<String>();
    for (final MovieAttribute dvdAttibute : attributes) {
      if (StringUtils.isEmpty(dvdAttibute.value)) {
        continue;
      }
      attrMap.add(dvdAttibute.value);
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(attrMap);

    return json;
  }

  /**
   * Searches the avaible {@link MovieAttribute}s as string for the select2
   * element
   * 
   * @param attributeType
   * @return
   * 
   */
  public static String searchAvaibleAttributesAsJson(final EMovieAttributeType attributeType, final String searchTerm) {

    // no searchTerm ?
    if (StringUtils.isEmpty(searchTerm) == true) {
      return StringUtils.EMPTY;
    }

    final List<MovieAttribute> attributes = MovieAttribute.finder.where().eq("attributeType", attributeType).istartsWith("value", searchTerm).order("value ASC").findList();
    final List<SelectAjaxContainer> retVal = new ArrayList<SelectAjaxContainer>();
    retVal.add(new SelectAjaxContainer(searchTerm, searchTerm));

    for (final MovieAttribute movieAttribute : attributes) {
      if (StringUtils.isEmpty(movieAttribute.value) == true || searchTerm.equals(movieAttribute.value) == true) {
        continue;
      }
      retVal.add(new SelectAjaxContainer(movieAttribute.value, movieAttribute.value));
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(retVal);

    return json;
  }
}
