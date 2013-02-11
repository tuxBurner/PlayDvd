package controllers;

import models.Dvd;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

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
  public Result addCopyToCart(final Long copyId) {

    Dvd dvdToBorrow = Dvd.getDvdToBorrow(copyId, Secured.getUsername());
    if(dvdToBorrow == null) {
      if(Logger.isErrorEnabled() == true) {
        Logger.error("Could not find dvd: "+copyId+" for adding it into the shopping cart");
        return Results.ok();
      }
    }

    return Results.ok();
  }

}
