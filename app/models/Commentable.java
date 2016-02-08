package models;

import play.Logger;
import com.avaje.ebean.Model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: tuxburner
 * Date: 5/3/13
 * Time: 9:47 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
public class Commentable extends Model{

  @Id
  public Long id;

  /**
   * How many comments are attached to the entity
   */
  public Integer commentsCount;


  /**
   * The comments which are attached to the commentable entity
   */
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
  @OrderBy("commentDate DESC")
  public List<Comment> comments;

  /**
   * Adds a new comment to the entity
   * @param comment
   */
  public void addNewComment(final Comment comment) {
    if(comment.id != null) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Could not add comment because id was set already");
      }
      return;
    }

    if(comment.user == null) {
      if(Logger.isErrorEnabled()) {
        Logger.error("Could not add comment because no user was set");
      }
      return;
    }

    comment.commentDate = new Date().getTime();


    comments.add(comment);
    commentsCount = comments.size();

    save();
  }

}
