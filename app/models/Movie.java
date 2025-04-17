package models;


import dao.MovieAttributeDao;
import forms.MovieForm;
import forms.dvd.CopyForm;
import grabbers.EGrabberType;
import grabbers.ImdbRatingGrabber;
import helpers.EImageType;
import helpers.ImageHelper;
import io.ebean.Expr;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.Query;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Transactional;
import scala.concurrent.duration.FiniteDuration;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Movie extends Model
{

  /**
   * The FINDER for the database for searching in the database
   */
  public static final Finder<Long, Movie> FINDER = new Finder<>(Movie.class);

  @Id
  private Long id;

  @Required
  private String title;

  private Boolean hasPoster;

  private Boolean hasBackdrop;

  /**
   * If true the movie has to be reviewed is for mass imports etc importand
   */
  @Column(nullable = false)
  private Boolean hasToBeReviewed = false;

  private String description;

  @Required
  @Column(nullable = false)
  private Integer year;

  private Integer runtime;

  @ManyToMany(cascade = CascadeType.MERGE, mappedBy = "movies")
  private Set<MovieAttribute> attributes;

  @OneToMany(mappedBy = "movie")
  private Set<Dvd> dvds;

  private String trailerUrl;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EGrabberType grabberType = EGrabberType.NONE;

  /**
   * Marks the date when the movie was las update/created
   */
  private Long updatedDate;

  /**
   * Id to the imdb
   */
  private String imdbId;

  /**
   * Rating of imdb for this movie
   */
  private String imdbRating;

  /**
   * If the EGrabberType is not null this is the id which is to use to FINDER the movie via the grabber
   */
  private String grabberId;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Boolean getHasPoster() {
    return hasPoster;
  }

  public void setHasPoster(Boolean hasPoster) {
    this.hasPoster = hasPoster;
  }

  public Boolean getHasBackdrop() {
    return hasBackdrop;
  }

  public void setHasBackdrop(Boolean hasBackdrop) {
    this.hasBackdrop = hasBackdrop;
  }

  public Boolean getHasToBeReviewed() {
    return hasToBeReviewed;
  }

  public void setHasToBeReviewed(Boolean hasToBeReviewed) {
    this.hasToBeReviewed = hasToBeReviewed;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Integer getYear() {
    return year;
  }

  public void setYear(Integer year) {
    this.year = year;
  }

  public Integer getRuntime() {
    return runtime;
  }

  public void setRuntime(Integer runtime) {
    this.runtime = runtime;
  }

  public Set<MovieAttribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<MovieAttribute> attributes) {
    this.attributes = attributes;
  }

  public Set<Dvd> getDvds() {
    return dvds;
  }

  public void setDvds(Set<Dvd> dvds) {
    this.dvds = dvds;
  }

  public String getTrailerUrl() {
    return trailerUrl;
  }

  public void setTrailerUrl(String trailerUrl) {
    this.trailerUrl = trailerUrl;
  }

  public EGrabberType getGrabberType() {
    return grabberType;
  }

  public void setGrabberType(EGrabberType grabberType) {
    this.grabberType = grabberType;
  }

  public Long getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Long updatedDate) {
    this.updatedDate = updatedDate;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public String getImdbRating() {
    return imdbRating;
  }

  public void setImdbRating(String imdbRating) {
    this.imdbRating = imdbRating;
  }

  public String getGrabberId() {
    return grabberId;
  }

  public void setGrabberId(String grabberId) {
    this.grabberId = grabberId;
  }
}
