package controllers;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;
import helpers.RequestToCollectionHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.Dvd;
import models.DvdAttibute;
import models.EAttributeType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import tmdb.GrabberException;
import tmdb.InfoGrabber;
import views.html.genremenu;
import views.html.dashboard.displaydvd;
import views.html.dashboard.dvdform;
import views.html.dashboard.listdvds;
import forms.DvdForm;
import forms.TmdbInfoForm;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {

  public static final String DVD_FORM_ADD_MODE = "add";

  public static final String DVD_FORM_EDIT_MODE = "edit";

  /**
   * Shows the add Dvd form
   * 
   * @return
   */
  public static Result showAddDvd() {
    final Form<DvdForm> form = Controller.form(forms.DvdForm.class);
    return Results.ok(dvdform.render(form.fill(new DvdForm()), Dashboard.DVD_FORM_ADD_MODE));
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

    final Form<DvdForm> form = Controller.form(forms.DvdForm.class);
    return Results.ok(dvdform.render(form.fill(DvdForm.dvdToDvdForm(dvdToEdit)), Dashboard.DVD_FORM_EDIT_MODE));
  }

  /**
   * Display the dvd and its informations
   * 
   * @param dvdId
   * @return
   */
  public static Result displayDvd(final Long dvdId) {
    final Dvd dvd = Dvd.find.byId(dvdId);

    if (dvd == null) {
      return Results.badRequest("Dvd with the given Id was not found");
    }

    final DvdForm dvdForm = DvdForm.dvdToDvdForm(dvd);

    return Results.ok(displaydvd.render(dvdForm));
  }

  /**
   * This is called when the user submits the add Dvd Form
   * 
   * @return
   */
  public static Result addDvd(final String mode) {

    final Map<String, String> map = RequestToCollectionHelper.requestToFormMap(Controller.request(), "actors", "genres");
    final Form<DvdForm> dvdForm = new Form<DvdForm>(DvdForm.class).bind(map);

    if (dvdForm.hasErrors()) {
      return Results.badRequest(dvdform.render(dvdForm, mode));
    } else {

      try {

        final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);

        if (Dashboard.DVD_FORM_ADD_MODE.equals(mode) == true) {
          // try to create the dvd from the form
          Dvd.createFromForm(userName, dvdForm.get());
          Controller.flash("success", "Dvd: " + dvdForm.get().title + " added");
        }

        if (Dashboard.DVD_FORM_EDIT_MODE.equals(mode) == true) {
          Dvd.editFromForm(userName, dvdForm.get());
          Controller.flash("success", "Dvd: " + dvdForm.get().title + " edited");
        }

      } catch (final Exception e) {
        e.printStackTrace();
        return Results.badRequest(dvdform.render(dvdForm, mode));
      }

      return Results.redirect(routes.ListDvds.listdvds(0));
    }
  }

  /**
   * When the user selected a movie from the tmdb popup we fill out the form and
   * display it
   * 
   * @param movieId
   * @return
   */
  public static Result addDvdByTmdbId(final String mode) {

    try {

      final Form<TmdbInfoForm> tmdbInfoForm = Controller.form(TmdbInfoForm.class).bindFromRequest();
      final DvdForm dvdForm = InfoGrabber.fillDvdFormWithMovieInfo(tmdbInfoForm.get());

      if (tmdbInfoForm.get().dvdId != null) {
        // check if we can edit this dvd
        final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);
        final Dvd dvd = Dvd.getDvdForUser(tmdbInfoForm.get().dvdId, userName);

        // user is not allowed to edit this dvd
        if (dvd == null) {
          return Results.badRequest();
        }

        dvdForm.dvdId = dvd.id;
      }

      final Form<DvdForm> form = Controller.form(DvdForm.class);

      return Results.ok(dvdform.render(form.fill(dvdForm), mode));
    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }

  }

  /**
   * This is called from the mainmenu.scala.html to have all the genres in the
   * menu
   * 
   * @return
   */
  public static Result menuGenres() {
    final List<DvdAttibute> genres = DvdAttibute.getAllByType(EAttributeType.GENRE);
    return Results.ok(genremenu.render(genres));
  }

  public static Result streamImage(final Long dvdId, final String imgType, final String imgSize) {
    final File file = ImageHelper.getImageFile(dvdId, EImageType.valueOf(imgType), EImageSize.valueOf(imgSize));
    if (file != null) {
      return Results.ok(file);
    }
    return Results.ok();
  }

}
