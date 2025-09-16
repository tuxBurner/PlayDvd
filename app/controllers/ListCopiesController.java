package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Singleton;
import com.typesafe.config.ConfigFactory;
import dao.DvdDao;
import forms.copy.CopySearchFrom;
import helpers.CacheHelper;
import helpers.ConfigurationHelper;
import helpers.ECopyListView;
import io.ebean.PagedList;
import models.Dvd;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;
import views.html.dashboard.listCopies;

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

  private final CacheHelper cacheHelper;

  private final MessagesApi messagesApi;


  @Inject
  ListCopiesController(final FormFactory formFactory, final CacheHelper cacheHelper, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.cacheHelper = cacheHelper;
    this.messagesApi = messagesApi;
  }

  /**
   * Lists all the dvds
   *
   * @return
   */
  public Result listCopies(final Integer pageNr, final Http.Request request) {
    final CopySearchFrom currentSearchForm = CopySearchFrom.getCurrentSearchForm(cacheHelper, request);
    if (pageNr != null && currentSearchForm != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return returnList(currentSearchForm, request);
  }

  /**
   * Lists all the dvds
   *
   * @return
   */
  @JSRoute
  public Result listCopiesJS(final Integer pageNr, final Http.Request request) {
    final CopySearchFrom currentSearchForm = CopySearchFrom.getCurrentSearchForm(cacheHelper, request);
    if (pageNr != null && currentSearchForm != null) {
      currentSearchForm.currentPage = pageNr;
    }
    return returnList(currentSearchForm, true, request);
  }

  /**
   * list all copies we have
   *
   * @return
   */
  public Result listAllCopies(final Http.Request request) {
    final CopySearchFrom copySearchFrom = new CopySearchFrom();
    return returnList(copySearchFrom, request);
  }

  /**
   * Lists the dvds by the user
   *
   * @param fromUserName
   * @return
   */
  public Result listByUser(final String fromUserName, final Http.Request request) {
    if (StringUtils.isEmpty(fromUserName)) {
      return Results.internalServerError("No Username given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.userName = fromUserName;

    return returnList(dvdListFrom, request);
  }

  /**
   * List the dvds by the genre
   *
   * @param genreName
   * @return
   */
  public Result listByGenre(final String genreName, final Http.Request request) {
    if (StringUtils.isEmpty(genreName)) {
      return Results.internalServerError("No Genrename given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.genre = urlDecodeString(genreName);

    return returnList(dvdListFrom, request);
  }

  /**
   * List the dvds by the actor
   *
   * @param actorName
   * @return
   */
  public Result listByActor(final String actorName, final Http.Request request) {
    if (StringUtils.isEmpty(actorName)) {
      return Results.internalServerError("No actorname given");
    }

    final CopySearchFrom dvdListFrom = new CopySearchFrom();

    dvdListFrom.actor = urlDecodeString(actorName);

    return returnList(dvdListFrom, request);
  }

  /**
   * Lists all {@link models.Dvd}s by the given director
   *
   * @param directorName
   * @return
   */
  public Result listByDirector(final String directorName, final Http.Request request) {
    if (StringUtils.isEmpty(directorName)) {
      return Results.internalServerError("No directorname given");
    }
    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.director = urlDecodeString(directorName);
    return returnList(dvdListFrom, request);
  }

  /**
   * Lists all the dvd the user lend to somebody
   *
   * @return
   */
  public Result listLendDvd(final Http.Request request) {

    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.lendDvd = true;
    dvdListFrom.userName = Secured.getUsernameStatic(request);

    return returnList(dvdListFrom, request);
  }

  public Result listReviewMovies(final Http.Request request) {
    final CopySearchFrom dvdListFrom = new CopySearchFrom();
    dvdListFrom.moviesToReview = true;

    return returnList(dvdListFrom, request);

  }

  public Result searchDvd(final Http.Request request) {
    final String[] strings = request.queryString().get("searchFor");
    if (strings == null || strings.length != 1) {
      return listAllCopies(request);
    } else {
      final CopySearchFrom listFrom = new CopySearchFrom();
      listFrom.searchFor = strings[0];
      return returnList(listFrom, request);
    }

  }

  /**
   * This is called when the user applys the search form above the list of
   * movies
   *
   * @return
   */
  public Result applySearchForm(final Http.Request request) {

    final Form<CopySearchFrom> form = formFactory.form(CopySearchFrom.class).bindFromRequest(request);

    return returnList(form.get(), request);
  }


  /**
   * Returns the dvds for the template
   *
   * @param copySearchFrom
   * @return
   */
  private Result returnList(final CopySearchFrom copySearchFrom, final Http.Request request) {
    return returnList(copySearchFrom, false, request);
  }

  /**
   * Returns the dvds for the template
   *
   * @param copySearchFrom
   * @return
   */
  private Result returnList(final CopySearchFrom copySearchFrom, final boolean jsMode, final Http.Request request) {

    final String username = Secured.getUsernameStatic(request);
    CopySearchFrom.setCurrentSearchForm(copySearchFrom, cacheHelper, request);
    final ECopyListView currentViewMode = getCurrentViewMode(request);
    final Integer itemsPerPage = DVDS_PER_PAGE_CONFIG.get(currentViewMode.name());
    final PagedList<Dvd> dvdsByForm = DvdDao.getDvdsBySearchForm(copySearchFrom, itemsPerPage);
    final DvdPage dvdPage = new DvdPage(dvdsByForm);
    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache(request);
    final Set<Long> bookmarkedCopyIds = cacheHelper.getBookmarkedCopyIds(request);

    final Messages messages = this.messagesApi.preferred(request);

    if (jsMode == false) {
      final Form<CopySearchFrom> form = formFactory.form(CopySearchFrom.class);
      return Results.ok(listCopies.render(dvdPage, form.fill(copySearchFrom), cacheHelper, username, shoppingCartFromCache, currentViewMode, bookmarkedCopyIds, request, messages));
    } else {
      return Results.ok(views.html.dashboard.listviews.listviewsWrapper.render(dvdPage, username, shoppingCartFromCache, bookmarkedCopyIds, currentViewMode, request, messages));
    }
  }

  /**
   * Sets the view mode for the dvd view
   *
   * @param viewMode
   * @return
   */
  public Result changeViewMode(final String viewMode, final Http.Request request) {
    return redirect(routes.ListCopiesController.listAllCopies())
        .addingToSession(request, SESSION_VIEW_MODE, viewMode);
  }

  /**
   * @param string
   * @return
   */
  private String urlDecodeString(final String string) {
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
   *
   * @return
   */
  public static ECopyListView getCurrentViewMode(final Http.Request request) {
    String viewMode = request.session().get(SESSION_VIEW_MODE).orElse(null);
    if (viewMode == null) {
      viewMode = DEFAULT_VIEW.name();
      request.session().adding(SESSION_VIEW_MODE, viewMode);
    }

    return ECopyListView.valueOf(viewMode);
  }
}
