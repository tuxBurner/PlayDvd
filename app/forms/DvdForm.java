package forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import models.Dvd;
import models.DvdAttibute;
import models.EDvdAttributeType;
import models.MovieAttibute;
import models.EMovieAttributeType;
import play.data.validation.Constraints.Required;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Form which handles all the inputs for adding, editing a dvd
 * 
 * @author tuxburner
 * 
 */
public class DvdForm {

  public Long dvdId;

  public Long movieId;

  @Required
  public String title;

  @Required
  public Integer year;

  public Integer runtime;

  public String plot;

  public Integer hullNr;

  public String posterUrl;

  public String backDropUrl;

  public List<String> genres = new ArrayList<String>();

  public List<String> actors = new ArrayList<String>();

  public String director;

  /**
   * This describes the box in which the DVD is in for example Arnolds Ultimate
   * Collectors Box
   */
  public String box;

  /**
   * This describes a collection of boxses for example
   */
  public String collection;

  /**
   * This describes in which series the movie is for example Alien, Terminator,
   * Indiana Jones are Series of movies
   */
  public String series;

  public Boolean hasBackdrop;

  public Boolean hasPoster;

  public String ownerName;

  /**
   * Transforms a {@link Dvd} to a {@link DvdForm} for editing the dvd in the
   * frontend
   * 
   * @param dvd
   * @return
   */
  public static DvdForm dvdToDvdForm(final Dvd dvd) {

    final DvdForm dvdForm = new DvdForm();

    dvdForm.movieId = dvd.movie.id;
    dvdForm.dvdId = dvd.id;
    dvdForm.title = dvd.movie.title;
    dvdForm.year = dvd.movie.year;
    dvdForm.runtime = dvd.movie.runtime;
    dvdForm.plot = dvd.movie.description;
    dvdForm.hullNr = dvd.hullNr;
    dvdForm.hasBackdrop = dvd.movie.hasBackdrop;
    dvdForm.hasPoster = dvd.movie.hasPoster;
    dvdForm.ownerName = dvd.owner.userName;

    final Set<MovieAttibute> attributes = dvd.movie.attributes;
    for (final MovieAttibute dvdAttibute : attributes) {
      if (EMovieAttributeType.GENRE.equals(dvdAttibute.attributeType)) {
        dvdForm.genres.add(dvdAttibute.value);
      }

      if (EMovieAttributeType.ACTOR.equals(dvdAttibute.attributeType)) {
        dvdForm.actors.add(dvdAttibute.value);
      }

      if (EMovieAttributeType.DIRECTOR.equals(dvdAttibute.attributeType)) {
        dvdForm.director = dvdAttibute.value;
      }
    }

    final Set<DvdAttibute> dvdAttrs = dvd.attributes;
    for (final DvdAttibute dvdAttibute : dvdAttrs) {
      if (EDvdAttributeType.BOX.equals(dvdAttibute.attributeType)) {
        dvdForm.box = dvdAttibute.value;
      }

      if (EDvdAttributeType.COLLECTION.equals(dvdAttibute.attributeType)) {
        dvdForm.collection = dvdAttibute.value;
      }
    }

    Collections.sort(dvdForm.actors);
    Collections.sort(dvdForm.genres);

    return dvdForm;
  }

  /**
   * Gathers all attributes for the given type from the db and checks if the dvd
   * 
   * @param attributeType
   * @return
   */
  public List<DvdFormAttribute> getDvdAttributes(final EMovieAttributeType attributeType, final Map<Integer, String> formVals) {

    final List<DvdFormAttribute> result = new ArrayList<DvdFormAttribute>();

    // merge wit the attributes from the database
    final List<MovieAttibute> genres = MovieAttibute.getAllByType(attributeType);
    final Set<String> newGenreMatchedWithDb = new HashSet<String>();
    for (final MovieAttibute dvdAttibute : genres) {

      final String value = dvdAttibute.value;
      boolean selected = false;
      for (final String dvdGenre : formVals.values()) {
        if (value.equals(dvdGenre)) {
          newGenreMatchedWithDb.add(dvdGenre);
          selected = true;
          break;
        }
      }

      result.add(new DvdFormAttribute(selected, value));
    }

    // add all attributes which where not in the database :)
    for (final String genre : formVals.values()) {
      if (newGenreMatchedWithDb.contains(genre) == false) {
        result.add(new DvdFormAttribute(true, genre));
      }
    }

    return result;
  }

  /**
   * Gets the avaible {@link MovieAttibute}s as string for the tagit element
   * 
   * @param attributeType
   * @return
   */
  public final String getAvaibleAttributesAsJson(final EMovieAttributeType attributeType) {

    final List<MovieAttibute> attributes = MovieAttibute.getAllByType(attributeType);
    final List<String> attrMap = new ArrayList<String>();
    for (final MovieAttibute dvdAttibute : attributes) {
      attrMap.add(dvdAttibute.value);
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(attrMap);

    return json;
  }

  /**
   * Gets all {@link MovieAttibute}s from the given {@link EMovieAttributeType}
   * 
   * @param attributeType
   * @return
   */
  public final List<String> getAvaibleAttributes(final EMovieAttributeType attributeType) {
    final List<MovieAttibute> attributes = MovieAttibute.getAllByType(attributeType);

    final List<String> list = new ArrayList<String>();
    list.add("");
    for (final MovieAttibute dvdAttibute : attributes) {
      list.add(dvdAttibute.value);
    }

    return list;
  }

  /**
   * This returns the values as a , seperated string
   * 
   * @param values
   * @return
   */
  public final String getDvdFormAttributesAsString(final List<String> values) {
    String returnVal = "";

    String sep = "";
    for (final String value : values) {
      returnVal += sep + value;
      sep = ",";
    }

    return returnVal;
  }

}
