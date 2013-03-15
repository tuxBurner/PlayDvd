package objects.shoppingcart;

import models.Dvd;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.collections.MapUtils;
import play.Logger;

import java.util.*;

/**
 * Holds the {@link CacheShoppingCartItem} items and provides some methods on it
 * User: tuxburner
 * Date: 3/15/13
 * Time: 12:45 PM
 */
public class CacheShoppingCart {

  private final List<CacheShoppingCartItem> items = new ArrayList<CacheShoppingCartItem>();

  private final Set<Long> knownCopyIds = new HashSet<Long>();


  /**
   * Adds a {@link Dvd} to the {@link CacheShoppingCart}
   * @param copyItem
   */
  public Boolean addItem(final Dvd copyItem) {

    if(isInShoppingCart(copyItem.id) == true) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("The shopping cart already contains: "+copyItem.id+" ("+copyItem.movie.title+")");
      }
      return Boolean.FALSE;
    }

    CacheShoppingCartItem item = new CacheShoppingCartItem(copyItem);

    items.add(item);
    knownCopyIds.add(copyItem.id);

    return Boolean.TRUE;
  }

  /**
   * Removes {@link CacheShoppingCartItem} from the {@link CacheShoppingCart}
   * @param copyId
   * @return
   */
  public Boolean removeItem(final Long copyId) {
    if(isInShoppingCart(copyId) == false) {
      if(Logger.isDebugEnabled() == true) {
        Logger.debug("The shopping does not contain: "+copyId);
      }
      return Boolean.FALSE;
    }


    CacheShoppingCartItem removeItem = null;
    for(final CacheShoppingCartItem item : items) {
       if(item.copyItem.id.equals(copyId) == true) {
         removeItem = item;
         break;
       }
    }

    items.remove(removeItem);
    knownCopyIds.remove(copyId);


    return Boolean.TRUE;
  }

  /**
   * Checks if the {@link Dvd} is already in the shopping cart
   * @param copyId
   * @return
   */
  public boolean isInShoppingCart(final Long copyId) {
    return knownCopyIds.contains(copyId);
  }

  /**
   * Retruns the last five @{link CacheShoppingCartItem} in the cart for the menu
   * @return
   */
  public List<CacheShoppingCartItem> getLastFiveItems() {
    if(items.isEmpty() == true) {
      return null;
    }

    if(items.size() <= 5) {
      return items;
    }

    final List<CacheShoppingCartItem> returnList = new ArrayList<CacheShoppingCartItem>();
    for(int i = items.size() -1; i >= items.size() - 6; i--) {
      returnList.add(items.get(i));
    }

    return returnList;
  }

  /**
   * This creates a {@link Map} where the key is the name of the user owning the copy
   * @return
   */
  public Map<String,List<CacheShoppingCartItem>> getItemsSortedByUser() {
    final Map<String,List<CacheShoppingCartItem>> returnVal = new HashMap<String, List<CacheShoppingCartItem>>();

    for(final CacheShoppingCartItem item :  items) {
      final String ownerName = item.copyItem.owner.userName;
      if(returnVal.containsKey(ownerName) == false) {
        returnVal.put(ownerName,new ArrayList<CacheShoppingCartItem>());
      }

      returnVal.get(ownerName).add(item);
    }

    return returnVal;
  }

  /**
   * Returns the amount of {@link CacheShoppingCartItem} in the cart
   * @return
   */
  public int getSize() {
    return items.size();
  }

}
