package controllers;

import forms.MovieForm;
import forms.dvd.DvdForm;
import forms.grabbers.GrabberInfoForm;
import grabbers.EGrabberType;
import grabbers.IInfoGrabber;
import grabbers.amazon.AmazonMovieLookuper;
import grabbers.amazon.AmazonResult;
import helpers.RequestToCollectionHelper;
import com.github.tuxBurner.jsAnnotations.JSRoute;
import models.Dvd;
import models.DvdAttribute;
import models.EDvdAttributeType;
import models.Movie;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dvd.dvdAmazonPopUp;
import views.html.dvd.dvdform;

import java.util.List;
import java.util.Map;

/**
 * This {@link Controller} handles all the edit and add {@link Dvd} magic
 *
 * @author tuxburner
 */
@Security.Authenticated(Secured.class)
public class DvdController extends Controller {

  public static final String DVD_FORM_ADD_MODE = "add";

  public static final String DVD_FORM_EDIT_MODE = "edit";

  public static final Long NO_COPY_SELECTED_ID = new Long(-1);

  /**
   * Shows the add Dvd form
   *
   * @return
   */
  public Result showAddDvd() {

    final Form<DvdForm> form = Form.form(DvdForm.class);
    return Results.ok(dvdform.render(form.fill(new DvdForm()), DvdController.DVD_FORM_ADD_MODE));
  }

  /**
   * Shows the edit dvd form
   *
   * @return
   */
  public Result showEditDvd(final Long dvdId) {

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
  public Result addDvd(final String mode) {

    final Map<String, String> map = RequestToCollectionHelper.requestToFormMap(Controller.request(), "audioTypes");
    final Form<DvdForm> dvdForm = new Form<DvdForm>(DvdForm.class).bind(map);
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
   * Searches a movie via amazon with the given code
   *
   * @param code   the code to lookup
   * @param copyId if set we search for an existing copy
   * @return
   */
  @JSRoute
  public Result searchAmazonByCode(final String code, final Long copyId) {
    AmazonResult result = null;
    List<Movie> movies = null;
    if (StringUtils.isEmpty(code) == false) {
      result = AmazonMovieLookuper.lookUp(code);
      if (result != null && StringUtils.isEmpty(result.title) == false) {
        movies = Movie.searchLikeAndAmazoneCode(result.title, result.ean);
      }
    }

    return ok(dvdAmazonPopUp.render(result, code, copyId, movies));
  }

  /**
   * Adds a {@link Movie}
   *
   * @param grabberType
   * @return
   */
  @JSRoute
  public Result addMovieByGrabber(final String grabberType) {
    try {
      final Form<GrabberInfoForm> grabberInfoForm = Form.form(GrabberInfoForm.class).bindFromRequest();

      final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));

      final MovieForm movieForm = grabber.fillInfoToMovieForm(grabberInfoForm.get());
      final Movie movie = Movie.editOrAddFromForm(movieForm);

      if (movie == null) {
        return Results.badRequest("An error happend while creating the new movie");
      }

      return ok(String.valueOf(movie.id));

    } catch (final Exception e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("Internal Error happened", e);
      }
      return Results.badRequest("Internal Error happened");
    }
  }

  /**
   * Shows the add edit Dvd form with the results from the {@link AmazonMovieLookuper}
   *
   * @param code
   * @param movieId
   * @param copyId
   * @return
   */
  @JSRoute
  public Result showDvdByAmazonAndMovie(final String code, final Long movieId, final Long copyId) {

    if (StringUtils.isEmpty(code) == true || movieId == null) {
      return badRequest();
    }

    AmazonResult amazonResult = AmazonMovieLookuper.lookUp(code);
    if (amazonResult == null) {
      if (Logger.isDebugEnabled() == true) {
        Logger.error("Error adding dvd with amazonecode: " + code);
      }
      return badRequest();
    }

    Movie movie = Movie.finder.byId(movieId);
    if (movie == null) {
      if (Logger.isDebugEnabled() == true) {
        Logger.error("Error adding dvd with movie: " + movieId);
      }
      return badRequest();
    }

    String mode = DVD_FORM_ADD_MODE;
    Dvd copy = null;
    if (copyId.equals(NO_COPY_SELECTED_ID) == false) {
      mode = DVD_FORM_EDIT_MODE;
      copy = Dvd.getDvdForUser(copyId, Secured.getUsername());
    }

    final Form<DvdForm> form = Form.form(DvdForm.class);
    final DvdForm dvdForm = DvdForm.amazonAndMovieToDvdForm(amazonResult, movieId, copy);

    return Results.ok(dvdform.render(form.fill(dvdForm), mode));
  }

  /**
   * Just fills the informations from the amazon lookup to the copy form an returns it
   */
  public Result showCopyFormWithAmazonInfo(final String code, final Long copyId) {
    if (StringUtils.isEmpty(code) == true) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No code is given for looking up amazon infos.");
      }
      return badRequest();
    }

    Dvd copy = null;
    if (copyId.equals(NO_COPY_SELECTED_ID) == false) {
      copy = Dvd.getDvdForUser(copyId, Controller.request().username());
      if (copy == null) {
        if (Logger.isDebugEnabled() == true) {
          Logger.debug("Could not find copy with id: " + copyId + " for user: " + Controller.request().username());
        }
        return badRequest();
      }
    }

    final AmazonResult amazonResult = AmazonMovieLookuper.lookUp(code);
    String mode = DVD_FORM_ADD_MODE;
    final Form<DvdForm> form = Form.form(DvdForm.class);
    final DvdForm dvdForm = DvdForm.amazonAndCopyToForm(copy, amazonResult);
    form.fill(dvdForm);


    if (copy != null) {
      mode = DVD_FORM_EDIT_MODE;
    }

    return Results.ok(dvdform.render(form.fill(dvdForm), mode));
  }

  /**
   * Searches for {@link DvdAttribute} returns a json with
   * {@link DvdAttribute} and {@link DvdAttribute#value}
   *
   * @param term
   * @param attrType
   * @return
   */
  @JSRoute
  public Result searchForCopyAttribute(final String term, final String attrType) {
    try {
      final EDvdAttributeType eattrType = EDvdAttributeType.valueOf(attrType);
      final String result = DvdAttribute.searchAvaibleAttributesAsJson(eattrType, term);
      return Results.ok(result);
    } catch (final Exception e) {
      Logger.error("An error happend while getting: " + attrType + " " + EDvdAttributeType.class.getName() + " with search term: " + term, e);
    }

    return Results.badRequest();
  }

}
