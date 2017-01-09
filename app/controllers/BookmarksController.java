package controllers;

import com.avaje.ebean.PagedList;
import com.google.inject.Singleton;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import models.Bookmark;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.bookmarks.bookmarklist;

import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Controller which handles the bookmarks.
 * User: tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class BookmarksController extends Controller {

  /**
   * Lists all the {@models.Bookmarks} which the current user created
   * @return
   */
  public Result listBookmarks(final Integer page) {
    PagedList<Bookmark> listForUser = Bookmark.getBookmarksForUser(page);

    return ok(bookmarklist.render(listForUser,page));
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

    final String msg = Messages.get("msg.success.bookmarkAdded",bookmark.copy.movie.title);
    Controller.flash("success",msg);

    CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }

  /**
   * Removes a {@link Bookmark}
   * @param bookmarkId
   * @return
   */
  public Result removeBookmark(final Long bookmarkId) {

    String title = Bookmark.removeBookmark(bookmarkId);

    final String msg = Messages.get("msg.success.bookmarkRemoved",title);
    Controller.flash("success",msg);

    CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks(0));
  }

  /**
   * Gets all {@Dvd#id} which the user bookedmarked
   * @return
   */
  public static Set<Long> getBookmarkedCopyIds() {
    final Callable<Set<Long>> callable = new Callable<Set<Long>>() {
      @Override
      public Set<Long> call() throws Exception {
        return Bookmark.getBookmarkCopyIdsForUser();
      }
    };

    return CacheHelper.getSessionObjectOrElse(ECacheObjectName.BOOKMARKS,callable);
  }

}
