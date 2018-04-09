package models;

import io.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created with IntelliJ IDEA.
 * User: tuxburner
 * Date: 5/3/13
 * Time: 9:44 AM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@Table(name="comment")
public class Comment extends Model
{

  @Id
  public Long id;

  /**
   * The comment itself
   */
  @Column(nullable = false)
  public String comment;

  /**
   * The date the comment was created
   */
  @Column(nullable = false)
  public Long commentDate;

  /**
   * The user who created the comment
   */
  @Column(nullable =  false)
  public User user;

}
