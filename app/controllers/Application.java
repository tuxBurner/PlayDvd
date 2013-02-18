package controllers;

import play.Routes;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

import static controllers.routes.javascript.*;

@Security.Authenticated(Secured.class)
public class Application extends Controller {



  public static Result index() {
    return redirect(routes.ListDvdsController.listAlldvds());
  }


  /**
   * Register the routes to certain stuff to the javascript routing so we can
   * reach it better from there
   *
   * @return
   */
  public static Result jsRoutes() {
    Controller.response().setContentType("text/javascript");
    return Results.ok(Routes.javascriptRouter(
        "jsRoutes",
        InfoGrabberController.searchGrabber(),
        InfoGrabberController.getMovieById(),
        Dashboard.displayDvd(),
        Dashboard.lendDialogContent(),
        Dashboard.unLendDialogContent(),
        Dashboard.lendDvd(),
        Dashboard.unlendDvd(),
        Dashboard.deleteDialogContent(),
        Dashboard.deleteDvd(),
        Dashboard.streamImage(),
        MovieController.showAddMovieForm(),
        MovieController.showEditMovieForm(),
        MovieController.addMovieByGrabberId(),
        MovieController.checkIfMovieAlreadyExists(),
        MovieController.addOrEditMovie(),
        MovieController.searchMoviesForDvdSelect(),
        MovieController.searchForMovieAttribute(),
        BarcodeController.displayBarcodeScaner(),
        DvdController.searchEanNr(),
        DvdController.showAddDvdByEanAndMovie()));
  }
}