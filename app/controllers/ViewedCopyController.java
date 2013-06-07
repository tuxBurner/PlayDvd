package controllers;

import models.Dvd;
import models.ViewedCopy;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import plugins.jsannotation.JSRoute;
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
  public static Result getViewedCopiesForCurrentUser() {
    final List<ViewedCopy> viewedCopiesForUser = ViewedCopy.getViewedCopiesForUser();
    return ok();
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

    return ok(markAsViewed.render(copy));
  }


  /**
   * Marks the {@link models.Dvd} as viewed
   * @param copyId
   * @return
   */
  public static Result doMarkCopyAsViewed(final Long copyId) {
    final ViewedCopy viewedCopy = ViewedCopy.markCopyAsViewed(copyId);

    if (viewedCopy == null) {
      return internalServerError("Could not mark copy as viewed.");
    }

    return ok();
  }

}
