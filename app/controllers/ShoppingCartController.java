package controllers;

import jsannotation.JSRoute;
import models.Dvd;
import objects.shoppingcart.CacheShoppingCart;
import objects.shoppingcart.CacheShoppingCartItem;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.api.templates.Html;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * This {@link Controller} handles when a user adds a copy of a movie into his shopping cart
 * and if he wants to send the owners a mail which movies he wants to borrow
 *
 * User: tuxburner
 * Date: 2/9/13
 * Time: 12:21 PM
 */
@Security.Authenticated(Secured.class)
public class ShoppingCartController extends Controller {

  private static final String CACHE_SHOPPING_CART_IDENT = "shopping.cart";

  /**
   * Checks if the {@link models.Dvd} exists and if the user can borrow it or not at this moment
   * @param copyId
   * @return
   */
  @JSRoute
  public static Result addCopyToCart(final Long copyId) {

    Dvd copyToBorrow = Dvd.getDvdToBorrow(copyId, Secured.getUsername());
    if(copyToBorrow == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find dvd: "+copyId+" for adding it into the shopping cart");
        return Results.ok("false");
      }
    }


    final CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    final Boolean addedToCart = shoppingCartFromCache.addItem(copyToBorrow);
    setShoppingCartToCache(shoppingCartFromCache);


    return Results.ok(addedToCart.toString());
  }

  /**
   * Checks if the {@link models.Dvd} exists and if the user can borrow it or not at this moment
   * @param copyId
   * @return
   */
  @JSRoute
  public static Result remCopyFromCart(final Long copyId) {
    final CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    final Boolean removedFromCart = shoppingCartFromCache.removeItem(copyId);
    setShoppingCartToCache(shoppingCartFromCache);

    return Results.ok(removedFromCart.toString());
  }

  /**
   * This is for displaying the shopping cart in the top main menu
   * @return
   */
  @JSRoute
  public static Result getShoppingCartMenu() {
    return ok(getShoppingCartMenuContent());
  }

  /**
   * Displays the current {@link CacheShoppingCart} and its items for checkout
   * @return
   */
  public static Result showShoppingCart() {
    return ok(views.html.shoppingcart.showshoppingcart.render(getShoppingCartFromCache()));
  }

  /**
   * Gets the {@link CacheShoppingCart} from the cache and renders the content for the mainmenu
   * @return
   */
  public static Html getShoppingCartMenuContent() {
    final CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    return views.html.shoppingcart.shoppingcartmenu.render(shoppingCartFromCache);
  }


  /**
   * Gets the shopping cart from the session
   * @return
   */
  public static CacheShoppingCart getShoppingCartFromCache() {

    final String uuid = createCacheUUID();

    // Access the cache
    CacheShoppingCart cart = (CacheShoppingCart) Cache.get(uuid + CACHE_SHOPPING_CART_IDENT);
    if(cart==null) {
      cart = new CacheShoppingCart();
      setShoppingCartToCache(cart);
    }

    return cart;
  }

  /**
   * Writes the shopping cart to the session cache of the user
   * @param cart
   */
  private static void setShoppingCartToCache(final CacheShoppingCart cart) {
    final String uuid = createCacheUUID();

    Cache.set(uuid+CACHE_SHOPPING_CART_IDENT, cart, 60 * 15);

  }

  /**
   * Checks if the user session has a uuid and if not it creates one for the cache
   * @return
   */
  private static String createCacheUUID() {
    // Generate a unique ID
    String uuid=session("uuid");
    if(StringUtils.isEmpty(uuid)) {
      uuid=java.util.UUID.randomUUID().toString();
      session("uuid", uuid);
    }

    return uuid;
  }

}
