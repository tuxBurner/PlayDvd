package controllers;

import grabbers.EGrabberType;
import grabbers.GrabberDisplayMovie;
import grabbers.GrabberException;
import grabbers.GrabberSearchMovie;
import grabbers.IInfoGrabber;
import grabbers.MovieCombined;
import grabbers.TheTvDbGrabber;
import grabbers.TmdbGrabber;
import jsAnnotations.JSRoute;
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

  public final static String AMAZON_CODE = "amazonCode";

  public final static String COPY_ID = "copyId";

  /**
   * This is called when the user wants to search the movie database
   *
   * @param searchTerm
   * @return
   */
  @JSRoute
  public static Result searchGrabber(final String searchTerm, final String grabberType) {

    final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();
    final String amazonCode = request().getQueryString(AMAZON_CODE);
    final Long copyId = getCopyId();

    try {
      List<GrabberSearchMovie> searchResults = new ArrayList<GrabberSearchMovie>();

      if (StringUtils.isEmpty(searchTerm) == false) {
        final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
        if (grabber != null) {
          searchResults = grabber.searchForMovie(searchTerm);
        }
      }

      return Results.ok(search.render(searchTerm, grabberType, searchResults, movieToEditId,amazonCode,copyId));

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

    if (EGrabberType.MOVIECOMBINED.equals(grabberType)) {
      return new MovieCombined();
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
      final String amazonCode = Controller.request().getQueryString(AMAZON_CODE);
      final Long copyId = getCopyId();

      final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
      final GrabberDisplayMovie displayMovie = grabber.getDisplayMovie(grabberId);

      final String mode = (movieToEditId == null) ? DvdController.DVD_FORM_ADD_MODE : DvdController.DVD_FORM_EDIT_MODE;

      return Results.ok(displaymovie.render(displayMovie, grabberType, movieToEditId, mode,amazonCode,copyId));

    } catch (final GrabberException e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("Internal Error happened", e);
      }
      return Results.badRequest("Internal Error happened");
    }
  }

  /**
   * Gets the currnet copyId from the request
   * @return
   */
  private static Long getCopyId() {
    final String copyId = request().getQueryString(COPY_ID);
    final Long copyIdLong = (StringUtils.isEmpty(copyId) == false && StringUtils.isNumeric(copyId)) ? Long.valueOf(copyId) : null;
    return copyIdLong;
  }


}
