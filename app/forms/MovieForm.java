package forms;

import grabbers.EGrabberType;
import models.EMovieAttributeType;
import models.Movie;
import models.MovieAttribute;
import org.apache.commons.lang3.StringUtils;
import play.data.validation.Constraints.Required;

import java.util.*;

/**
 * This is used when the user adds or edits a new movie
 *
 * @author tuxburner
 */
public class MovieForm {

  public Long movieId;

  @Required
  public String title;

  @Required
  public Integer year;

  public Integer runtime;

  public String plot;

  public String posterUrl;

  public String backDropUrl;

  public List<String> genres = new ArrayList<String>();

  public List<String> actors = new ArrayList<String>();

  public String director;

  public String trailerUrl;

  public String grabberId;

  public EGrabberType grabberType = EGrabberType.NONE;

  public String imdbId;

  public String imdbRating;

  /**
   * This describes in which series the movie is for example Alien, Terminator,
   * Indiana Jones are Series of movies
   */
  public String series;

  public Boolean hasBackdrop;

  public Boolean hasPoster;

  /**
   * Transforms a {@link Movie} to a {@link MovieForm} for editing the dvd in
   * the frontend
   *
   * @param movie
   * @return
   */
  public static MovieForm movieToForm(final Movie movie) {
    final MovieForm movieForm = new MovieForm();

    movieForm.movieId = movie.getId();
    movieForm.title = movie.getTitle();
    movieForm.year = movie.getYear();
    movieForm.runtime = movie.getRuntime();
    movieForm.plot = movie.getDescription();
    movieForm.trailerUrl = movie.getTrailerUrl();

    movieForm.hasBackdrop = movie.getHasBackdrop();
    movieForm.hasPoster = movie.getHasPoster();
    movieForm.imdbId = movie.getImdbId();
    movieForm.imdbRating = movie.getImdbRating();
    if (EGrabberType.NONE.equals(movie.getGrabberType()) == false && StringUtils.isEmpty(movie.getGrabberId()) == false) {
      movieForm.grabberType = movie.getGrabberType();
      movieForm.grabberId = movie.getGrabberId();
    }

    final Set<String> genreSet = new HashSet<String>();
    final Set<String> actorSet = new HashSet<String>();

    final Set<MovieAttribute> attributes = movie.getAttributes();
    for (final MovieAttribute movieAttibute : attributes) {
      if (EMovieAttributeType.GENRE.equals(movieAttibute.getAttributeType())) {
        genreSet.add(movieAttibute.getValue().trim());
      }

      if (EMovieAttributeType.ACTOR.equals(movieAttibute.getAttributeType())) {
        actorSet.add(movieAttibute.getValue());
      }

      if (EMovieAttributeType.DIRECTOR.equals(movieAttibute.getAttributeType())) {
        movieForm.director = movieAttibute.getValue();
      }

      if (EMovieAttributeType.MOVIE_SERIES.equals(movieAttibute.getAttributeType())) {
        movieForm.series = movieAttibute.getValue();
      }
    }


    movieForm.actors.addAll(actorSet);
    Collections.sort(movieForm.actors);

    movieForm.genres.addAll(genreSet);
    Collections.sort(movieForm.genres);

    return movieForm;
  }

  /**
   * This returns the values as a , seperated string
   *
   * @param values
   * @return
   */
  public static String getDvdFormAttributesAsString(final Optional<String> values) {
    if (values.isEmpty()) {
      return "";
    }
    return StringUtils.replace(StringUtils.replaceChars(values.get(), "[]", ""), ", ", ",");
  }

  public Long getMovieId() {
    return movieId;
  }

  public void setMovieId(Long movieId) {
    this.movieId = movieId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public String getPlot() {
    return plot;
  }

  public void setPlot(String plot) {
    this.plot = plot;
  }

  public String getPosterUrl() {
    return posterUrl;
  }

  public void setPosterUrl(String posterUrl) {
    this.posterUrl = posterUrl;
  }

  public String getBackDropUrl() {
    return backDropUrl;
  }

  public void setBackDropUrl(String backDropUrl) {
    this.backDropUrl = backDropUrl;
  }

  public List<String> getGenres() {
    return genres;
  }

  public void setGenres(List<String> genres) {
    this.genres = genres;
  }

  public List<String> getActors() {
    return actors;
  }

  public void setActors(List<String> actors) {
    this.actors = actors;
  }

  public String getDirector() {
    return director;
  }

  public void setDirector(String director) {
    this.director = director;
  }

  public String getTrailerUrl() {
    return trailerUrl;
  }

  public void setTrailerUrl(String trailerUrl) {
    this.trailerUrl = trailerUrl;
  }

  public String getGrabberId() {
    return grabberId;
  }

  public void setGrabberId(String grabberId) {
    this.grabberId = grabberId;
  }

  public EGrabberType getGrabberType() {
    return grabberType;
  }

  public void setGrabberType(EGrabberType grabberType) {
    this.grabberType = grabberType;
  }

  public String getImdbId() {
    return imdbId;
  }

  public void setImdbId(String imdbId) {
    this.imdbId = imdbId;
  }

  public String getSeries() {
    return series;
  }

  public void setSeries(String series) {
    this.series = series;
  }

  public Boolean getHasBackdrop() {
    return hasBackdrop;
  }

  public void setHasBackdrop(Boolean hasBackdrop) {
    this.hasBackdrop = hasBackdrop;
  }

  public Boolean getHasPoster() {
    return hasPoster;
  }

  public void setHasPoster(Boolean hasPoster) {
    this.hasPoster = hasPoster;
  }
}
