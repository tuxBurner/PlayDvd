package forms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import models.Dvd;
import models.DvdAttibute;
import models.EAttributeType;
import play.data.validation.Constraints.Required;

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

  public String box;

  public String collection;

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

    final Set<DvdAttibute> attributes = dvd.movie.attributes;
    for (final DvdAttibute dvdAttibute : attributes) {
      if (EAttributeType.GENRE.equals(dvdAttibute.attributeType)) {
        dvdForm.genres.add(dvdAttibute.value);
      }

      if (EAttributeType.ACTOR.equals(dvdAttibute.attributeType)) {
        dvdForm.actors.add(dvdAttibute.value);
      }

      if (EAttributeType.DIRECTOR.equals(dvdAttibute.attributeType)) {
        dvdForm.director = dvdAttibute.value;
      }

      if (EAttributeType.BOX.equals(dvdAttibute.attributeType)) {
        dvdForm.box = dvdAttibute.value;
      }

      if (EAttributeType.COLLECTION.equals(dvdAttibute.attributeType)) {
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
  public List<DvdFormAttribute> getDvdAttributes(final EAttributeType attributeType, final Map<Integer, String> formVals) {

    final List<DvdFormAttribute> result = new ArrayList<DvdFormAttribute>();

    // merge wit the attributes from the database
    final List<DvdAttibute> genres = DvdAttibute.getAllByType(attributeType);
    final Set<String> newGenreMatchedWithDb = new HashSet<String>();
    for (final DvdAttibute dvdAttibute : genres) {

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
   * Gets the avaible {@link DvdAttibute}s as string for the tagit element
   * 
   * @param attributeType
   * @return
   */
  public final String getAvaibleAttributesAsJson(final EAttributeType attributeType) {

    final List<DvdAttibute> attributes = DvdAttibute.getAllByType(attributeType);
    final List<String> attrMap = new ArrayList<String>();
    for (final DvdAttibute dvdAttibute : attributes) {
      attrMap.add(dvdAttibute.value);
    }

    final Gson gson = new GsonBuilder().create();
    final String json = gson.toJson(attrMap);

    return json;
  }

  /**
   * Gets all {@link DvdAttibute}s from the given {@link EAttributeType}
   * 
   * @param attributeType
   * @return
   */
  public final List<String> getAvaibleAttributes(final EAttributeType attributeType) {
    final List<DvdAttibute> attributes = DvdAttibute.getAllByType(attributeType);

    final List<String> list = new ArrayList<String>();
    list.add("");
    for (final DvdAttibute dvdAttibute : attributes) {
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
