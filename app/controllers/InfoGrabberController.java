package controllers;

import grabbers.*;
import jsannotation.JSRoute;
import org.apache.commons.lang.StringUtils;
import play.Logger;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.grabber.displaymovie;
import views.html.grabber.search;

import java.util.ArrayList;
import java.util.List;

@Security.Authenticated(Secured.class)
public class InfoGrabberController extends Controller {

  public final static String MOVIE_TO_EDIT_ID = "movieToEditId";

  public final static String EAN_NR = "eanNr";

  /**
   * This is called when the user wants to search the movie database
   *
   * @param searchTerm
   * @return
   */
  @JSRoute
  public static Result searchGrabber(final String searchTerm, final String grabberType) {

    final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();
    final String eanNr = Controller.request().getQueryString(EAN_NR);

    try {
      List<GrabberSearchMovie> searchResults = new ArrayList<GrabberSearchMovie>();

      if (StringUtils.isEmpty(searchTerm) == false) {
        final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
        if (grabber != null) {
          searchResults = grabber.searchForMovie(searchTerm);
        }
      }

      return Results.ok(search.render(searchTerm, grabberType, searchResults, movieToEditId,eanNr));

    } catch (final GrabberException e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("Internal Error happened", e);
      }
      return Results.badRequest("Internal Error happened");
    }
  }

  /**
   * Gets the grabber for the given type
   *
   * @param grabberType
   * @return
   */
  public static IInfoGrabber getGrabber(final EGrabberType grabberType) {

    if (EGrabberType.TMDB.equals(grabberType)) {
      return new TmdbGrabber();
    }

    if (EGrabberType.THETVDB.equals(grabberType)) {
      return new TheTvDbGrabber();
    }

    return null;
  }

  /**
   * This gets the dvdToEdit from the Request
   *
   * @param
   * @return
   */
  private static Long getMovieToEditIdFromReq() {

    Long movieToEditId = null;
    final String movieToEditIdString = Controller.request().getQueryString(InfoGrabberController.MOVIE_TO_EDIT_ID);

    if (StringUtils.isEmpty(movieToEditIdString) == false && StringUtils.isNumeric(movieToEditIdString)) {
        movieToEditId = Long.valueOf(movieToEditIdString);
      }


    return movieToEditId;
  }

  /**
   * Display the movie which was selected
   *
   * @param grabberId
   * @param grabberType
   * @return
   */
  @JSRoute
  public static Result getMovieById(final String grabberId, final String grabberType) {

    try {

      final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();
      final String eanNr = Controller.request().getQueryString(EAN_NR);

      final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
      final GrabberDisplayMovie displayMovie = grabber.getDisplayMovie(grabberId);

      final String mode = (movieToEditId == null) ? DvdController.DVD_FORM_ADD_MODE : DvdController.DVD_FORM_EDIT_MODE;

      return Results.ok(displaymovie.render(displayMovie, grabberType, movieToEditId, mode,eanNr));

    } catch (final GrabberException e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("Internal Error happened", e);
      }
      return Results.badRequest("Internal Error happened");
    }
  }


}
