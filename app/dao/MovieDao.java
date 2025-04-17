package dao;

import forms.MovieForm;
import forms.dvd.CopyForm;
import grabbers.EGrabberType;
import grabbers.ImdbRatingGrabber;
import helpers.EImageType;
import helpers.ImageHelper;
import io.ebean.Expr;
import io.ebean.Query;
import models.*;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.db.ebean.Transactional;
import scala.concurrent.duration.FiniteDuration;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MovieDao {

  /**
   * This creates a movie from the information of the given {@link CopyForm}
   *
   * @param movieForm    the form to create the data from
   * @param updateImages if to update the images or not
   * @return the update/created Movie
   * @throws Exception when an error happened
   */
  public static Movie editOrAddFromForm(final MovieForm movieForm, final boolean updateImages) throws Exception
  {


    final ImdbRatingGrabber imdbRatingGrabber = new ImdbRatingGrabber();
    imdbRatingGrabber.grabImdbRating(movieForm);

    Movie movie = null;

    if (movieForm.movieId != null) {
      movie = Movie.FINDER.byId(movieForm.movieId);
      if (movie == null) {
        final String message = "No Movie by the id: " + movieForm.movieId + " found !";
        Logger.error(message);
        throw new Exception(message);
      }
    }

    if (movie == null) {
      movie = new Movie();
    }

    movie.setTitle(movieForm.title);
    movie.setDescription(movieForm.plot);
    movie.setYear(movieForm.year);
    movie.setRuntime(movieForm.runtime);

    if ((movie.getId() != null && StringUtils.isBlank(movieForm.trailerUrl) == false) || movie.getId() == null) {
      movie.setTrailerUrl(movieForm.trailerUrl);
    }

    movie.setHasToBeReviewed(false);
    movie.setImdbId(movieForm.imdbId);
    movie.setImdbRating(movieForm.imdbRating);
    if (movieForm.grabberType != null && movieForm.grabberType != EGrabberType.NONE && StringUtils.isEmpty(movieForm.grabberId) == false) {
      movie.setGrabberType(movieForm.grabberType);
      movie.setGrabberId(movieForm.grabberId);
    } else {
      movie.setGrabberType(EGrabberType.NONE);
    }

    if (movie.getId() == null) {
      movie.setHasPoster(false);
      movie.setHasBackdrop(false);
      movie.save();
    } else {
      movie.getAttributes().clear();
      movie.update();
    }

    // add the images if we have some :)
    if (updateImages == true) {
      final Boolean newPoster = ImageHelper.createFileFromUrl(movie.getId(), movieForm.posterUrl, EImageType.POSTER);
      if (movie.getHasPoster() == null || movie.getHasPoster() == false) {
        movie.setHasPoster(newPoster);
      }

      final Boolean newBackDrop = ImageHelper.createFileFromUrl(movie.getId(), movieForm.backDropUrl, EImageType.BACKDROP);
      if (movie.getHasBackdrop() == null || movie.getHasBackdrop() == false) {
        movie.setHasBackdrop(newBackDrop);
      }
    }

    movie.setAttributes(new HashSet<>());

    // gather all the genres and add them to the dvd
    final Set<MovieAttribute> genres = MovieAttributeDao.gatherAndAddAttributes(new HashSet<>(movieForm.genres), EMovieAttributeType.GENRE);
    movie.getAttributes().addAll(genres);

    final Set<MovieAttribute> actors = MovieAttributeDao.gatherAndAddAttributes(new HashSet<>(movieForm.actors), EMovieAttributeType.ACTOR);
    movie.getAttributes().addAll(actors);

    MovieDao.addSingleAttribute(movieForm.series, EMovieAttributeType.MOVIE_SERIES, movie);

    MovieDao.addSingleAttribute(movieForm.director, EMovieAttributeType.DIRECTOR, movie);

    movie.setUpdatedDate(new Date().getTime());

    movie.update();

    return movie;
  }

  /**
   * Adds a single Attribute to the dvd
   *
   * @param attrToAdd
   * @param attributeType
   * @param movie
   */
  private static void addSingleAttribute(final String attrToAdd, final EMovieAttributeType attributeType, final Movie movie)
  {
    if (StringUtils.isEmpty(attrToAdd) == true) {
      return;
    }
    final Set<String> attribute = new HashSet<String>();
    attribute.add(attrToAdd);
    final Set<MovieAttribute> dbAttrs = MovieAttributeDao.gatherAndAddAttributes(attribute, attributeType);
    movie.getAttributes().addAll(dbAttrs);
  }

  /**
   * Searches all {@link Movie}s by the term and returns a list of the result
   *
   * @param term
   * @param numberOfResults
   * @return
   */
  @Transactional
  public static List<Movie> searchLike(final String term, final int numberOfResults)
  {
    final Query<Movie> order = Movie.FINDER.query().where()
        .ilike("title", "%" + term + "%").select("id ,title, hasPoster")
        .order("title asc");
    if (numberOfResults <= 0) {
      return order.findList();
    } else {
      return order
          .setFirstRow(0)
          .setMaxRows(numberOfResults)
          .findPagedList()
          .getList();
    }
  }

  /**
   * Searches for {@link Movie}s which have the same title, or the where the {@link Dvd} has the same ean nr
   *
   * @param term
   * @param eanNr
   * @return
   */
  public static List<Movie> searchLikeAndAmazoneCode(final String term, final String eanNr)
  {
    final List<Movie> movies = searchLike(term, 0);

    final List<Dvd> dvds = Dvd.FINDER.query().where()
        .eq("eanNr", eanNr)
        .findList();
    for (Dvd dvd : dvds) {
      final Long movieId = dvd.movie.getId();
      boolean foundMovie = false;
      for (Movie movie : movies) {
        if (movie.getId().equals(movieId) == true) {
          foundMovie = true;
          break;
        }
      }
      if (foundMovie == false) {
        movies.add(dvd.movie);
      }
    }

    return movies;

  }

  /**
   * Checks if a movie already exists with the grabberId and the grabberType
   *
   * @param grabberId
   * @param grabberType
   * @return
   */
  public static boolean checkIfMovieWasGrabbedBefore(final String grabberId, final EGrabberType grabberType)
  {
    if (StringUtils.isEmpty(grabberId) == true || EGrabberType.NONE.equals(grabberType) == true) {
      return false;
    }

    int rowCount = Movie.FINDER.query().where()
        .eq("grabberId", grabberId)
        .eq("grabberType", grabberType)
        .findCount();
    return rowCount > 0;
  }

  /**
   * Finds all the movies which are older than the given Duration
   *
   * @return
   */
  public static List<Movie> findMoviesToUpdate(final FiniteDuration duration, final int amount)
  {

    final Long olderThan = new Date().getTime() - duration.toMillis();

    List<Movie> list = Movie.FINDER.query()
        .where()
        .or(Expr.lt("updatedDate", olderThan), Expr.isNull("updatedDate"))
        .isNotNull("grabberType")
        .isNotNull("grabberId")
        .setFirstRow(0)
        .setMaxRows(amount)
        .findPagedList().getList();

    return list;
  }

  /**
   * Adds a  {@link Comment} to the {@link Movie} with the given id
   *
   * @param movieId
   * @param commentText
   */
  public static Commentable addComment(final Long movieId, final String commentText)
  {
    return null;

  }

}
