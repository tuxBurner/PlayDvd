package controllers;

import helpers.CacheHelper;
import helpers.ECacheObjectName;
import models.Bookmark;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.bookmarks.bookmarklist;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created with IntelliJ IDEA.
 * User: tuxburner
 * Date: 4/29/13
 * Time: 12:04 AM
 * To change this template use File | Settings | File Templates.
 */
@Security.Authenticated(Secured.class)
public class BookmarksController extends Controller {

  /**
   * Lists all the {@models.Bookmarks} which the current user created
   * @return
   */
  public static Result listBookmarks() {
    List<Bookmark> listForUser = Bookmark.getBookmarksForUser();

    return ok(bookmarklist.render(listForUser));
  }

  /**
   * Creates a {@link models.Bookmark}
   * @param copyId
   * @return
   */
  public static Result bookmarkCopy(final Long copyId) {

    final Bookmark bookmark = Bookmark.bookmarkCopy(copyId);
    if(bookmark == null) {
      return badRequest();
    }

    Controller.flash("success","Copy: "+ bookmark.copy.movie.title+" was bookmarked.");

    CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks());
  }

  /**
   * Removes a {@link Bookmark}
   * @param bookmarkId
   * @return
   */
  public static Result removeBookmark(final Long bookmarkId) {

    String title = Bookmark.removeBookmark(bookmarkId);

    Controller.flash("success","Bookmark for copy: "+title+" was removed");

    CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);

    return redirect(routes.BookmarksController.listBookmarks());
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
