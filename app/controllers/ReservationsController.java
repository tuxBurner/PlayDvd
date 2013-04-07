package controllers;

import jsannotation.JSRoute;
import models.CopyReservation;
import models.Dvd;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.reservations.showborrowed;

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
    return ok(views.html.reservations.showreservations.render(CopyReservation.getReservations()));
  }


  /**
   * Shows an overview off all {@CopyReservation}s the current {@link models.User} has
   * @return
   */
  public static Result showReserved() {
    return ok(views.html.reservations.showreserved.render(CopyReservation.getOwnReservations()));
  }

  /**
   * Displays all {@Dvd}s which the current {@link User} lent to somebody else
   * @return
   */
  public static Result showLentCopies() {
    return ok(views.html.reservations.lentcopies.render(Dvd.getLentDvds()));
  }

  /**
   * Displays all {@Dvd}s which the which the current {@link User} borrowed from other {@link User}s
   * @return
   */
  public static Result showBorrowedCopies() {
    return ok(showborrowed.render(Dvd.getBorrowedDvds()));
  }

  /**
   * Method for deleting a {@link CopyReservation} from the current {@link models.User}
   * @param reservationId
   * @return
   */
  @JSRoute
  public static  Result deleteReserved(final Long reservationId) {
   CopyReservation.deleteReserved(reservationId);
    return redirect(routes.ReservationsController.showReserved());
  }

  /**
   * Method for deleting  {@link CopyReservation}s where the owner of the {@link models.Dvd} current {@link models.User}
   * @param reservationIds
   * @return
   */
  @JSRoute
  public static  Result deleteReservations(final String reservationIds) {
    if(StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = StringUtils.split(reservationIds,',');
      final User currentUser = User.getCurrentUser();
      for(final String id : ids) {
        if(StringUtils.isNumeric(id) == true) {
          CopyReservation.deleteReservation(Long.valueOf(id),currentUser);
        }
      }
    }

    return redirect(routes.ReservationsController.showReservations());
  }


  /**
   * Method for borrowing {@link models.Dvd}s from the {@link CopyReservation} from the given ids
   * @param reservationIds
   * @return
   */
  @JSRoute
  public static Result borrowReservations(final String reservationIds) {
    if(StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = StringUtils.split(reservationIds,',');
      final User currentUser = User.getCurrentUser();
      for(final String id : ids) {
        if(StringUtils.isNumeric(id) == true) {
          CopyReservation.borrowReservation(Long.valueOf(id),currentUser);
        }
      }
    }

    return redirect(routes.ReservationsController.showReservations());
  }

}
