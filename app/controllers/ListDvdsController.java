package controllers;

import com.avaje.ebean.Page;
import com.typesafe.config.ConfigFactory;
import forms.dvd.DvdSearchFrom;
import helpers.ConfigurationHelper;
import helpers.ECopyListView;
import plugins.jsAnnotations.JSRoute;
import models.Dvd;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dashboard.listdvds;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

@Security.Authenticated(Secured.class)
public class ListDvdsController extends Controller {

  private final static Map<String, Integer> DVDS_PER_PAGE_CONFIG = ConfigurationHelper.createValMap("dvddb.dvds.perpage");

  private final static ECopyListView DEFAULT_VIEW = ECopyListView.valueOf(ConfigFactory.load().getString("dvddb.dvds.defaultview"));

  private final static String SESSION_VIEW_MODE = "_view_mode";

  /**
   * Lists all the dvds
   *
   * @return
   */
  public static Result listdvds(final Integer pageNr) {
    final DvdSearchFrom currentSearchForm = DvdSearchFrom.getCurrentSearchForm();
    if (pageNr != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return ListDvdsController.returnList(currentSearchForm);
  }

  /**
   * Lists all the dvds
   *
   * @return
   */
  @JSRoute
  public static Result listCopiesJS(final Integer pageNr) {
    final DvdSearchFrom currentSearchForm = DvdSearchFrom.getCurrentSearchForm();
    if (pageNr != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return ListDvdsController.returnList(currentSearchForm,true);
  }

  /**
   * list all dvds we have
   *
   * @return
   */
  public static Result listAlldvds() {
    final DvdSearchFrom dvdSearchFrom = new DvdSearchFrom();
    return ListDvdsController.returnList(dvdSearchFrom);
  }

  /**
   * Lists the dvds by the user
   *
   * @param fromUserName
   * @return
   */
  public static Result listByUser(final String fromUserName) {
    if (StringUtils.isEmpty(fromUserName)) {
      return Results.internalServerError("No Username given");
    }

    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();
    dvdListFrom.userName = fromUserName;

    return ListDvdsController.returnList(dvdListFrom);
  }

  /**
   * List the dvds by the genre
   *
   * @param genreName
   * @return
   */
  public static Result listByGenre(final String genreName) {
    if (StringUtils.isEmpty(genreName)) {
      return Results.internalServerError("No Genrename given");
    }

    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();
    dvdListFrom.genre = urlDecodeString(genreName);

    return ListDvdsController.returnList(dvdListFrom);
  }

  /**
   * List the dvds by the actor
   *
   * @param actorName
   * @return
   */
  public static Result listByActor(final String actorName) {
    if (StringUtils.isEmpty(actorName)) {
      return Results.internalServerError("No actorname given");
    }

    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();

    dvdListFrom.actor = urlDecodeString(actorName);

    return ListDvdsController.returnList(dvdListFrom);
  }

  /**
   * Lists all {@link Dvd}s by the given director
   *
   * @param directorName
   * @return
   */
  public static Result listByDirector(final String directorName) {
    if (StringUtils.isEmpty(directorName)) {
      return Results.internalServerError("No directorname given");
    }
    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();
    dvdListFrom.director = urlDecodeString(directorName);
    return ListDvdsController.returnList(dvdListFrom);
  }

  /**
   * Lists all the dvd the user lend to somebody
   *
   * @return
   */
  public static Result listLendDvd() {

    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();
    dvdListFrom.lendDvd = true;
    dvdListFrom.userName = Secured.getUsername();

    return ListDvdsController.returnList(dvdListFrom);
  }

  public static Result listReviewMovies() {
    final DvdSearchFrom dvdListFrom = new DvdSearchFrom();
    dvdListFrom.moviesToReview = true;

    return ListDvdsController.returnList(dvdListFrom);

  }

  public static Result searchDvd() {
    final String[] strings = Controller.request().queryString().get("searchFor");
    if (strings == null || strings.length != 1) {
      return ListDvdsController.listAlldvds();
    } else {
      final DvdSearchFrom listFrom = new DvdSearchFrom();
      listFrom.searchFor = strings[0];
      return ListDvdsController.returnList(listFrom);
    }

  }

  /**
   * This is called when the user applys the search form above the list of
   * movies
   *
   * @return
   */
  public static Result applySearchForm() {

    final Form<DvdSearchFrom> form = Form.form(DvdSearchFrom.class).bindFromRequest();

    return ListDvdsController.returnList(form.get());
  }


  /**
   * Returns the dvds for the template
   * @param dvdSearchFrom
   * @return
   */
  private static Result returnList(final DvdSearchFrom dvdSearchFrom) {
    return returnList(dvdSearchFrom,false);
  }

  /**
   * Returns the dvds for the template
   *
   * @param dvdSearchFrom
   * @return
   */
  private static Result returnList(final DvdSearchFrom dvdSearchFrom, final boolean jsMode) {

    final String username = Secured.getUsername();
    DvdSearchFrom.setCurrentSearchForm(dvdSearchFrom);
    final ECopyListView currentViewMode = getCurrentViewMode();
    final Integer itemsPerPage = DVDS_PER_PAGE_CONFIG.get(currentViewMode.name());
    final Page<Dvd> dvdsByForm = Dvd.getDvdsBySearchForm(dvdSearchFrom, itemsPerPage);
    final DvdPage dvdPage = new DvdPage(dvdsByForm);
    final CacheShoppingCart shoppingCartFromCache = ShoppingCartController.getShoppingCartFromCache();
    final Set<Long> bookmarkedCopyIds = BookmarksController.getBookmarkedCopyIds();
    if(jsMode == false) {
      final Form<DvdSearchFrom> form = Form.form(DvdSearchFrom.class);
      return Results.ok(listdvds.render(dvdPage, form.fill(dvdSearchFrom), username, shoppingCartFromCache,currentViewMode,bookmarkedCopyIds));
    } else {
      return Results.ok(views.html.dashboard.listviews.listviewsWrapper.render(dvdPage,username,shoppingCartFromCache,bookmarkedCopyIds,currentViewMode));
    }
  }

  /**
   * Sets the view mode for the dvd view
   * @param viewMode
   * @return
   */
  public static Result changeViewMode(final String viewMode) {
    session().put(SESSION_VIEW_MODE,viewMode);

    return redirect(routes.ListDvdsController.listAlldvds());
  }

  /**
   * Decodes a string from an url
   *
   * @param string
   * @return
   */
  private static String urlDecodeString(final String string) {
    try {
      return URLDecoder.decode(string, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error(e.getMessage(), e);
      }

      return string;
    }
  }

  /**
   * Reads the current {@ECopyListView} from the session
   * @return
   */
  public static ECopyListView getCurrentViewMode() {
    String viewMode = session().get(SESSION_VIEW_MODE);
    if(viewMode == null) {
      viewMode = DEFAULT_VIEW.name();
      session().put(SESSION_VIEW_MODE,viewMode);
    }

    return ECopyListView.valueOf(viewMode);
  }
}
