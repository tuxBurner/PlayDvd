package controllers;

import com.avaje.ebean.PagedList;
import com.google.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import forms.dvd.CopySearchFrom;
import helpers.ConfigurationHelper;
import helpers.ECopyListView;
import com.github.tuxBurner.jsAnnotations.JSRoute;

import models.Dvd;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dashboard.listdvds;

import javax.inject.Inject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.Set;

@Security.Authenticated(Secured.class)
@Singleton
public class ListCopiesController extends Controller {

  private final static Map<String, Integer> DVDS_PER_PAGE_CONFIG = ConfigurationHelper.createValMap("dvddb.dvds.perpage");

  private final static ECopyListView DEFAULT_VIEW = ECopyListView.valueOf(ConfigFactory.load().getString("dvddb.dvds.defaultview"));

  private final static String SESSION_VIEW_MODE = "_view_mode";

  private final FormFactory formFactory;


  @Inject
  ListCopiesController(final FormFactory formFactory) {
    this.formFactory = formFactory;
  }

  /**
   * Lists all the dvds
   *
   * @return
   */
  public Result listCopies(final Integer pageNr) {
    final CopySearchFrom currentSearchForm = CopySearchFrom.getCurrentSearchForm();
    if (pageNr != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return returnList(currentSearchForm);
  }

  /**
   * Lists all the dvds
   *
   * @return
   */
  @JSRoute
  public Result listCopiesJS(final Integer pageNr) {
    final CopySearchFrom currentSearchForm = CopySearchFrom.getCurrentSearchForm();
    if (pageNr != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return returnList(currentSearchForm,true);
  }

  /**
   * list all copies we have
   *
   * @return
   */
  public Result listAllCopies() {
    final CopySearchFrom copySearchFrom = new CopySearchFrom();
    return returnList(copySearchFrom);
  }

  /**
   * Lists the dvds by the user
   *
   * @param fromUserName
   * @return
   */
  public Result listByUser(final String fromUserName) {
    if (StringUtils.isEmpty(fromUserName)) {
      return Results.internalServerError("No Username given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.userName = fromUserName;

    return returnList(dvdListFrom);
  }

  /**
   * List the dvds by the genre
   *
   * @param genreName
   * @return
   */
  public Result listByGenre(final String genreName) {
    if (StringUtils.isEmpty(genreName)) {
      return Results.internalServerError("No Genrename given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.genre = urlDecodeString(genreName);

    return returnList(dvdListFrom);
  }

  /**
   * List the dvds by the actor
   *
   * @param actorName
   * @return
   */
  public Result listByActor(final String actorName) {
    if (StringUtils.isEmpty(actorName)) {
      return Results.internalServerError("No actorname given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();

    dvdListFrom.actor = urlDecodeString(actorName);

    return returnList(dvdListFrom);
  }

  /**
   * Lists all {@link models.Dvd}s by the given director
   *
   * @param directorName
   * @return
   */
  public Result listByDirector(final String directorName) {
    if (StringUtils.isEmpty(directorName)) {
      return Results.internalServerError("No directorname given");
    }
    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.director = urlDecodeString(directorName);
    return returnList(dvdListFrom);
  }

  /**
   * Lists all the dvd the user lend to somebody
   *
   * @return
   */
  public Result listLendDvd() {

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.lendDvd = true;
    dvdListFrom.userName = Secured.getUsername();

    return returnList(dvdListFrom);
  }

  public Result listReviewMovies() {
    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.moviesToReview = true;

    return returnList(dvdListFrom);

  }

  public Result searchDvd() {
    final String[] strings = Controller.request().queryString().get("searchFor");
    if (strings == null || strings.length != 1) {
      return listAllCopies();
    } else {
      final CopySearchFrom listFrom = new CopySearchFrom();
      listFrom.searchFor = strings[0];
      return returnList(listFrom);
    }

  }

  /**
   * This is called when the user applys the search form above the list of
   * movies
   *
   * @return
   */
  public Result applySearchForm() {

    final Form<CopySearchFrom> form = formFactory.form(CopySearchFrom.class).bindFromRequest();

    return returnList(form.get());
  }


  /**
   * Returns the dvds for the template
   * @param copySearchFrom
   * @return
   */
  private Result returnList(final CopySearchFrom copySearchFrom) {
    return returnList(copySearchFrom,false);
  }

  /**
   * Returns the dvds for the template
   *
   * @param copySearchFrom
   * @return
   */
  private  Result returnList(final CopySearchFrom copySearchFrom, final boolean jsMode) {

    final String username = Secured.getUsername();
    CopySearchFrom.setCurrentSearchForm(copySearchFrom);
    final ECopyListView currentViewMode = getCurrentViewMode();
    final Integer itemsPerPage = DVDS_PER_PAGE_CONFIG.get(currentViewMode.name());
    final PagedList<Dvd> dvdsByForm = Dvd.getDvdsBySearchForm(copySearchFrom, itemsPerPage);
    final DvdPage dvdPage = new DvdPage(dvdsByForm);
    final CacheShoppingCart shoppingCartFromCache = ShoppingCartController.getShoppingCartFromCache();
    final Set<Long> bookmarkedCopyIds = BookmarksController.getBookmarkedCopyIds();
    if(jsMode == false) {
      final Form<CopySearchFrom> form =  formFactory.form(CopySearchFrom.class);
      return Results.ok(listdvds.render(dvdPage, form.fill(copySearchFrom), username, shoppingCartFromCache,currentViewMode,bookmarkedCopyIds));
    } else {
      return Results.ok(views.html.dashboard.listviews.listviewsWrapper.render(dvdPage,username,shoppingCartFromCache,bookmarkedCopyIds,currentViewMode));
    }
  }

  /**
   * Sets the view mode for the dvd view
   * @param viewMode
   * @return
   */
  public Result changeViewMode(final String viewMode) {
    session().put(SESSION_VIEW_MODE,viewMode);

    return redirect(routes.ListCopiesController.listAllCopies());
  }

  /**
   * Decodes a string from an url
   *
   * @param string
   * @return
   */
  private  String urlDecodeString(final String string) {
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
