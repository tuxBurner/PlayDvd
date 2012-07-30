package controllers;

import models.Dvd;
import play.data.Form;
import play.data.Form.Field;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dashboard.dvdform;
import forms.DvdForm;

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
    final Form<DvdForm> form = Controller.form(forms.DvdForm.class);
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

    final Form<DvdForm> form = Controller.form(forms.DvdForm.class);

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

        final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);

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

      return Results.redirect(routes.ListDvds.listdvds(null));
    }
  }
}
