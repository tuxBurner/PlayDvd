package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Singleton;
import models.Commentable;
import models.Movie;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;

import static play.data.Form.form;

/**
 * Controller for handling {@link models.Comment}
 * User: tuxburner *
 */
@Security.Authenticated(Secured.class)
@Singleton
public class CommentController extends Controller {


  public static String COMMENT_SUCCESS_FLASH = "commentSuccess";

  /**
   * Factory handling form binding
   */
  private final FormFactory formFactory;

  @Inject
  public CommentController(final FormFactory formFactory) {

    this.formFactory = formFactory;
  }

  @JSRoute
  public Result addComment(final Long movieId) {

    DynamicForm requestData = formFactory.form().bindFromRequest();
    final String commentText = requestData.get("commentText");

    final Commentable commentable = Movie.addComment(movieId, commentText);

    flash(COMMENT_SUCCESS_FLASH,"Comment was added to movie.");

    return ok(views.html.dashboard.comments.displayComments.render(commentable,movieId));
  }

}
