package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import dao.DvdDao;
import dao.UserDao;
import models.CopyReservation;
import models.Dvd;
import models.User;
import org.apache.commons.lang3.StringUtils;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;
import views.html.reservations.showborrowed;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * User: tuxburner
 * Date: 3/20/13
 * Time: 5:21 PM
 */
@Security.Authenticated(Secured.class)
@Singleton
public class ReservationsController extends Controller {

  private final MessagesApi messagesApi;

  @Inject
  ReservationsController(final MessagesApi messagesApi) {
    this.messagesApi = messagesApi;
  }


  /**
   * Displays the overview over {@link CopyReservation} made by other {@link models.User}s and made by the current  {@link models.User}
   *
   * @return
   */
  public Result showReservations(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.reservations.showreservations.render(CopyReservation.getReservations(request), request, messages));
  }


  /**
   * Shows an overview off all {@CopyReservation}s the current {@link models.User} has
   *
   * @return
   */
  public Result showReserved(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.reservations.showreserved.render(CopyReservation.getOwnReservations(request), request, messages));
  }

  /**
   * Displays all {@Dvd}s which the current {@link User} lent to somebody else
   *
   * @return
   */
  public Result showLentCopies(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return ok(views.html.reservations.lentcopies.render(DvdDao.getLentDvds(request), request, messages));
  }

  /**
   * Displays all {@Dvd}s which the which the current {@link User} borrowed from other {@link User}s
   *
   * @return
   */
  public Result showBorrowedCopies(final Http.Request request) {
    final Messages messages = this.messagesApi.preferred(request);
    return ok(showborrowed.render(DvdDao.getBorrowedDvds(request), request, messages));
  }

  /**
   * Method for deleting a {@link CopyReservation} from the current {@link models.User}
   *
   * @param reservationId
   * @return
   */
  @JSRoute
  public Result deleteReserved(final Long reservationId, final Http.Request request) {
    CopyReservation.deleteReserved(reservationId, request);
    return redirect(routes.ReservationsController.showReserved());
  }

  /**
   * Method for deleting  {@link CopyReservation}s where the owner of the {@link Dvd} current {@link models.User}
   *
   * @param reservationIds
   * @return
   */
  @JSRoute
  public Result deleteReservations(final String reservationIds, final Http.Request request) {
    if (StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = extractIds(reservationIds);
      final User currentUser = UserDao.findCurrentUser(request);
      for (final String id : ids) {
        if (StringUtils.isNumeric(id) == true) {
          CopyReservation.deleteReservation(Long.valueOf(id), currentUser);
        }
      }
    }

    return redirect(routes.ReservationsController.showReservations());
  }


  /**
   * Method for borrowing {@link Dvd}s from the {@link CopyReservation} from the given ids
   *
   * @param reservationIds
   * @return
   */
  @JSRoute
  public Result borrowReservations(final String reservationIds, final Http.Request request) {
    if (StringUtils.isEmpty(reservationIds) == false) {
      final String[] ids = extractIds(reservationIds);
      final User currentUser = UserDao.findCurrentUser(request);
      for (final String id : ids) {
        if (StringUtils.isNumeric(id) == true) {
          CopyReservation.borrowReservation(Long.valueOf(id), currentUser);
        }
      }
    }

    return redirect(routes.ReservationsController.showReservations());
  }

  /**
   * Method for unlenting {@link Dvd}s
   *
   * @param copyIds
   * @return
   */
  @JSRoute
  public Result unlentCopies(final String copyIds, final Http.Request request) {
    if (StringUtils.isEmpty(copyIds) == false) {
      final String[] ids = extractIds(copyIds);
      final User currentUser = UserDao.findCurrentUser(request);
      for (final String id : ids) {
        if (StringUtils.isNumeric(id) == true) {
          DvdDao.unlendDvdToUser(Long.valueOf(id), Secured.getUsernameStatic(request), false);
        }
      }
    }

    return redirect(routes.ReservationsController.showLentCopies());
  }

  /**
   * Extracts some ids from the given {@link String}
   *
   * @param idsToExtract
   * @return
   */
  private static String[] extractIds(final String idsToExtract) {
    String[] ids = StringUtils.split(idsToExtract, ',');

    return ids;

  }

}
