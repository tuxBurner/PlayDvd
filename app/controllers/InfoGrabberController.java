package controllers;

import grabbers.EGrabberType;
import grabbers.GrabberDisplayMovie;
import grabbers.GrabberException;
import grabbers.GrabberSearchMovie;
import grabbers.IInfoGrabber;
import grabbers.TheTvDbGrabber;
import grabbers.TmdbGrabber;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import views.html.grabber.displaymovie;
import views.html.grabber.search;

public class InfoGrabberController extends Controller {

  public final static String DVD_ID_FIELD_NAME = "movieToEditId";

  /**
   * This is called when the user wants to search the movie database
   * 
   * @param searchTerm
   * @return
   */
  public static Result searchGrabber(final String searchTerm, final String grabberType) {

    final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();

    try {
      List<GrabberSearchMovie> searchResults = new ArrayList<GrabberSearchMovie>();

      if (StringUtils.isEmpty(searchTerm) == false) {
        final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
        if (grabber != null) {
          searchResults = grabber.searchForMovie(searchTerm);
        }
      }

      return Results.ok(search.render(searchTerm, grabberType, searchResults, movieToEditId));

    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
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
   * @param tmdbDvdId
   * @return
   */
  private static Long getMovieToEditIdFromReq() {

    Long movieToEditId = null;

    if (Controller.request().queryString().containsKey(InfoGrabberController.DVD_ID_FIELD_NAME)) {
      final String[] strings = Controller.request().queryString().get(InfoGrabberController.DVD_ID_FIELD_NAME);
      if (strings != null && strings.length == 1 && StringUtils.isEmpty(strings[0]) == false) {
        movieToEditId = Long.valueOf(strings[0]);
      }
    }

    return movieToEditId;
  }

  /**
   * Display the movie which was selected
   * 
   * @param movieId
   * @return
   */
  public static Result getMovieById(final String grabberId, final String grabberType) {

    try {

      final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();

      final IInfoGrabber grabber = InfoGrabberController.getGrabber(EGrabberType.valueOf(grabberType));
      final GrabberDisplayMovie displayMovie = grabber.getDisplayMovie(grabberId);

      final String mode = (movieToEditId == null) ? DvdController.DVD_FORM_ADD_MODE : DvdController.DVD_FORM_EDIT_MODE;

      return Results.ok(displaymovie.render(displayMovie, grabberType, movieToEditId, mode));

    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }
  }

}
