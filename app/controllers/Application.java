package controllers;

import jsannotation.JSRoutesPlugin;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

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
   return JSRoutesPlugin.getJsRoutesResult();
  }
}