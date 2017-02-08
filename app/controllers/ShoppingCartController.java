package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import helpers.MailerHelper;
import models.CopyReservation;
import models.Dvd;
import models.User;
import objects.shoppingcart.CacheShoppingCart;
import play.Logger;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import play.twirl.api.Txt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

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

  /**
   * Helper for cached objects
   */
  private final CacheHelper cacheHelper;

  /**
   * The messages api
   */
  private final MessagesApi messagesApi;

  @Inject
  public ShoppingCartController(final MailerHelper mailerHelper, final CacheHelper cacheHelper, final MessagesApi messagesApi) {
    this.mailerHelper = mailerHelper;
    this.cacheHelper = cacheHelper;
    this.messagesApi = messagesApi;
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


    CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache();
    final Boolean addedToCart = shoppingCartFromCache.addItem(copyToBorrow);
    cacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache);

    return Results.ok(addedToCart.toString());
  }

  /**
   * Checks if the {@link Dvd} exists and if the user can borrow it or not at this moment
   * @param copyId
   * @return
   */
  @JSRoute
  public Result remCopyFromCart(final Long copyId) {
    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache();
    final Boolean removedFromCart = shoppingCartFromCache.removeItem(copyId);
    cacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache);

    return Results.ok(removedFromCart.toString());
  }

  /**
   * This is for displaying the shopping cart in the top main menu
   * @return
   */
  @JSRoute
  public Result getShoppingCartMenu() {
    return getShoppingCartMenuContent();
  }

  /**
   * Displays the current {@link CacheShoppingCart} and its items for checkout
   * @return
   */
  public Result showShoppingCart() {
    return ok(views.html.shoppingcart.showshoppingcart.render(cacheHelper.getShoppingCartFromCache()));
  }

  /**
   * Persists the shopping cart and displays the reservations
   * @return
   */
  public Result checkoutShoppingCart() {
    final CacheShoppingCart shoppingCart = cacheHelper.getShoppingCartFromCache();
    if(shoppingCart != null) {
      Set<User> owners = CopyReservation.createFromShoppingCart(shoppingCart);

      for (User owner : owners) {
        Txt emailTxt = views.txt.email.checkout.render(owner,User.getCurrentUser());
        mailerHelper.sendMail(messagesApi.preferred(request()).at("email.shoppingcart.subject"),owner.email,emailTxt.body(),false);
        
      }

      cacheHelper.removeSessionObj(ECacheObjectName.SHOPPINGCART);
    }


    return ok(views.html.shoppingcart.showshoppingcart.render(cacheHelper.getShoppingCartFromCache()));
  }

  /**
   * Gets the {@link CacheShoppingCart} from the cache and renders the content for the mainmenu
   * @return
   */
  public Result getShoppingCartMenuContent() {
    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache();
    return ok(views.html.shoppingcart.shoppingcartmenu.render(shoppingCartFromCache));
  }


}
