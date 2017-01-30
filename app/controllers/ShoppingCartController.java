package controllers;

import helpers.CacheHelper;
import helpers.ECacheObjectName;
import com.github.tuxBurner.jsAnnotations.JSRoute;
import helpers.MailerHelper;
import models.CopyReservation;

import models.Dvd;
import models.User;
import objects.shoppingcart.CacheShoppingCart;
import play.Logger;
import play.cache.Cache;
import play.i18n.Messages;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import play.twirl.api.Html;
import play.twirl.api.Txt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 *
 * This {@link Controller} handles when a user adds a copy of a movie into his shopping cart
 * and if he wants to send the owners a mail which movies he wants to borrow
 *
 * User: tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class ShoppingCartController extends Controller {


  /**
   * Helper for sending mails
   */
  private final MailerHelper mailerHelper;

  @Inject
  public ShoppingCartController(final MailerHelper mailerHelper) {

    this.mailerHelper = mailerHelper;
  }

  /**
   * Checks if the {@link Dvd} exists and if the user can borrow it or not at this moment
   * @param copyId
   * @return
   */
  @JSRoute
  public Result addCopyToCart(final Long copyId) {

    Dvd copyToBorrow = Dvd.getDvdToBorrow(copyId, Secured.getUsername());
    if(copyToBorrow == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find dvd: "+copyId+" for adding it into the shopping cart");
        return Results.ok("false");
      }
    }


    CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    final Boolean addedToCart = shoppingCartFromCache.addItem(copyToBorrow);
    CacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache);

    return Results.ok(addedToCart.toString());
  }

  /**
   * Checks if the {@link Dvd} exists and if the user can borrow it or not at this moment
   * @param copyId
   * @return
   */
  @JSRoute
  public Result remCopyFromCart(final Long copyId) {
    final CacheShoppingCart shoppingCartFromCache = getShoppingCartFromCache();
    final Boolean removedFromCart = shoppingCartFromCache.removeItem(copyId);
    CacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache);

    return Results.ok(removedFromCart.toString());
  }

  /**
   * This is for displaying the shopping cart in the top main menu
   * @return
   */
  @JSRoute
  public Result getShoppingCartMenu() {
    return ok(getShoppingCartMenuContent());
  }

  /**
   * Displays the current {@link CacheShoppingCart} and its items for checkout
   * @return
   */
  public Result showShoppingCart() {
    return ok(views.html.shoppingcart.showshoppingcart.render(getShoppingCartFromCache()));
  }

  /**
   * Persists the shopping cart and displays the reservations
   * @return
   */
  public Result checkoutShoppingCart() {
    final CacheShoppingCart shoppingCart = getShoppingCartFromCache();
    if(shoppingCart != null) {
      Set<User> owners = CopyReservation.createFromShoppingCart(shoppingCart);

      for (User owner : owners) {
        Txt emailTxt = views.txt.email.checkout.render(owner,User.getCurrentUser());
        mailerHelper.sendMail(Messages.get("email.shoppincart.subject"),owner.email,emailTxt.body(),false);
      }

      CacheHelper.removeSessionObj(ECacheObjectName.SHOPPINGCART);
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

    return CacheHelper.getSessionObjectOrElse(ECacheObjectName.SHOPPINGCART,callable);
  }

  private static  final Callable<CacheShoppingCart> callable = new Callable<CacheShoppingCart>() {
    @Override
    public CacheShoppingCart call() throws Exception {
      return new CacheShoppingCart();
    }
  };


}
