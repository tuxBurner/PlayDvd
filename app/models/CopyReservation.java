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
import java.util.Date;

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


}
