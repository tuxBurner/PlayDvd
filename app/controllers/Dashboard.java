package controllers;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;
import helpers.RequestToCollectionHelper;

import java.io.File;
import java.util.List;
import java.util.Map;

import jgravatar.Gravatar;
import jgravatar.GravatarDefaultImage;
import jgravatar.GravatarRating;
import models.Dvd;
import models.DvdAttibute;
import models.EAttributeType;
import models.User;

import org.apache.commons.lang.StringUtils;

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
import views.html.dashboard.lendform;
import forms.DvdForm;
import forms.InfoDvd;
import forms.LendForm;
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

    final InfoDvd infoDvd = new InfoDvd(dvd);

    return Results.ok(displaydvd.render(infoDvd));
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

      return Results.redirect(routes.ListDvds.listdvds(null));
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
        dvdForm.hullNr = dvd.hullNr;
      }

      final Form<DvdForm> form = Controller.form(DvdForm.class);

      return Results.ok(dvdform.render(form.fill(dvdForm), mode));
    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }
  }

  /**
   * Displays the dialog content for lending a dvd to a another {@link User}
   * 
   * @return
   */
  public static Result lendDialogContent(final Long dvdId) {
    // check if the user may see the dvd
    final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);
    final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    final Form<LendForm> form = Controller.form(LendForm.class);
    return Results.ok(lendform.render(form, dvdForUser));
  }

  /**
   * This actually lends a dvd to a user this is called via ajax
   * 
   * @param dvdId
   * @return
   */
  public static Result lendDvd(final Long dvdId) {

    final Form<LendForm> form = Controller.form(LendForm.class).bindFromRequest();

    // check if the form is okay
    final LendForm lendForm = form.get();
    final String userName = StringUtils.trimToNull(lendForm.userName);
    final String freeName = StringUtils.trimToNull(lendForm.freeName);

    if ((StringUtils.isEmpty(userName) == true && StringUtils.isEmpty(freeName) == true)) {
      System.out.println("username: " + userName + " freename: " + freeName);
      return Results.internalServerError();
    }

    final String ownerName = Controller.ctx().session().get(Secured.AUTH_SESSION);
    Dvd.lendDvdToUser(dvdId, ownerName, userName, freeName);

    return Results.TODO;
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

  /**
   * Gets the gravatar for the user
   * 
   * @return
   */
  public static Result gravatar(final Integer size, final String userName) {

    final String ownerName = (userName == null) ? Controller.ctx().session().get(Secured.AUTH_SESSION) : userName;
    final User userByName = User.getUserByName(ownerName);

    if (userByName == null) {
      return Results.ok();
    }

    final Gravatar gravatar = new Gravatar();
    gravatar.setSize(size);
    gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
    gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
    final byte[] jpg = gravatar.download(userByName.email);
    return Results.ok(jpg);

  }

}
