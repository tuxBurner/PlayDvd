package models;


import controllers.Secured;
import io.ebean.Ebean;
import io.ebean.Finder;
import io.ebean.Model;
import io.ebean.PagedList;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * This holds the infromation when a user marks an own copy to view in the future
 *
 * User: tuxburner
 * Date: 3/17/13
 * Time: 3:43 PM
 */
@Entity
public class Bookmark extends Model
{

  @Id
  public Long id;


  /**
   * The copy the user wants to view
   */
  @ManyToOne
  @Column(nullable = false)
  public Dvd copy;

  /**
   * The date when the user marked the copy to view
   */
  @Column(nullable = false)
  public Long date;

  private static Finder<Long, Bookmark> FINDER = new Finder<>(Bookmark.class);

  /**
   * Creates the
   *
   * @param copyId
   */
  public static Bookmark bookmarkCopy(Long copyId)
  {

    if (copyId == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("No copyId given to mark to view in future");
      }
      return null;
    }

    String username = Secured.getUsername();

    // check if the user already marked the copy as to view
    Bookmark bookmarkCheck = FINDER.query()
      .where()
      .eq("copy.owner.userName", username)
      .eq("copy.id", copyId)
      .findOne();

    if (bookmarkCheck != null) {
      if (Logger.isInfoEnabled() == true) {
        Logger.info("Copy: " + copyId + " already marked by the user to view in the future, setting current date.");
      }

      bookmarkCheck.date = new Date().getTime();
      bookmarkCheck.update();
      return bookmarkCheck;
    }

    Dvd copy = Dvd.getDvdForUser(copyId, username);
    if (copy == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find copy for mark to view");
      }
      return null;
    }

    Bookmark bookmark = new Bookmark();
    bookmark.copy = copy;
    bookmark.date = new Date().getTime();

    bookmark.save();

    return bookmark;
  }


  /**
   * Gets all  {@link Bookmark} where the owner of the {@link Dvd} is the current {@link User}
   *
   * @return
   */
  public static PagedList<Bookmark> getBookmarksForUser(final Integer page)
  {
    String username = Secured.getUsername();
    return FINDER.query().where()
      .eq("copy.owner.userName", username)
      .orderBy("date DESC")
      .setFirstRow(page*10)
      .setMaxRows(10)
      .findPagedList();

  }

  /**
   * Gets all {@link Dvd#id} where the owner of the {@link Dvd} is the current {@link User}
   *
   * @return
   */
  public static Set<Long> getBookmarkCopyIdsForUser()
  {
    final String username = Secured.getUsername();
    final Set<Bookmark> set = FINDER.query()
      .fetch("copy", "id")
      .where()
      .eq("copy.owner.userName", username)
      .findSet();

    final Set<Long> copyIds = new HashSet<Long>();

    if (CollectionUtils.isEmpty(set)) {
      return copyIds;
    }

    for (final Bookmark bookmark : set) {
      copyIds.add(bookmark.copy.id);
    }

    return copyIds;
  }


  /**
   * Gets the amount of {@link Bookmark} the current {@link User} has
   *
   * @return
   */
  public static int getBookmarkCount()
  {
    String username = Secured.getUsername();
    return FINDER.query().where()
      .eq("copy.owner.userName", username)
      .findCount();
  }

  /**
   * Check if the current {@link User} has bookmarked the {@link Dvd}
   *
   * @return
   */
  public static boolean isCopyBookmarkedByUser(final Dvd copy)
  {
    String username = Secured.getUsername();
    final int count = FINDER.query().where()
      .eq("copy.owner.userName", username)
      .eq("copy", copy)
      .findCount();
    return (count != 0);
  }

  /**
   * Removes the {@link Bookmark} from the list
   *
   * @param id
   */
  public static String removeBookmark(final Long id)
  {
    String username = Secured.getUsername();
    Bookmark bookmarkToDelete = FINDER.query().where()
      .eq("copy.owner.userName", username)
      .eq("id", id)
      .findOne();
    if (bookmarkToDelete == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find " + Bookmark.class.getName() + " with id: " + id + " for user: " + username);
      }
      return "";
    }

    String title = bookmarkToDelete.copy.movie.title;

    bookmarkToDelete.delete();

    return title;
  }

  /**
   * Deletes all the bookmarks for the {@link Dvd} where the owner is the current {@link User}
   *
   * @param copy
   */
  public static void deletAllBookmarksForCopy(final Dvd copy)
  {
    String username = Secured.getUsername();
    final Set<Bookmark> bookmarks = FINDER.query().where()
      .eq("copy.owner.userName", username)
      .eq("copy", copy)
      .findSet();
    if (CollectionUtils.isEmpty(bookmarks) == false) {
      Ebean.delete(bookmarks);
    }
  }

}
