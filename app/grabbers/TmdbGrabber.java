package grabbers;

import com.omertron.themoviedbapi.MovieDbException;
import com.omertron.themoviedbapi.TheMovieDbApi;
import com.omertron.themoviedbapi.model.*;
import forms.MovieForm;
import forms.grabbers.GrabberInfoForm;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TmdbGrabber implements IInfoGrabber {

  private static final String API_KEY = "a67216a4ad62ec0f81e3fffbfe18507f";

  private final static EGrabberType TYPE = EGrabberType.TMDB;

  private static final String LANGUAGE = Locale.GERMAN.getLanguage();

  private TheMovieDbApi theMovieDb;

  private TmdbConfiguration configuration;

  public TmdbGrabber() {
    try {
      theMovieDb = new TheMovieDbApi(TmdbGrabber.API_KEY);
      configuration = theMovieDb.getConfiguration();
    } catch (final MovieDbException e) {
      Logger.error("An error happend while initializing: " + TheMovieDbApi.class.getName(), e);
    }
  }

  @Override
  public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException {

    try {
      final List<GrabberSearchMovie> returnVal = new ArrayList<GrabberSearchMovie>();
      //final List<MovieDb> results = theMovieDb.searchMovie(searchTerm,0, TmdbGrabber.LANGUAGE, false,0);
        List<MovieDb> results = theMovieDb.searchMovie(searchTerm, 0, TmdbGrabber.LANGUAGE, false, 0).getResults();

        if (CollectionUtils.isEmpty(results) == false) {
        for (final MovieDb movieDb : results) {
          final String posterImageUrl = buildImageUrl(configuration.getPosterSizes().get(0), movieDb.getPosterPath());
          returnVal.add(new GrabberSearchMovie(String.valueOf(movieDb.getId()), movieDb.getTitle(), posterImageUrl, TmdbGrabber.TYPE));
        }
      }

      return returnVal;
    } catch (final MovieDbException e) {
      Logger.error("An error happend while searching for movies.", e);
      throw new GrabberException(e);
    }

  }

  /**
   * Builds an url to the image
   *
   * @param size
   * @param imgPath
   * @return
   */
  private String buildImageUrl(final String size, final String imgPath) {
    if (StringUtils.isEmpty(imgPath) == true) {
      return null;
    }
    return configuration.getBaseUrl() + size + imgPath;
  }

  @Override
  public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException {

    try {
      final Integer idAsInt = Integer.valueOf(id);
      MovieDb movieInfo = getMovieDB(idAsInt);

      final List<Artwork> movieImages = theMovieDb.getMovieImages(idAsInt, null).getResults();

      final List<GrabberImage> posters = new ArrayList<GrabberImage>();
      final List<GrabberImage> backdrops = new ArrayList<GrabberImage>();

      if (CollectionUtils.isEmpty(movieImages) == false) {
        for (final Artwork artwork : movieImages) {
          if (ArtworkType.POSTER.equals(artwork.getArtworkType()) == true) {
            posters.add(new GrabberImage(artwork.getFilePath(), buildImageUrl(configuration.getPosterSizes().get(0), artwork.getFilePath())));
          }

          if (ArtworkType.BACKDROP.equals(artwork.getArtworkType()) == true) {
            backdrops.add(new GrabberImage(artwork.getFilePath(), buildImageUrl(configuration.getBackdropSizes().get(0), artwork.getFilePath())));
          }
        }
      }

      final List<String> trailerUrls = new ArrayList<String>();
      final List<Trailer> movieTrailers = theMovieDb.getMovieTrailers(idAsInt, TmdbGrabber.LANGUAGE).getResults();
      movieTrailers.addAll(theMovieDb.getMovieTrailers(idAsInt, null).getResults());

      for (final Trailer trailer : movieTrailers) {
        if ("youtube".equals(trailer.getWebsite()) == true) {
          trailerUrls.add(trailer.getSource());
        }
      }

      final GrabberDisplayMovie displayMovie = new GrabberDisplayMovie(id, movieInfo.getTitle(), movieInfo.getOverview(), posters, backdrops, trailerUrls, TmdbGrabber.TYPE,movieInfo.getImdbID());

      return displayMovie;

    } catch (final NumberFormatException e) {
      throw new GrabberException(e);
    } catch (final MovieDbException e) {
      throw new GrabberException(e);
    }
  }

  @Override
  public MovieForm fillInfoToMovieForm(final GrabberInfoForm grabberInfoForm) throws GrabberException {

    try {

      final Integer id = Integer.valueOf(grabberInfoForm.grabberMovieId);
      MovieDb movieInfo = getMovieDB(id);

      final MovieForm movieForm = new MovieForm();
      movieForm.title = movieInfo.getTitle();
      movieForm.plot = movieInfo.getOverview();
      movieForm.runtime = movieInfo.getRuntime();
      movieForm.imdbId = movieInfo.getImdbID();
      movieForm.grabberType = TmdbGrabber.TYPE;
      movieForm.grabberId = grabberInfoForm.grabberMovieId;

      final String releaseDate = movieInfo.getReleaseDate();

      if (StringUtils.isEmpty(releaseDate) == false) {
        final String[] split = releaseDate.split("-");
        if (split.length == 3) {
          movieForm.year = Integer.valueOf(split[0]);
        }
      }

      final List<Genre> genres = movieInfo.getGenres();
      for (final Genre genre : genres) {
        movieForm.genres.add(genre.getName());
      }

      final List<Person> movieCasts = theMovieDb.getMovieCasts(id).getResults();
      for (final Person castInfo : movieCasts) {
        if ("Director".equals(castInfo.getJob())) {
          movieForm.director = castInfo.getName();
          continue;
        }

        if ("actor".equals(castInfo.getJob())) {
          movieForm.actors.add(castInfo.getName());
          continue;
        }
      }

      final Collection belongsToCollection = movieInfo.getBelongsToCollection();
      if (belongsToCollection != null) {
        movieForm.series = belongsToCollection.getName();
      }

      final String tmdbBackDrop = grabberInfoForm.grabberBackDropId;
      if (StringUtils.isEmpty(tmdbBackDrop) == false) {
        movieForm.backDropUrl = buildImageUrl(configuration.getBackdropSizes().get(configuration.getBackdropSizes().size() - 1), tmdbBackDrop);
      }

      final String tmdbPoster = grabberInfoForm.grabberPosterId;
      if (StringUtils.isEmpty(tmdbPoster) == false) {
        movieForm.posterUrl = buildImageUrl(configuration.getPosterSizes().get(configuration.getPosterSizes().size() - 1), tmdbPoster);
      }

      if (StringUtils.isEmpty(grabberInfoForm.grabberTrailerUrl) == false) {
        movieForm.trailerUrl = grabberInfoForm.grabberTrailerUrl;
      }

      return movieForm;

    } catch (final MovieDbException e) {
      throw new GrabberException(e);
    }
  }

  /**
   * Gets the MovieDB Information from the TMDB WS
   *
   * @param id
   * @return
   * @throws MovieDbException
   */
  protected MovieDb getMovieDB(int id) throws MovieDbException {
    final MovieDb movieInfo = theMovieDb.getMovieInfo(id, TmdbGrabber.LANGUAGE);
    return movieInfo;
  }
}
