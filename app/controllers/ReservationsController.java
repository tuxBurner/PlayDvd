package controllers;

import jsannotation.JSRoute;
import models.CopyReservation;
import models.Dvd;
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

  /**
   * Displays the overview over {@link CopyReservation} made by other {@link models.User}s and made by the current  {@link models.User}
   * @return
   */
  public static Result showReservations() {
    return ok(views.html.reservations.showreservations.render(CopyReservation.getReservations(),CopyReservation.getOwnReservations(), Dvd.getLentDvds()));
  }

  /**
   * Method for deleting a {@link CopyReservation} from the current {@link models.User}
   * @param reservationId
   * @return
   */
  @JSRoute
  public static  Result deleteOwnReservation(final Long reservationId) {
   CopyReservation.deleteOwnReservation(reservationId);
    return ok();
  }

  /**
   * Method for deleting a {@link CopyReservation} where the owner of the {@link models.Dvd} current {@link models.User}
   * @param reservationId
   * @return
   */
  @JSRoute
  public static  Result deleteReservation(final Long reservationId) {
    CopyReservation.deleteReservation(reservationId);
    return ok();
  }

}
