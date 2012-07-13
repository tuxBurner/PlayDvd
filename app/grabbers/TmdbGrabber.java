package grabbers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONException;

import com.github.savvasdalkitsis.jtmdb.CastInfo;
import com.github.savvasdalkitsis.jtmdb.GeneralSettings;
import com.github.savvasdalkitsis.jtmdb.Genre;
import com.github.savvasdalkitsis.jtmdb.Movie;
import com.github.savvasdalkitsis.jtmdb.MovieBackdrop;
import com.github.savvasdalkitsis.jtmdb.MovieImages;
import com.github.savvasdalkitsis.jtmdb.MoviePoster;

import forms.MovieForm;
import forms.GrabberInfoForm;

public class TmdbGrabber implements IInfoGrabber {

  private static final String API_KEY = "a67216a4ad62ec0f81e3fffbfe18507f";
  private final static EGrabberType TYPE = EGrabberType.TMDB;

  public TmdbGrabber() {
    GeneralSettings.setApiKey(TmdbGrabber.API_KEY);
    GeneralSettings.setAPILocale(Locale.GERMANY);
  }

  @Override
  public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException {
    try {

      final List<GrabberSearchMovie> returnVal = new ArrayList<GrabberSearchMovie>();

      final List<Movie> results = Movie.search(searchTerm);

      if (CollectionUtils.isEmpty(results) == false) {
        for (final Movie movie : results) {

          final MovieImages images = movie.getImages();

          String posterUrl = null;
          if (images != null && CollectionUtils.isEmpty(images.posters) == false) {
            posterUrl = images.posters.iterator().next().getSmallestImage().toString();
          }

          returnVal.add(new GrabberSearchMovie(String.valueOf(movie.getID()), movie.getName(), posterUrl, TmdbGrabber.TYPE));
        }
      }

      return returnVal;
    } catch (final IOException e) {
      throw new GrabberException(e);
    } catch (final JSONException e) {
      throw new GrabberException(e);
    }
  }

  @Override
  public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException {
    try {
      final Movie info = Movie.getInfo(Integer.valueOf(id));

      final MovieImages images = info.getImages();
      final List<GrabberImage> posters = new ArrayList<GrabberImage>();
      final List<GrabberImage> backdrops = new ArrayList<GrabberImage>();
      if (images != null) {

        if (CollectionUtils.isEmpty(images.posters) == false) {
          for (final MoviePoster poster : images.posters) {
            posters.add(new GrabberImage(poster.getID(), poster.getSmallestImage().toString()));
          }
        }

        if (CollectionUtils.isEmpty(images.backdrops) == false) {
          for (final MovieBackdrop backdrop : images.backdrops) {
            backdrops.add(new GrabberImage(backdrop.getID(), backdrop.getSmallestImage().toString()));
          }
        }

      }

      final GrabberDisplayMovie displayMovie = new GrabberDisplayMovie(id, info.getName(), info.getOverview(), posters, backdrops, TmdbGrabber.TYPE);

      return displayMovie;

    } catch (final IOException e) {
      throw new GrabberException(e);
    } catch (final JSONException e) {
      throw new GrabberException(e);
    }
  }

  @Override
  public MovieForm filleInfoToMovieForm(final GrabberInfoForm grabberInfoForm) throws GrabberException {

    try {
      final Movie movieInfo = Movie.getInfo(Integer.valueOf(grabberInfoForm.grabberMovieId));

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

      final String tmdbBackDrop = grabberInfoForm.grabberBackDropId;
      if (StringUtils.isEmpty(tmdbBackDrop) == false) {
        final Set<MovieBackdrop> backdrops = movieInfo.getImages().backdrops;
        for (final MovieBackdrop movieBackdrop : backdrops) {
          if (movieBackdrop.getID().equals(tmdbBackDrop) == true) {
            movieForm.backDropUrl = movieBackdrop.getLargestImage().toString();
            break;
          }
        }
      }

      final String tmdbPoster = grabberInfoForm.grabberPosterId;
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

    } catch (final IOException e) {
      throw new GrabberException(e);
    } catch (final JSONException e) {
      throw new GrabberException(e);
    }

  }

}
