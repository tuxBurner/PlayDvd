package controllers;

import models.CopyReservation;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * User: tuxburner
 * Date: 3/20/13
 * Time: 5:21 PM
 */
@Security.Authenticated(Secured.class)
public class ReservationsController extends Controller {

  public static Result showReservations() {
    return ok(views.html.reservations.showreservations.render(CopyReservation.getReservations()));
  }

}
