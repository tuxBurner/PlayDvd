package controllers;

import forms.user.LoginForm;
import forms.user.RegisterForm;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.user.login;
import views.html.user.register;

public class Application extends Controller {


  @Security.Authenticated(Secured.class)
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
        controllers.routes.javascript.InfoGrabberController.searchGrabber(),
        controllers.routes.javascript.InfoGrabberController.getMovieById(),
        controllers.routes.javascript.Dashboard.displayDvd(),
        controllers.routes.javascript.Dashboard.lendDialogContent(),
        controllers.routes.javascript.Dashboard.unLendDialogContent(),
        controllers.routes.javascript.Dashboard.lendDvd(),
        controllers.routes.javascript.Dashboard.unlendDvd(),
        controllers.routes.javascript.Dashboard.deleteDialogContent(),
        controllers.routes.javascript.Dashboard.deleteDvd(),
        controllers.routes.javascript.Dashboard.streamImage(),
        controllers.routes.javascript.MovieController.showAddMovieForm(),
        controllers.routes.javascript.MovieController.showEditMovieForm(),
        controllers.routes.javascript.MovieController.addMovieByGrabberId(),
        controllers.routes.javascript.MovieController.addOrEditMovie(),
        controllers.routes.javascript.MovieController.searchMoviesForDvdSelect(),
        controllers.routes.javascript.MovieController.searchForMovieAttribute()));
  }
}