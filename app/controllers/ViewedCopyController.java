package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import io.ebean.PagedList;
import models.Bookmark;
import models.Dvd;
import models.ViewedCopy;
import org.apache.commons.lang3.BooleanUtils;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.viewedcopy.markAsViewed;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Controller for handling viewed copies.
 * User: tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class ViewedCopyController extends Controller {

  private final CacheHelper cacheHelper;

  private final MessagesApi messagesApi;

  @Inject
  public ViewedCopyController(final CacheHelper cacheHelper, final MessagesApi messagesApi) {
    this.cacheHelper = cacheHelper;
    this.messagesApi = messagesApi;
  }

  /**
   * Lists all {@link models.Dvd}s the current user has marked as viewed
   *
   * @return
   */
  public Result getViewedCopiesForCurrentUser(final Integer page, final Http.Request request) {
    final PagedList<ViewedCopy> viewedCopiesForUser = ViewedCopy.getViewedCopiesForUser(page, request);
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.viewedcopy.viewedList.render(viewedCopiesForUser, null, request, messages));
  }

  /**
   * Marks the {@link models.Dvd} as viewed
   *
   * @param copyId
   * @return
   */
  @JSRoute
  public Result markCopyAsViewedDialog(final Long copyId, final Http.Request request) {

    final Dvd copy = Dvd.FINDER.byId(copyId);
    if (copy == null) {
      return internalServerError();
    }


    final Messages messages = this.messagesApi.preferred(request);
    final List<ViewedCopy> copyViewed = ViewedCopy.getCopyViewed(copy, request);
    final boolean copyBookmarkedByUser = Bookmark.isCopyBookmarkedByUser(copy, request);

    return ok(markAsViewed.render(copy, copyViewed, copyBookmarkedByUser, request, messages));
  }


  /**
   * Marks the {@link Dvd} as viewed
   *
   * @param copyId
   * @return
   */
  @JSRoute
  public Result doMarkCopyAsViewed(final Long copyId, final Boolean remBookMark, final Http.Request request) {
    final ViewedCopy viewedCopy = ViewedCopy.markCopyAsViewed(copyId, request);

    if (viewedCopy == null) {
      return internalServerError("Could not mark copy as viewed.");
    }

    if (BooleanUtils.isTrue(remBookMark)) {
      Bookmark.deleteAllBookmarksForCopy(viewedCopy.copy, request);
      cacheHelper.removeSessionObj(ECacheObjectName.BOOKMARKS, request);
    }

    return ok();
  }

}
