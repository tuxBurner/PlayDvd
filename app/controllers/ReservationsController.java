package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import models.CopyReservation;
import models.Dvd;
import models.User;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import views.html.reservations.showborrowed;

import javax.inject.Singleton;

/**
 * User: tuxburner
 * Date: 3/20/13
 * Time: 5:21 PM
 */
@Security.Authenticated(Secured.class)
@Singleton
public class ReservationsController extends Controller {

  /**
   * Displays the overview over {@link CopyReservation} made by other {@link models.User}s and made by the current  {@link models.User}
   * @return
   */
  public Result showReservations() {
    return ok(views.html.reservations.showreservations.render(CopyReservation.getReservations()));
  }


  /**
   * Shows an overview off all {@CopyReservation}s the current {@link models.User} has
   * @return
   */
  public Result showReserved() {
    return ok(views.html.reservations.showreserved.render(CopyReservation.getOwnReservations()));
  }

  /**
   * Displays all {@Dvd}s which the current {@link User} lent to somebody else
   * @return
   */
  public Result showLentCopies() {
    return ok(views.html.reservations.lentcopies.render(Dvd.getLentDvds()));
  }

  /**
   * Displays all {@Dvd}s which the which the current {@link User} borrowed from other {@link User}s
   * @return
   */
  public Result showBorrowedCopies() {
    return ok(showborrowed.render(Dvd.getBorrowedDvds()));
  }

  /**
   * Method for deleting a {@link CopyReservation} from the current {@link models.User}
   * @param reservationId
   * @return
   */
  @JSRoute
  public Result deleteReserved(final Long reservationId) {
   CopyReservation.deleteReserved(reservationId);
    return redirect(routes.ReservationsController.showReserved());
  }

  /**
   * Method for deleting  {@link CopyReservation}s where the owner of the {@link models.Dvd} current {@link models.User}
   * @param reservationIds
   * @return
   */
  @JSRoute
  public Result deleteReservations(final String reservationIds) {
    if(StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = extractIds(reservationIds);
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
  public Result borrowReservations(final String reservationIds) {
    if(StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = extractIds(reservationIds);
      final User currentUser = User.getCurrentUser();
      for(final String id : ids) {
        if(StringUtils.isNumeric(id) == true) {
          CopyReservation.borrowReservation(Long.valueOf(id),currentUser);
        }
      }
    }

    return redirect(routes.ReservationsController.showReservations());
  }

  /**
   * Method for unlenting {@link models.Dvd}s
   * @param reservationIds
   * @return
   */
  @JSRoute
  public Result unlentCopies(final String copyIds) {
    if(StringUtils.isEmpty(copyIds) == false) {
      final String[] ids = extractIds(copyIds);
      final User currentUser = User.getCurrentUser();
      for(final String id : ids) {
        if(StringUtils.isNumeric(id) == true) {
          Dvd.unlendDvdToUser(Long.valueOf(id),Secured.getUsername(),false);
        }
      }
    }

    return redirect(routes.ReservationsController.showLentCopies());
  }

  /**
   * Extracts some ids from the given {@link String}
   * @param idsToExtract
   * @return
   */
  private static String[] extractIds(final String idsToExtract) {
    String[] ids = StringUtils.split(idsToExtract,',');

    return  ids;

  }

}
