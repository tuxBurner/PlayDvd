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
  public EMovieAttributeType attributeType;

  public String value;

  @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  public Set<Movie> movies;

  /**
   * finds all {@link MovieAttibute} by the given values and the given
   * {@link EMovieAttributeType}
   * 
   * @return
   */
  public static Set<MovieAttibute> findAttributesByName(final Set<String> values, final EMovieAttributeType type) {
    final Set<MovieAttibute> findSet = MovieAttibute.finder.where().in("value", values).eq("attributeType", type).findSet();
    return findSet;
  }

  /**
   * Gets all attributes of the given type ordered by the value ASC
   * 
   * @param type
   * @return
   */
  public static List<MovieAttibute> getAllByType(final EMovieAttributeType type) {
    final List<MovieAttibute> findList = MovieAttibute.finder.where().eq("attributeType", type).order("value ASC").findList();
    return findList;
  }

  /**
   * Creates a {@link MovieAttibute} by the given {@link EMovieAttributeType}
   * and value
   * 
   * @param type
   * @param value
   * @return
   */
  public static MovieAttibute createAttribute(final EMovieAttributeType type, final String value) {
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
  public static Set<MovieAttibute> gatherAndAddAttributes(final Set<String> attributeValues, final EMovieAttributeType type) {

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

  /**
   * Gets the avaible {@link MovieAttibute}s as string for the tagit element
   * 
   * @param attributeType
   * @return
   */
  public static String getAvaibleAttributesAsJson(final EMovieAttributeType attributeType) {

    final List<MovieAttibute> attributes = MovieAttibute.getAllByType(attributeType);
    final List<String> attrMap = new ArrayList<String>();
    for (final MovieAttibute dvdAttibute : attributes) {
      attrMap.add(dvdAttibute.value);
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(attrMap);

    return json;
  }

}
