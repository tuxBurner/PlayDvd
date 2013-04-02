package models;

import objects.shoppingcart.CacheShoppingCart;
import objects.shoppingcart.CacheShoppingCartItem;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;
import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.*;

/**
 * This holds the infromation when a user persists his {@link objects.shoppingcart.CacheShoppingCart}
 *
 * User: tuxburner
 * Date: 3/17/13
 * Time: 3:43 PM
 */
@Entity
public class CopyReservation extends Model {

  @Id
  public Long id;

  /**
  * The user who wants to borrow the copy
  */
  @ManyToOne
  @Column(nullable = false)
  public User borrower;

  /**
   * The copy the borrower wants to borrow
   */
  @ManyToOne
  @Column(nullable = false)
  public Dvd copy;

  /**
   * The date wen the borrower placed the reservation
   */
  @Column(nullable = false)
  public Long date;

  private static Finder<Long,CopyReservation> finder = new Finder<Long, CopyReservation>(Long.class,CopyReservation.class);

  /**
   * Creates the reservation items
   * @param cart
   */
  public static void createFromShoppingCart(final CacheShoppingCart cart) {
    if(cart != null && CollectionUtils.isEmpty(cart.getItems()) == false) {

      final long time = new Date().getTime();
      final User currentUser = User.getCurrentUser();

      if(currentUser == null) {
        if(Logger.isErrorEnabled()) {
          Logger.error("No current user found while persisting "+CacheShoppingCart.class.getCanonicalName());
        }
        return;
      }

      for(final CacheShoppingCartItem item : cart.getItems()) {
        CopyReservation reservation = new CopyReservation();
        reservation.borrower = currentUser;
        reservation.copy = item.copyItem;
        reservation.date = time;

        reservation.save();
      }
    }
  }

  /**
   * Gets the {@link CopyReservation}s where the owner for the {@link Dvd} is the current user
   * @return
   */
  public static Map<User, List<CopyReservation>> getReservations() {
    final User currentUser = User.getCurrentUser();
    final List<CopyReservation> list = finder.where().eq("copy.owner", currentUser).order("borrower").findList();
    final Map<User,List<CopyReservation>> result = new HashMap<User, List<CopyReservation>>();

    if(CollectionUtils.isEmpty(list) == false) {
      for(final CopyReservation reservation : list) {

        final User borrower = reservation.borrower;

        if(result.containsKey(borrower) == false) {
          result.put(borrower,new ArrayList<CopyReservation>());
        }

        result.get(borrower).add(reservation);
      }
    }

    return result;
  }

  /**
   * Gets the {@link CopyReservation}s where the owner for the {@link CopyReservation} is the current user
   * @return
   */
  public static List<CopyReservation> getOwnReservations() {
    final User currentUser = User.getCurrentUser();
    final List<CopyReservation> list = finder.where().eq("borrower", currentUser).order("date DESC").findList();

    return list;
  }

  /**
   * Gets the amount of {@link CopyReservation} where the current {@link User} is the owner
   * @return
   */
  public static int getReservationsCount() {
    final User currentUser = User.getCurrentUser();
    return finder.where().eq("copy.owner", currentUser).findRowCount();
  }

  /**
   * Gets the amount of {@link CopyReservation} wher the current {@link User} is the one reserved the {@link Dvd}
   * @return
   */
  public static int getReservedCount() {
    final User currentUser = User.getCurrentUser();
    return finder.where().eq("borrower", currentUser).findRowCount();
  }

  /**
   * Deletes a {@link CopyReservation} where the {@link CopyReservation#borrower} is the current {@link User}
   * @param reservationId
   */
  public static void deleteOwnReservation(Long reservationId) {
    final User currentUser = User.getCurrentUser();
    final CopyReservation reservation = finder.where().eq("borrower", currentUser).eq("id", reservationId).findUnique();
    if(reservation == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find "+CopyReservation.class.getName()+": "+reservationId+" where the owner is: "+currentUser.userName);
      }
      return;
    }

    reservation.delete();
  }

  public static void deleteReservation(Long reservationId) {
    final User currentUser = User.getCurrentUser();
    final CopyReservation reservation = finder.where().eq("copy.owner", currentUser).eq("id", reservationId).findUnique();
    if(reservation == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find "+CopyReservation.class.getName()+": "+reservationId+" where the copy.owner is: "+currentUser.userName);
      }
      return;
    }

    reservation.delete();
  }
}
