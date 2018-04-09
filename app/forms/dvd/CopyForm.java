package forms.dvd;

import com.google.gson.Gson;
import controllers.MovieSelect2Value;
import grabbers.amazon.AmazonResult;
import models.Dvd;
import models.DvdAttribute;
import models.Movie;
import org.apache.commons.lang.StringUtils;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Form which handles all the inputs for adding, editing a copy.
 *
 * @author tuxburner
 */
public class CopyForm {

  public Long dvdId;

  /**
   * The id if the movie the dvd references to
   */
  @Required(message = "msg.error.noMovieSelected")
  public Long movieId;

  /**
   * This describes the box in which the DVD is in for example Arnolds Ultimate
   * Collectors Box
   */
  public String box;

  /**
   * This describes a collection of boxses for example
   */
  public String collection;

  public String ownerName;

  /**
   * Boolean if the owner has a gravatar or not.
   */
  public Boolean ownerHasGravatar;

  public String ageRating;

  public String copyType;

  public Integer hullNr;

  public String eanNr;

  public String asinNr;

  /**
   * Additional info off the copy like directors cut
   */
  @Constraints.MaxLength(value = 255)
  public String additionalInfo;

  public List<String> audioTypes = new ArrayList<String>();

  /**
   * Transforms an {@link AmazonResult} and movieId to a dvdForm
   *
   * @param amazonResult
   * @param movieId
   * @param copy if not null the form will be filled with the informations from the copy
   */
  public static CopyForm amazonAndMovieToDvdForm(final AmazonResult amazonResult, final Long movieId, final Dvd copy) {

    CopyForm copyForm = amazonAndCopyToForm(copy, amazonResult);
    copyForm.movieId = movieId;

    return copyForm;
  }

  /**
   * Creates a dvdform from the informations from the copy and the amazonresult
   * if the copy is null it will be an empty form with just the amazon results
   * @param copy
   * @param amazonResult
   * @return
   */
  public static CopyForm amazonAndCopyToForm(final Dvd copy, final AmazonResult amazonResult) {
    final CopyForm copyForm = (copy == null) ? new CopyForm() : dvdToDvdForm(copy);

    copyForm.ageRating = amazonResult.rating;
    copyForm.copyType = amazonResult.copyType;
    copyForm.audioTypes = amazonResult.audioTypes;
    copyForm.asinNr = amazonResult.asin;
    copyForm.eanNr = amazonResult.ean;
    if(copy != null && copy.movie != null) {
      copyForm.movieId = copy.movie.id;
    }

    Collections.sort(copyForm.audioTypes);

    return copyForm;
  }


  /**
   * Transforms a {@link Dvd} to a {@link CopyForm} for editing the copy in the
   * frontend
   *
   * @param copy
   * @return
   */
  public static CopyForm dvdToDvdForm(final Dvd copy) {

    final CopyForm copyForm = new CopyForm();

    copyForm.movieId = copy.movie.id;
    copyForm.dvdId = copy.id;
    copyForm.ownerName = copy.owner.userName;
    copyForm.hullNr = copy.hullNr;
    copyForm.eanNr = copy.eanNr;
    copyForm.asinNr = copy.asinNr;
    copyForm.additionalInfo = copy.additionalInfo;
    copyForm.ownerHasGravatar = copy.owner.hasGravatar;


    final Set<DvdAttribute> dvdAttrs = copy.attributes;
    for (final DvdAttribute dvdAttribute : dvdAttrs) {

      switch (dvdAttribute.attributeType) {
        case BOX:
          copyForm.box = dvdAttribute.value;
          break;
        case COLLECTION:
          copyForm.collection = dvdAttribute.value;
          break;
        case RATING:
          copyForm.ageRating = dvdAttribute.value;
          break;
        case COPY_TYPE:
          copyForm.copyType = dvdAttribute.value;
          break;
        case AUDIO_TYPE:
          copyForm.audioTypes.add(dvdAttribute.value);
          break;
      }

      Collections.sort(copyForm.audioTypes);

    }

    return copyForm;
  }


  /**
   * This returns the selected {@link Movie} for the {@link Dvd} as a json
   * representation
   *
   * @param movieId
   * @return
   */
  public static String getSelectedMovieAsJson(final String movieId) {

    final Gson gson = new Gson();
    if (StringUtils.isEmpty(movieId) == true) {
      return gson.toJson(null);
    }

    final Movie byId = Movie.FINDER.query()
      .select("id, hasPoster,title")
      .where().eq("id", movieId)
      .findOne();

    return gson.toJson(new MovieSelect2Value(byId));
  }

  public Long getDvdId() {
    return dvdId;
  }

  public void setDvdId(Long dvdId) {
    this.dvdId = dvdId;
  }

  public Long getMovieId() {
    return movieId;
  }

  public void setMovieId(Long movieId) {
    this.movieId = movieId;
  }

  public String getBox() {
    return box;
  }

  public void setBox(String box) {
    this.box = box;
  }

  public String getCollection() {
    return collection;
  }

  public void setCollection(String collection) {
    this.collection = collection;
  }

  public String getOwnerName() {
    return ownerName;
  }

  public void setOwnerName(String ownerName) {
    this.ownerName = ownerName;
  }

  public String getAgeRating() {
    return ageRating;
  }

  public void setAgeRating(String ageRating) {
    this.ageRating = ageRating;
  }

  public String getCopyType() {
    return copyType;
  }

  public void setCopyType(String copyType) {
    this.copyType = copyType;
  }

  public Integer getHullNr() {
    return hullNr;
  }

  public void setHullNr(Integer hullNr) {
    this.hullNr = hullNr;
  }

  public String getEanNr() {
    return eanNr;
  }

  public void setEanNr(String eanNr) {
    this.eanNr = eanNr;
  }

  public String getAsinNr() {
    return asinNr;
  }

  public void setAsinNr(String asinNr) {
    this.asinNr = asinNr;
  }

  public String getAdditionalInfo() {
    return additionalInfo;
  }

  public void setAdditionalInfo(String additionalInfo) {
    this.additionalInfo = additionalInfo;
  }

  public List<String> getAudioTypes() {
    return audioTypes;
  }

  public void setAudioTypes(List<String> audioTypes) {
    this.audioTypes = audioTypes;
  }
}
