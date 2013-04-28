package controllers;

import models.Bookmark;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.bookmarks.bookmarklist;

import java.util.List;

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

    return redirect(routes.BookmarksController.listBookmarks());
  }

}
