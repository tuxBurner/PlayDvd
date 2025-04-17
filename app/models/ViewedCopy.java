package models;

import controllers.Secured;
import dao.UserDao;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import play.Logger;
import play.mvc.Http;

import java.util.Date;
import java.util.List;

/**
 * Here the user can mark a {@link Dvd} as viewed
 * <p>
 * User: tuxburner
 * Date: 3/17/13
 * Time: 3:43 PM
 */
@Entity
public class ViewedCopy extends Model {

  @Id
  public Long id;


  /**
   * The {@link User} which has seen the {@link Dvd}
   */
  @ManyToOne
  @Column(nullable = false)
  public User user;


  /**
   * The copy the user has seen
   */
  @ManyToOne
  @Column(nullable = false)
  public Dvd copy;

  /**
   * The date when the user marked the copy as viewed
   */
  @Column(nullable = false)
  public Long date;

  private static Finder<Long, ViewedCopy> FINDER = new Finder<>(ViewedCopy.class);

  /**
   * Creates the
   *
   * @param copyId
   */
  public static ViewedCopy markCopyAsViewed(final Long copyId, final Http.Request request) {

    if (copyId == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No copyId given to mark to view in future");
      }
      return null;
    }


    Dvd copy = Dvd.FINDER.byId(copyId);
    if (copy == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find copy with id: " + copyId + " for marking as viewed.");
      }
      return null;
    }

    final User currentUser = UserDao.findCurrentUser(request);
    if (currentUser == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find current user");
      }
      return null;
    }

    ViewedCopy viewedCopy = new ViewedCopy();
    viewedCopy.copy = copy;
    viewedCopy.date = new Date().getTime();
    viewedCopy.user = currentUser;

    viewedCopy.save();

    return viewedCopy;
  }


  /**
   * Gets the list when the current {@link User} has last seen the {@link Dvd}
   *
   * @param copy
   * @return
   */
  public static List<ViewedCopy> getCopyViewed(final Dvd copy, final Http.Request request) {
    String username = Secured.getUsernameStatic(request);
    return FINDER.query()
        .where()
        .ieq("user.userName", username)
        .eq("copy", copy)
        .orderBy("date DESC")
        .findList();
  }

  /**
   * Gets all  {@link models.ViewedCopy} where the owner of the {@link Dvd} is the current {@link models.User}
   *
   * @return
   */
  public static PagedList<ViewedCopy> getViewedCopiesForUser(final Integer page, final Http.Request request) {
    String username = Secured.getUsernameStatic(request);
    return FINDER.query()
        .where()
        .ieq("user.userName", username)
        .orderBy("date DESC")
        .setFirstRow(page * 10)
        .setMaxRows(10)
        .findPagedList();
  }

  /**
   * Gets the count of viewed {@link Dvd}s off the current {@link User}
   *
   * @return
   */
  public static int getCopiesViewedCount(final Http.Request request) {
    String username = Secured.getUsernameStatic(request);
    return FINDER.query()
        .where()
        .ieq("user.userName", username)
        .findCount();
  }

}
