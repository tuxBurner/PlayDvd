package controllers;

import com.avaje.ebean.PagedList;
import com.google.inject.Singleton;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import models.Bookmark;
import play.i18n.MessagesApi;
import play.mvc.Controller;
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
   * @return
   */
  public Result listBookmarks(final Integer page) {
    PagedList<Bookmark> listForUser = Bookmark.getBookmarksForUser(page);

    return ok(views.html.bookmarks.bookmarklist.render(listForUser,page));
  }

  /**
   * Creates a {@link models.Bookmark}
   * @param copyId
   * @return
   */
  public Result bookmarkCopy(final Long copyId) {

    final Bookmark bookmark = Bookmark.bookmarkCopy(copyId);
    if(bookmark == null) {
      return badRequest();
    }

    String msg = messagesApi.preferred(request()).at("msg.success.bookmarkAdded",bookmark.copy.movie.title);
    Controller.flash("success",msg);

    cacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }

  /**
   * Removes a {@link Bookmark}
   * @param bookmarkId
   * @return
   */
  public Result removeBookmark(final Long bookmarkId) {

    String title = Bookmark.removeBookmark(bookmarkId);

    String msg = messagesApi.preferred(request()).at("msg.success.bookmarkRemoved",title);
    Controller.flash("success",msg);

    cacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }

  
  
}
