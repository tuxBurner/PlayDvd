package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import dao.DvdDao;
import dao.MovieDao;
import forms.MovieForm;
import forms.dvd.CopyForm;
import forms.grabbers.GrabberInfoForm;
import grabbers.EGrabberType;
import grabbers.GrabberHelper;
import grabbers.IInfoGrabber;
import grabbers.amazonwebcrawler.AmazonMovieWebCrawler;
import grabbers.amazonwebcrawler.AmazonResult;
import helpers.RequestToCollectionHelper;
import models.Dvd;
import models.DvdAttribute;
import models.EDvdAttributeType;
import models.Movie;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This {@link Controller} handles all the edit and add {@link Dvd} magic
 *
 * @author tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class CopyController extends Controller {

  public static final String DVD_FORM_ADD_MODE = "add";

  public static final String DVD_FORM_EDIT_MODE = "edit";

  public static final Long NO_COPY_SELECTED_ID = Long.valueOf(-1);

  final FormFactory formFactory;
  private final MessagesApi messagesApi;

  @Inject
  public CopyController(final FormFactory formFactory, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.messagesApi = messagesApi;
  }


  /**
   * Shows the add Dvd form
   *
   * @return
   */
  public Result showAddDvd(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    final Form<CopyForm> form = formFactory.form(CopyForm.class);
    return Results.ok(views.html.dvd.dvdform.render(form.fill(new CopyForm()), CopyController.DVD_FORM_ADD_MODE, request, messages));
  }

  /**
   * Shows the edit dvd form
   *
   * @return
   */
  public Result showEditDvd(final Long dvdId, final Http.Request request) {

    final var username = Secured.getUsernameStatic(request);
    final Dvd dvdToEdit = DvdDao.getDvdForUser(dvdId, username);

    if (dvdToEdit == null) {
      return Results.badRequest("U ARE NOT ALLOWED TO EDIT :) ");
    }

    final Form<CopyForm> form = formFactory.form(CopyForm.class);
    final Messages messages = this.messagesApi.preferred(request);

    return Results.ok(views.html.dvd.dvdform.render(form.fill(CopyForm.dvdToDvdForm(dvdToEdit)), CopyController.DVD_FORM_EDIT_MODE, request, messages));
  }

  /**
   * This is called when the user submits the add Dvd Form
   *
   * @return
   */
  public Result addDvd(final String mode, final Http.Request request) {

    final Map<String, String> map = RequestToCollectionHelper.requestToFormMap(request, "audioTypes");
    // TODO: LIFT bind(map) is not working
    final Form<CopyForm> dvdForm = formFactory.form(CopyForm.class).bindFromRequest(request);
    final Messages messages = this.messagesApi.preferred(request);
    if (dvdForm.hasErrors()) {
      return Results.badRequest(views.html.dvd.dvdform.render(dvdForm, mode, request, messages));
    } else {

      try {

        final String userName = Secured.getUsernameStatic(request);

        if (CopyController.DVD_FORM_ADD_MODE.equals(mode) == true) {
          final Dvd createFromForm = DvdDao.createFromForm(userName, dvdForm.get());
          request.flash().adding("success", "Dvd: " + createFromForm.movie.title + " added");
        }

        if (CopyController.DVD_FORM_EDIT_MODE.equals(mode) == true) {
          final Dvd editFromForm = DvdDao.editFromForm(userName, dvdForm.get());
          request.flash().adding("success", "Dvd: " + editFromForm.movie.title + " edited");
        }

      } catch (final Exception e) {
        e.printStackTrace();
        return Results.badRequest(views.html.dvd.dvdform.render(dvdForm, mode, request, messages));
      }

      return Results.redirect(routes.ListCopiesController.listCopies(null));
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
  public Result searchAmazonByCode(final String code, final Long copyId, final Http.Request request) {

    final Messages messages = this.messagesApi.preferred(request);

    Optional<AmazonResult> result = null;
    List<Movie> movies = null;
    if (StringUtils.isEmpty(code) == false) {
      result = AmazonMovieWebCrawler.lookUpByCode(code);
      if (result.isPresent() && StringUtils.isEmpty(result.get().title) == false) {
        movies = MovieDao.searchLikeAndAmazoneCode(result.get().title, result.get().ean);
      }
    }

    return ok(views.html.dvd.dvdAmazonPopUp.render(result.orElseGet(null), code, copyId, movies, request, messages));
  }

  /**
   * Starts a search for the given title on amazon.
   *
   * @param title the title to search for.
   * @return
   */
  @JSRoute
  public Result searchAmazonByTitle(final String title, final Http.Request request) {

    List<AmazonResult> amazonResults = null;

    if (StringUtils.isBlank(title) == false) {
      //amazonResults = AmazonMovieLookuper.findByName(title);
      amazonResults = AmazonMovieWebCrawler.findByName(title);
    }

    final Messages messages = this.messagesApi.preferred(request);

    return ok(views.html.dvd.searchAmazonByTitlePopUp.render(amazonResults, title, request, messages));
  }

  /**
   * Adds a {@link Movie}
   *
   * @param grabberType
   * @return
   */
  @JSRoute
  public Result addMovieByGrabber(final String grabberType, final Http.Request request) {
    try {
      final Form<GrabberInfoForm> grabberInfoForm = formFactory.form(GrabberInfoForm.class).bindFromRequest(request);

      final IInfoGrabber grabber = GrabberHelper.getGrabber(EGrabberType.valueOf(grabberType));

      final MovieForm movieForm = grabber.fillInfoToMovieForm(grabberInfoForm.get());
      final Movie movie = MovieDao.editOrAddFromForm(movieForm, true);

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
  public Result showDvdByAmazonAndMovie(final String code, final Long movieId, final Long copyId, final Http.Request request) {

    if (StringUtils.isEmpty(code) == true || movieId == null) {
      return badRequest();
    }

    Optional<AmazonResult> amazonResult = AmazonMovieWebCrawler.lookUpByCode(code);
    if (amazonResult == null) {
      if (Logger.isDebugEnabled() == true) {
        Logger.error("Error adding dvd with amazonecode: " + code);
      }
      return badRequest();
    }

    Movie movie = Movie.FINDER.byId(movieId);
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
      copy = DvdDao.getDvdForUser(copyId, Secured.getUsernameStatic(request));
    }

    final Form<CopyForm> form = formFactory.form(CopyForm.class);
    final CopyForm copyForm = CopyForm.amazonAndMovieToDvdForm(amazonResult.orElseGet(null), movieId, copy);

    final Messages messages = this.messagesApi.preferred(request);

    return Results.ok(views.html.dvd.dvdform.render(form.fill(copyForm), mode, request, messages));
  }

  /**
   * Just fills the informations from the amazon lookup to the copy form an returns it
   */
  public Result showCopyFormWithAmazonInfo(final String code, final Long copyId, final Http.Request request) {
    if (StringUtils.isEmpty(code) == true) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No code is given for looking up amazon infos.");
      }
      return badRequest();
    }

    Dvd copy = null;
    if (copyId.equals(NO_COPY_SELECTED_ID) == false) {
      copy = DvdDao.getDvdForUser(copyId, Secured.getUsernameStatic(request));
      if (copy == null) {
        if (Logger.isDebugEnabled() == true) {
          Logger.debug("Could not find copy with id: " + copyId + " for user: " + Secured.getUsernameStatic(request));
        }
        return badRequest();
      }
    }

    final Optional<AmazonResult> amazonResult = AmazonMovieWebCrawler.lookUpByCode(code);
    String mode = DVD_FORM_ADD_MODE;
    final Form<CopyForm> form = formFactory.form(CopyForm.class);
    final CopyForm copyForm = CopyForm.amazonAndCopyToForm(copy, amazonResult.orElse(null));
    form.fill(copyForm);


    if (copy != null) {
      mode = DVD_FORM_EDIT_MODE;
    }

    final Messages messages = this.messagesApi.preferred(request);
    return Results.ok(views.html.dvd.dvdform.render(form.fill(copyForm), mode, request, messages));
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
