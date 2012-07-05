package controllers;

import helpers.RequestToCollectionHelper;

import java.util.Map;

import models.Dvd;
import models.Movie;

import org.codehaus.jackson.node.ObjectNode;

import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import tmdb.GrabberException;
import tmdb.InfoGrabber;
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
   * Displays the {@link MovieForm} to the user in the add mode
   * 
   * @return
   */
  public static Result showAddMovieForm() {
    final Form<MovieForm> form = Controller.form(MovieForm.class);
    return Results.ok(movieform.render(form.fill(new MovieForm()), DvdController.DVD_FORM_ADD_MODE));
  }

  /**
   * This is called when the user submits the add Dvd Form
   * 
   * @return
   */
  public static Result addOrEditMovie(final String mode) {

    final Map<String, String> map = RequestToCollectionHelper.requestToFormMap(Controller.request(), "actors", "genres");
    final Form<MovieForm> movieForm = new Form<MovieForm>(MovieForm.class).bind(map);

    if (movieForm.hasErrors()) {
      return Results.badRequest(movieform.render(movieForm, mode));
    } else {
      try {
        final Movie editOrAddFromForm = Movie.editOrAddFromForm(movieForm.get());
        final ObjectNode result = Json.newObject();
        result.put("id", editOrAddFromForm.id);
        result.put("title", editOrAddFromForm.title);
        return Results.ok(result);
      } catch (final Exception e) {
        e.printStackTrace();
        return Results.badRequest(movieform.render(movieForm, mode));
      }

    }
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
