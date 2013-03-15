package objects.shoppingcart;

import models.Dvd;

import java.util.Date;

/**
 * This is hold in the cache when the user adds a {@link models.Dvd} to the shopping cart in the cache
 * User: tuxburner
 * Date: 3/15/13
 * Time: 12:21 PM
 */
public class CacheShoppingCartItem implements  Comparable<CacheShoppingCartItem> {

  final public Dvd copyItem;

  final public Long addedTimeStamp;

  public CacheShoppingCartItem(Dvd copyItem) {
    this.copyItem = copyItem;
    this.addedTimeStamp = new Date().getTime();
  }

  @Override
  public int compareTo(CacheShoppingCartItem compareItem) {
    return this.addedTimeStamp.compareTo(compareItem.addedTimeStamp);
  }
}
