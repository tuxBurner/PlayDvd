package controllers;

import com.google.inject.Singleton;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import io.ebean.PagedList;
import models.Bookmark;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;

/**
 * Controller which handles the bookmarks.
 * User: tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class BookmarksController extends Controller {


  /**
   * The messages api
   */
  private final MessagesApi messagesApi;
  private final CacheHelper cacheHelper;

  @Inject
  public BookmarksController(final MessagesApi messagesApi, final CacheHelper cacheHelper) {
    this.messagesApi = messagesApi;
    this.cacheHelper = cacheHelper;
  }

  /**
   * Lists all the {@models.Bookmarks} which the current user created
   *
   * @return
   */
  public Result listBookmarks(final Integer page, final Http.Request request) {
    PagedList<Bookmark> listForUser = Bookmark.getBookmarksForUser(page, request);
    final Messages messages = this.messagesApi.preferred(request);

    return ok(views.html.bookmarks.bookmarklist.render(listForUser, page, request, messages));
  }

  /**
   * Creates a {@link models.Bookmark}
   *
   * @param copyId
   * @return
   */
  public Result bookmarkCopy(final Long copyId, final Http.Request request) {

    final Bookmark bookmark = Bookmark.bookmarkCopy(copyId, request);
    if (bookmark == null) {
      return badRequest();
    }

    String msg = messagesApi.preferred(request).at("msg.success.bookmarkAdded", bookmark.copy.movie.title);
    request.flash().adding("success", msg);

    cacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS, request);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }

  /**
   * Removes a {@link Bookmark}
   *
   * @param bookmarkId
   * @return
   */
  public Result removeBookmark(final Long bookmarkId, final Http.Request request) {

    String title = Bookmark.removeBookmark(bookmarkId, request);

    String msg = messagesApi.preferred(request).at("msg.success.bookmarkRemoved", title);
    request.flash().adding("success", msg);

    cacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS, request);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }


}
