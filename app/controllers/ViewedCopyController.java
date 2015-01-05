package controllers;

import com.avaje.ebean.Page;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import jsAnnotations.JSRoute;
import models.Bookmark;
import models.Dvd;
import models.ViewedCopy;
import org.apache.commons.lang.BooleanUtils;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.viewedcopy.markAsViewed;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tuxburner
 * Date: 6/7/13
 * Time: 4:45 PM
 * To change this template use File | Settings | File Templates.
 */
@Security.Authenticated(Secured.class)
public class ViewedCopyController extends Controller {

  /**
   * Lists all {@link models.Dvd}s the current user has marked as viewed
   *
   * @return
   */
  public static Result getViewedCopiesForCurrentUser(final Integer page) {
    final Page<ViewedCopy> viewedCopiesForUser = ViewedCopy.getViewedCopiesForUser(page);
    return ok(views.html.viewedcopy.viewedList.render(viewedCopiesForUser,null));
  }

  /**
   * Marks the {@link models.Dvd} as viewed
   * @param copyId
   * @return
   */
  @JSRoute
  public static Result markCopyAsViewedDialog(final Long copyId) {

    final Dvd copy = Dvd.find.byId(copyId);
    if(copy == null) {
      return internalServerError();
    }


    final List<ViewedCopy> copyViewed = ViewedCopy.getCopyViewed(copy);
    final boolean copyBookmarkedByUser = Bookmark.isCopyBookmarkedByUser(copy);

    return ok(markAsViewed.render(copy,copyViewed,copyBookmarkedByUser));
  }


  /**
   * Marks the {@link models.Dvd} as viewed
   * @param copyId
   * @return
   */
  @JSRoute
  public static Result doMarkCopyAsViewed(final Long copyId, final Boolean remBookMark) {
    final ViewedCopy viewedCopy = ViewedCopy.markCopyAsViewed(copyId);

    if (viewedCopy == null) {
      return internalServerError("Could not mark copy as viewed.");
    }

    if(BooleanUtils.isTrue(remBookMark)) {
      Bookmark.deletAllBookmarksForCopy(viewedCopy.copy);
      CacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS);
    }

    return ok();
  }

}
