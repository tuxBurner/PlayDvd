package controllers;

import helpers.CacheHelper;
import helpers.ECacheObjectName;
import jsannotation.JSRoute;
import models.CopyReservation;
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


    CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    final Boolean addedToCart = shoppingCartFromCache.addItem(copyToBorrow);
    CacheHelper.setObjectToCache(ECacheObjectName.SHOPPINGCART,shoppingCartFromCache);

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
    CacheHelper.setObjectToCache(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache);

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
   * Persists the shopping cart and displays the reservations
   * @return
   */
  public static Result checkoutShoppingCart() {
    final CacheShoppingCart shoppingCart = getShoppingCartFromCache();
    if(shoppingCart != null) {
      CopyReservation.createFromShoppingCart(shoppingCart);
      CacheHelper.removeFromCache(ECacheObjectName.SHOPPINGCART);
    }

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
   * Gets the {@link CacheShoppingCart} from the {@link Cache} if it is null a new instance is created
   * @return
   */
  public static CacheShoppingCart  getShoppingCartFromCache(){
    CacheShoppingCart objectFromCache = CacheHelper.getObjectFromCache(ECacheObjectName.SHOPPINGCART);
    if(objectFromCache == null) {
      objectFromCache = new CacheShoppingCart();
    }

    return objectFromCache;
  }


}
