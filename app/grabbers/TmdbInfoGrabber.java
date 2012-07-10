package grabbers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import com.github.savvasdalkitsis.jtmdb.CastInfo;
import com.github.savvasdalkitsis.jtmdb.GeneralSettings;
import com.github.savvasdalkitsis.jtmdb.Genre;
import com.github.savvasdalkitsis.jtmdb.Movie;
import com.github.savvasdalkitsis.jtmdb.MovieBackdrop;
import com.github.savvasdalkitsis.jtmdb.MoviePoster;

import forms.MovieForm;
import forms.GrabberInfoForm;

public class TmdbInfoGrabber {

  public static List<Movie> searchForMovie(final String movieName) throws GrabberException {

    TmdbInfoGrabber.prepareSettings();
    try {
      final List<Movie> search = Movie.search(movieName);
      return search;
    } catch (final IOException e) {
      throw new GrabberException(e);
    } catch (final JSONException e) {
      throw new GrabberException(e);
    }
  }

  /**
   * Gets the movie for the given id
   * 
   * @param tmdbId
   * @return
   * @throws GrabberException
   */
  public static Movie getMovieInfo(final Integer tmdbId) throws GrabberException {
    TmdbInfoGrabber.prepareSettings();
    try {
      return Movie.getInfo(tmdbId);
    } catch (final IOException e) {
      throw new GrabberException(e);
    } catch (final JSONException e) {
      throw new GrabberException(e);
    }

  }

  public static MovieForm fillDvdFormWithMovieInfo(final GrabberInfoForm tmdbInfoForm) throws GrabberException {

    final Movie movieInfo = TmdbInfoGrabber.getMovieInfo(Integer.valueOf(tmdbInfoForm.grabberMovieId));

    final MovieForm movieForm = new MovieForm();
    movieForm.title = movieInfo.getName();
    movieForm.plot = movieInfo.getOverview();
    movieForm.runtime = movieInfo.getRuntime();

    final Date releasedDate = movieInfo.getReleasedDate();
    final Calendar releaseCal = Calendar.getInstance();
    releaseCal.setTime(releasedDate);
    movieForm.year = releaseCal.get(Calendar.YEAR);

    final Set<Genre> genres = movieInfo.getGenres();
    for (final Genre genre : genres) {
      movieForm.genres.add(genre.getName());
    }

    final Set<CastInfo> cast = movieInfo.getCast();
    for (final CastInfo castInfo : cast) {
      if ("Director".equals(castInfo.getJob())) {
        movieForm.director = castInfo.getName();
        continue;
      }

      if ("Actor".equals(castInfo.getJob())) {
        movieForm.actors.add(castInfo.getName());

        continue;
      }
    }

    final String tmdbBackDrop = tmdbInfoForm.grabberBackDropId;
    if (StringUtils.isEmpty(tmdbBackDrop) == false) {
      final Set<MovieBackdrop> backdrops = movieInfo.getImages().backdrops;
      for (final MovieBackdrop movieBackdrop : backdrops) {
        if (movieBackdrop.getID().equals(tmdbBackDrop) == true) {
          movieForm.backDropUrl = movieBackdrop.getLargestImage().toString();
          break;
        }
      }
    }

    final String tmdbPoster = tmdbInfoForm.grabberPosterId;
    if (StringUtils.isEmpty(tmdbPoster) == false) {
      final Set<MoviePoster> posters = movieInfo.getImages().posters;
      for (final MoviePoster poster : posters) {
        if (poster.getID().equals(tmdbPoster) == true) {
          movieForm.posterUrl = poster.getLargestImage().toString();
          break;
        }
      }
    }

    return movieForm;

  }

  /**
   * Needed in the displaymovie.scala.html view for tmdb
   * 
   * @param set
   * @return
   */
  public static List<MoviePoster> posterSetToList(final Set<MoviePoster> set) {
    final List<MoviePoster> retVal = new ArrayList<MoviePoster>(set);
    return retVal;
  }

  /**
   * Needed in the displaymovie.scala.html view for tmdb
   * 
   * @param set
   * @return
   */
  public static List<MovieBackdrop> backdropSetToList(final Set<MovieBackdrop> set) {
    final List<MovieBackdrop> retVal = new ArrayList<MovieBackdrop>(set);
    return retVal;
  }

  private static void prepareSettings() {
    GeneralSettings.setApiKey("a67216a4ad62ec0f81e3fffbfe18507f");
    GeneralSettings.setAPILocale(Locale.GERMANY);
  }

}
