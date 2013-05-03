package controllers;

import jsannotation.JSRoute;
import models.Movie;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

/**
 * Controller for handling {@link models.Comment}
 * User: tuxburner
 * Date: 5/3/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
@Security.Authenticated(Secured.class)
public class CommentController extends Controller {

  @JSRoute
  public static Result addComment(final Long movieId, final String commentText) {
    Movie.addComment(movieId,commentText);

    return ok();
  }

}
