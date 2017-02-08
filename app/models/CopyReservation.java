package models;

import com.avaje.ebean.Model;
import objects.shoppingcart.CacheShoppingCart;
import objects.shoppingcart.CacheShoppingCartItem;
import org.apache.commons.collections.CollectionUtils;
import play.Logger;

import javax.persistence.*;
import java.util.*;

/**
 * This holds the infromation when a user persists his {@link objects.shoppingcart.CacheShoppingCart}
 *
 * User: tuxburner
 * Date: 3/17/13
 * Time: 3:43 PM
 */
@Entity
@Table(name="copy_reservation")
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

  private static Find<Long,CopyReservation> finder = new Find<Long, CopyReservation>() {};

  /**
   * Creates the reservation items
   * @param cart the shoppingcart
   * @return a set of the {@link User} owners of the movies
   */
  public static Set<User> createFromShoppingCart(final CacheShoppingCart cart) {
    if(cart != null && CollectionUtils.isEmpty(cart.getItems()) == false) {

      final long time = new Date().getTime();
      final User currentUser = User.getCurrentUser();

      if(currentUser == null) {
        if(Logger.isErrorEnabled()) {
          Logger.error("No current user found while persisting "+CacheShoppingCart.class.getCanonicalName());
        }
        return null;
      }

      final Set<User> movieOwners = new HashSet<>();

      for(final CacheShoppingCartItem item : cart.getItems()) {
        CopyReservation reservation = new CopyReservation();
        reservation.borrower = currentUser;
        reservation.copy = item.copyItem;
        reservation.date = time;

        movieOwners.add(item.copyItem.owner);

        reservation.save();
      }

      return movieOwners;
    }


    return new HashSet<>();
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
  public static void deleteReserved(Long reservationId) {
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

  /**
   * Deletes a {@link CopyReservation} where the {@link CopyReservation#copy} owner is the current {@link User}
   * @param reservationId
   * @param currentUser
   */
  public static void deleteReservation(Long reservationId, final User currentUser) {
    final CopyReservation reservation = finder.where().eq("copy.owner", currentUser).eq("id", reservationId).findUnique();
    if(reservation == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find "+CopyReservation.class.getName()+": "+reservationId+" where the copy.owner is: "+currentUser.userName);
      }
      return;
    }

    reservation.delete();
  }

  /**
   * Gets all {@link CopyReservation} where the {@link CopyReservation#copy} is the given copyId
   * @param copyId
   * @return
   */
  public static Map<String,String> getReservationsForCopy(final Long copyId) {
    final List<CopyReservation> list = finder.fetch("borrower", "userName").where().eq("copy.id", copyId).findList();
    final Map<String,String> result = new HashMap<String, String>();
    if(CollectionUtils.isEmpty(list) == false) {
      result.put("","");
      for(final CopyReservation reservation : list) {
        result.put(String.valueOf(reservation.id),reservation.borrower.userName);
      }
    }
    return result;
  }

  public static String getReservationBorrowerName(final Long reservationId) {
    final CopyReservation copyReservation = finder.fetch("borrower", "userName").where().eq("id", reservationId).findUnique();
    if(copyReservation == null) {
      return null;
    }

    return copyReservation.borrower.userName;
  }

  /**
   * Borrows the copy to the borrower and removes the reservation
   * @param reservationId
   * @param currentUser
   */
  public static void borrowReservation(Long reservationId, User currentUser) {
    final CopyReservation copyReservation = finder.where().eq("id", reservationId).eq("copy.owner", currentUser).findUnique();
    if(copyReservation == null) {
      return;
    }

    final Long borrowDate = copyReservation.copy.borrowDate;
    if(borrowDate != null) {
      return;
    }

    Dvd.lendDvdToUser(copyReservation.copy.id,currentUser.userName,copyReservation.borrower.userName,null,false);

    copyReservation.delete();
  }
}
