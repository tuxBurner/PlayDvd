package models;

import com.avaje.ebean.PagedList;
import controllers.Secured;
import play.Logger;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.List;

/**
 * Here the user can mark a {@link Dvd} as viewed
 *
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

  private static Finder<Long, ViewedCopy> finder = new Finder<Long, ViewedCopy>(Long.class, ViewedCopy.class);

  /**
   * Creates the
   * @param copyId
   */
  public static ViewedCopy markCopyAsViewed(Long copyId) {

    if(copyId == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("No copyId given to mark to view in future");
      }
      return null;
    }


    Dvd copy = Dvd.find.byId(copyId);
    if(copy == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find copy with id: "+copyId+" for marking as viewed.");
      }
      return null;
    }

    final User currentUser = User.getCurrentUser();
    if(currentUser == null) {
      if(Logger.isErrorEnabled() == true) {
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
   * @param copy
   * @return
   */
  public static List<ViewedCopy> getCopyViewed(final Dvd copy) {
    String username = Secured.getUsername();
    return ViewedCopy.finder.where().ieq("user.userName", username).eq("copy", copy).orderBy("date DESC").findList();
  }

  /**
   * Gets all  {@link models.ViewedCopy} where the owner of the {@link Dvd} is the current {@link models.User}
   * @return
   */
  public static PagedList<ViewedCopy> getViewedCopiesForUser(final Integer page) {
    String username = Secured.getUsername();
    return ViewedCopy.finder.where().ieq("user.userName", username).orderBy("date DESC").findPagedList(page,10);
  }

  /**
   * Gets the count of viewed {@link Dvd}s off the current {@link User}
   * @return
   */
  public static int getCopiesViewedCount() {
    String username = Secured.getUsername();
    return ViewedCopy.finder.where().ieq("user.userName", username).findRowCount();
  }

}
