package models;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

import com.avaje.ebean.Ebean;

import forms.DvdForm;
import forms.MovieForm;

@Entity
public class Movie extends Model {

  private static final long serialVersionUID = -2107177668174396511L;

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

  @Lob
  public String description;

  @Required
  @Column(nullable = false)
  public Integer year;

  public Integer runtime;

  @ManyToMany(cascade = CascadeType.MERGE, fetch = FetchType.LAZY, mappedBy = "movies")
  public Set<MovieAttibute> attributes;

  @OneToMany(fetch = FetchType.LAZY, mappedBy = "movie", orphanRemoval = true)
  public Set<Dvd> dvds;

  /**
   * The finder for the database for searching in the database
   */
  public static Finder<Long, Movie> find = new Finder<Long, Movie>(Long.class, Movie.class);

  /**
   * This creates a movie from the information of the given {@link DvdForm}
   * 
   * @param dvdForm
   * @param movie
   * @return
   * @throws Exception
   */
  public static Movie editOrAddFromForm(final MovieForm movieForm) throws Exception {

    Movie movie = null;

    if (movieForm.movieId != null) {
      movie = Movie.find.byId(movieForm.movieId);
      if (movie == null) {
        final String message = "No Movie by the id: " + movieForm.movieId + " found !";
        Logger.error(message);
        throw new Exception(message);
      }
    }

    if (movie == null) {
      movie = new Movie();
    }

    movie.title = movieForm.title;
    movie.description = movieForm.plot;
    movie.year = movieForm.year;
    movie.runtime = movieForm.runtime;

    if (movie.id == null) {
      movie.hasPoster = false;
      movie.hasBackdrop = false;
      movie.save();
    } else {
      Ebean.deleteManyToManyAssociations(movie, "attributes");
    }

    // add the images if we have some :)
    final Boolean newPoster = ImageHelper.createFileFromUrl(movie.id, movieForm.posterUrl, EImageType.POSTER, EImageSize.ORIGINAL);
    if (movie.hasPoster == false || movie.hasPoster == null) {
      movie.hasPoster = newPoster;
    }

    final Boolean newBackDrop = ImageHelper.createFileFromUrl(movie.id, movieForm.backDropUrl, EImageType.BACKDROP, EImageSize.ORIGINAL);
    if (movie.hasBackdrop == false || movie.hasBackdrop == null) {
      movie.hasBackdrop = newBackDrop;
    }

    movie.attributes = new HashSet<MovieAttibute>();

    // gather all the genres and add them to the dvd
    final Set<MovieAttibute> genres = MovieAttibute.gatherAndAddAttributes(new HashSet<String>(movieForm.genres), EMovieAttributeType.GENRE);
    movie.attributes.addAll(genres);

    final Set<MovieAttibute> actors = MovieAttibute.gatherAndAddAttributes(new HashSet<String>(movieForm.actors), EMovieAttributeType.ACTOR);
    movie.attributes.addAll(actors);

    Movie.addSingleAttribute(movieForm.director, EMovieAttributeType.DIRECTOR, movie);

    movie.update();

    return movie;
  }

  /**
   * Adds a single Attribute to the dvd
   * 
   * @param attrToAdd
   * @param attributeType
   * @param dvd
   */
  private static void addSingleAttribute(final String attrToAdd, final EMovieAttributeType attributeType, final Movie movie) {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<String>();
    attribute.add(attrToAdd);
    final Set<MovieAttibute> dbAttrs = MovieAttibute.gatherAndAddAttributes(attribute, attributeType);
    movie.attributes.addAll(dbAttrs);
  }

  /**
   * List all movies orderd by the title only fetching id and title
   * 
   * @return
   */
  public static List<Movie> listByDistinctTitle() {
    return Movie.find.select("id,title").order("title asc").findList();
  }

}
