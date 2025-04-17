package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import dao.DvdDao;
import dao.UserDao;
import helpers.CacheHelper;
import helpers.ECacheObjectName;
import helpers.MailerHelper;
import models.CopyReservation;
import models.Dvd;
import models.User;
import objects.shoppingcart.CacheShoppingCart;
import play.Logger;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;
import play.twirl.api.Txt;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;

/**
 * This {@link Controller} handles when a user adds a copy of a movie into his shopping cart
 * and if he wants to send the owners a mail which movies he wants to borrow
 * <p>
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
   *
   * @param copyId
   * @return
   */
  @JSRoute
  public Result addCopyToCart(final Long copyId, final Http.Request request) {

    Dvd copyToBorrow = DvdDao.getDvdToBorrow(copyId, Secured.getUsernameStatic(request));
    if (copyToBorrow == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find dvd: " + copyId + " for adding it into the shopping cart");
        return Results.ok("false");
      }
    }


    CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache(request);
    final Boolean addedToCart = shoppingCartFromCache.addItem(copyToBorrow);
    cacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache, request);

    return Results.ok(addedToCart.toString());
  }

  /**
   * Checks if the {@link Dvd} exists and if the user can borrow it or not at this moment
   *
   * @param copyId
   * @return
   */
  @JSRoute
  public Result remCopyFromCart(final Long copyId, final Http.Request request) {
    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache(request);
    final Boolean removedFromCart = shoppingCartFromCache.removeItem(copyId);
    cacheHelper.setSessionObject(ECacheObjectName.SHOPPINGCART, shoppingCartFromCache, request);

    return Results.ok(removedFromCart.toString());
  }

  /**
   * This is for displaying the shopping cart in the top main menu
   *
   * @return
   */
  @JSRoute
  public Result getShoppingCartMenu(final Http.Request request) {
    return getShoppingCartMenuContent(request);
  }

  /**
   * Displays the current {@link CacheShoppingCart} and its items for checkout
   *
   * @return
   */
  public Result showShoppingCart(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.shoppingcart.showshoppingcart.render(cacheHelper.getShoppingCartFromCache(request), request, messages));
  }

  /**
   * Persists the shopping cart and displays the reservations
   *
   * @return
   */
  public Result checkoutShoppingCart(final Http.Request request) {
    final CacheShoppingCart shoppingCart = cacheHelper.getShoppingCartFromCache(request);

    final Messages messages = this.messagesApi.preferred(request);
    if (shoppingCart != null) {
      Set<User> owners = CopyReservation.createFromShoppingCart(shoppingCart, request);

      for (User owner : owners) {
        Txt emailTxt = views.txt.email.checkout.render(owner, UserDao.findCurrentUser(request), request, messages);
        mailerHelper.sendMail(messagesApi.preferred(request).at("email.shoppingcart.subject"), owner.getEmail(), emailTxt.body(), false);
      }

      cacheHelper.removeSessionObj(ECacheObjectName.SHOPPINGCART, request);
    }

    return ok(views.html.shoppingcart.showshoppingcart.render(cacheHelper.getShoppingCartFromCache(request), request, messages));
  }

  /**
   * Gets the {@link CacheShoppingCart} from the cache and renders the content for the mainmenu
   *
   * @return
   */
  public Result getShoppingCartMenuContent(final Http.Request request) {
    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache(request);
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.shoppingcart.shoppingcartmenu.render(shoppingCartFromCache, request, messages));
  }


}
