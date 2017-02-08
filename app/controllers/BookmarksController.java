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
   * The messages api
   */
  private final MessagesApi messagesApi;

  @Inject
  public BookmarksController(final MessagesApi messagesApi) {

    this.messagesApi = messagesApi;
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

    String msg = messagesApi.preferred(request()).at("msg.success.bookmarkRemoved",title);
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
