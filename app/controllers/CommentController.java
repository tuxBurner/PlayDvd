package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Singleton;
import models.Commentable;
import models.Movie;
import play.data.DynamicForm;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import static play.data.Form.form;

/**
 * Controller for handling {@link models.Comment}
 * User: tuxburner
 * Date: 5/3/13
 * Time: 4:57 PM
 * To change this template use File | Settings | File Templates.
 */
@Security.Authenticated(Secured.class)
@Singleton
public class CommentController extends Controller {

  public static String COMMENT_SUCCESS_FLASH = "commentSuccess";

  @JSRoute
  public Result addComment(final Long movieId) {

    DynamicForm requestData = form().bindFromRequest();
    final String commentText = requestData.get("commentText");

    final Commentable commentable = Movie.addComment(movieId, commentText);

    flash(COMMENT_SUCCESS_FLASH,"Comment was added to movie.");

    return ok(views.html.dashboard.comments.displayComments.render(commentable,movieId));
  }

}
