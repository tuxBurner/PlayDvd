package controllers;

import grabbers.GrabberException;
import grabbers.TmdbInfoGrabber;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;

import views.html.grabber.*;

import com.github.savvasdalkitsis.jtmdb.Movie;

public class InfoGrabberController extends Controller {

  public final static String DVD_ID_FIELD_NAME = "tmdbMovieId";

  /**
   * This is called when the user wants to search the movie database
   * 
   * @param searchTerm
   * @return
   */
  public static Result searchTmdb(final String searchTerm) {

    final Long tmdbDvdId = InfoGrabberController.getMovieToEditIdFromReq();

    try {
      List<Movie> searchForMovie = new ArrayList<Movie>();

      if (StringUtils.isEmpty(searchTerm) == false) {
        searchForMovie = TmdbInfoGrabber.searchForMovie(searchTerm);
      }

      return Results.ok(search.render(searchTerm, searchForMovie, tmdbDvdId));

    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }
  }

  /**
   * This gets the dvdToEdit from the Request
   * 
   * @param tmdbDvdId
   * @return
   */
  private static Long getMovieToEditIdFromReq() {

    Long tmdbDvdId = null;

    if (Controller.request().queryString().containsKey(InfoGrabberController.DVD_ID_FIELD_NAME)) {
      final String[] strings = Controller.request().queryString().get(InfoGrabberController.DVD_ID_FIELD_NAME);
      if (strings != null && strings.length == 1 && StringUtils.isEmpty(strings[0]) == false) {
        tmdbDvdId = Long.valueOf(strings[0]);
      }
    }
    return tmdbDvdId;
  }

  /**
   * Display the movie which was selected
   * 
   * @param movieId
   * @return
   */
  public static Result getMovieById(final Integer movieId) {

    try {

      final Long movieToEditId = InfoGrabberController.getMovieToEditIdFromReq();

      final Movie movieInfo = TmdbInfoGrabber.getMovieInfo(movieId);

      final String mode = (movieToEditId == null) ? DvdController.DVD_FORM_ADD_MODE : DvdController.DVD_FORM_EDIT_MODE;

      return Results.ok(displaymovie.render(movieInfo, movieToEditId, mode));

    } catch (final GrabberException e) {
      return Results.badRequest("Internal Error happend");
    }
  }

}
