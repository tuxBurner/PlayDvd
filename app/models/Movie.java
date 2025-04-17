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
  public Long id;

  @Required
  public String title;

  public Boolean hasPoster;

  public Boolean hasBackdrop;

  /**
   * If true the movie has to be reviewed is for mass imports etc importand
   */
  @Column(nullable = false)
  public Boolean hasToBeReviewed = false;

  public String description;

  @Required
  @Column(nullable = false)
  public Integer year;

  public Integer runtime;

  @ManyToMany(cascade = CascadeType.MERGE, mappedBy = "movies")
  public Set<MovieAttribute> attributes;

  @OneToMany(mappedBy = "movie")
  public Set<Dvd> dvds;

  public String trailerUrl;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  public EGrabberType grabberType = EGrabberType.NONE;

  /**
   * Marks the date when the movie was las update/created
   */
  public Long updatedDate;

  /**
   * Id to the imdb
   */
  public String imdbId;

  /**
   * Rating of imdb for this movie
   */
  public String imdbRating;

  /**
   * If the EGrabberType is not null this is the id which is to use to FINDER the movie via the grabber
   */
  public String grabberId;




}
