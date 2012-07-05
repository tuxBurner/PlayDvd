package controllers;

import java.util.List;

import models.Dvd;
import models.Movie;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import tmdb.GrabberException;
import tmdb.InfoGrabber;
import views.html.movie.listExistingMovies;
import views.html.movie.movieform;
import forms.MovieForm;
import forms.TmdbInfoForm;

/**
 * This {@link Controller} handles all the edit and add {@link Movie} magic
 * 
 * @author tuxburner
 * 
 */
@Security.Authenticated(Secured.class)
public class MovieController extends Controller {

  /**
   * This displays the user a select with dvds stored in the database so when he
   * wants to add a new dvd he can select one to prefill the informations
   */
  public static Result listExistingMovies(final Long dvdToEdit) {
    final List<Movie> movies = Movie.listByDistinctTitle();

    return Results.ok(listExistingMovies.render(movies, dvdToEdit));
  }

  /**
   * Displays the {@link MovieForm} to the user in the add mode
   * 
   * @return
   */
  public static Result showAddMovieForm() {
    final Form<MovieForm> form = Controller.form(MovieForm.class);
    return Results.ok(movieform.render(form.fill(new MovieForm()), Dashboard.DVD_FORM_ADD_MODE));
  }

  /**
   * When the user selected a movie from the tmdb popup we fill out the form and
   * display it
   * 
   * @param mode
   *          mode if we add or edit the movie
   * @return
   */
  public static Result addMovieByTmdbId(final String mode) {

    try {

      final Form<TmdbInfoForm> tmdbInfoForm = Controller.form(TmdbInfoForm.class).bindFromRequest();
      final MovieForm movieForm = InfoGrabber.fillDvdFormWithMovieInfo(tmdbInfoForm.get());

      if (tmdbInfoForm.get().dvdId != null) {
        // check if we can edit this dvd
        final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);
        final Dvd dvd = Dvd.getDvdForUser(tmdbInfoForm.get().dvdId, userName);

        // user is not allowed to edit this dvd
        if (dvd == null) {
          return Results.badRequest();
        }

        movieForm.movieId = dvd.id;
      }

      final Form<MovieForm> form = Controller.form(MovieForm.class);

      return Results.ok(movieform.render(form.fill(movieForm), mode));
    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }
  }

}
