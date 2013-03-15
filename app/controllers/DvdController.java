package controllers;

import forms.MovieForm;
import forms.dvd.DvdForm;
import forms.grabbers.GrabberInfoForm;
import grabbers.EGrabberType;
import grabbers.IInfoGrabber;
import grabbers.amazon.AmazonMovieLookuper;
import grabbers.amazon.AmazonResult;
import jsannotation.JSRoute;
import models.Dvd;
import models.Movie;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dvd.dvdEanNrPopUp;
import views.html.dvd.dvdform;

import java.util.List;

/**
 * This {@link Controller} handles all the edit and add {@link Dvd} magic
 * 
 * @author tuxburner
 * 
 */
@Security.Authenticated(Secured.class)
public class DvdController extends Controller {

  public static final String DVD_FORM_ADD_MODE = "add";

  public static final String DVD_FORM_EDIT_MODE = "edit";

  /**
   * Shows the add Dvd form
   * 
   * @return
   */
  public static Result showAddDvd() {

    final Form<DvdForm> form = Form.form(DvdForm.class);
    return Results.ok(dvdform.render(form.fill(new DvdForm()), DvdController.DVD_FORM_ADD_MODE));
  }

  /**
   * Shows the edit dvd form
   * 
   * @return
   */
  public static Result showEditDvd(final Long dvdId) {

    final Dvd dvdToEdit = Dvd.getDvdForUser(dvdId, Controller.request().username());

    if (dvdToEdit == null) {
      return Results.badRequest("U ARE NOT ALLOWED TO EDIT :) ");
    }

    final Form<DvdForm> form = Form.form(DvdForm.class);

    return Results.ok(dvdform.render(form.fill(DvdForm.dvdToDvdForm(dvdToEdit)), DvdController.DVD_FORM_EDIT_MODE));
  }

  /**
   * This is called when the user submits the add Dvd Form
   * 
   * @return
   */
  public static Result addDvd(final String mode) {

    final Form<DvdForm> dvdForm = new Form<DvdForm>(DvdForm.class).bindFromRequest();
    if (dvdForm.hasErrors()) {
      return Results.badRequest(dvdform.render(dvdForm, mode));
    } else {

      try {

        final String userName = Secured.getUsername();

        if (DvdController.DVD_FORM_ADD_MODE.equals(mode) == true) {
          final Dvd createFromForm = Dvd.createFromForm(userName, dvdForm.get());
          Controller.flash("success", "Dvd: " + createFromForm.movie.title + " added");
        }

        if (DvdController.DVD_FORM_EDIT_MODE.equals(mode) == true) {
          final Dvd editFromForm = Dvd.editFromForm(userName, dvdForm.get());
          Controller.flash("success", "Dvd: " + editFromForm.movie.title + " edited");
        }

      } catch (final Exception e) {
        e.printStackTrace();
        return Results.badRequest(dvdform.render(dvdForm, mode));
      }

      return Results.redirect(routes.ListDvdsController.listdvds(null));
    }
  }

  /**
   * Searches a movie via amazon with the given eanNr
   * @param eanNr the ean nr to lookup
   * @return
   */
  @JSRoute
  public static Result searchEanNr(final String eanNr) {

    AmazonResult result = null;

    List<Movie> movies = null;
    if (StringUtils.isEmpty(eanNr) == false) {
      result = AmazonMovieLookuper.lookUpByEanNR(eanNr);

      if (result != null && StringUtils.isEmpty(result.title) == false) {
        movies = Movie.searchLike(result.title, 0);
      }
    }


    return ok(dvdEanNrPopUp.render(result, eanNr, movies));
  }

  /**
   * Adds a {@link Movie}
   * @param grabberType
   * @return
   */
  @JSRoute
  public static Result addMovieByGrabber(final String grabberType) {
    try {
    final Form<GrabberInfoForm> grabberInfoForm = Form.form(GrabberInfoForm.class).bindFromRequest();

    final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));

    final MovieForm movieForm = grabber.fillInfoToMovieForm(grabberInfoForm.get());
      final Movie movie = Movie.editOrAddFromForm(movieForm);

      if(movie == null) {
        return Results.badRequest("An error happend while creating the new movie");
      }

      return ok(String.valueOf(movie.id));

    }catch (final Exception e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("Internal Error happened", e);
      }
      return Results.badRequest("Internal Error happened");
    }
  }

  /**
   * Shows the add Dvd form
   * @param eanNr
   * @param movieId
   *
   * @return
   */
  @JSRoute
  public static Result showAddDvdByEanAndMovie(final String eanNr, final Long movieId) {

    if(StringUtils.isEmpty(eanNr) == true || movieId == null) {
      return badRequest();
    }

    AmazonResult amazonResult = AmazonMovieLookuper.lookUpByEanNR(eanNr);
    if(amazonResult == null) {
      if(Logger.isDebugEnabled() == true) {
        Logger.error("Error adding dvd with eanNr: " + eanNr);
      }
      return badRequest();
    }

    Movie movie = Movie.find.byId(movieId);
    if(movie == null) {
      if(Logger.isDebugEnabled() == true) {
        Logger.error("Error adding dvd with movie: " + movieId);
      }
      return badRequest();
    }


    final Form<DvdForm> form = Form.form(DvdForm.class);
    final DvdForm dvdForm = DvdForm.eanAndMovieToDvdForm(amazonResult,movieId,eanNr);


    return Results.ok(dvdform.render(form.fill(dvdForm), DvdController.DVD_FORM_ADD_MODE));
  }

}
