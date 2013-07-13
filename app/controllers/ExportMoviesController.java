package controllers;


import play.mvc.Controller;
import play.mvc.Result;

/**
 * User: tuxburner
 * Date: 7/13/13
 * Time: 3:43 PM
 */
public class ExportMoviesController extends Controller {

  public static Result displayExportOptions() {

    return ok(views.html.export.export.render());
  }

  public static Result exportXbmc() {
    return play.mvc.Results.TODO;
  }
}
