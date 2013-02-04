package forms.grabbers;

/**
 * Form which handles all the inputs for adding, editing a dvd
 * 
 * @author tuxburner
 * 
 */
public class GrabberInfoForm {

  /**
   * The id of the selected movieId
   */
  public String grabberMovieId;

  /**
   * The id of the selected backdrop
   */
  public String grabberBackDropId;

  /**
   * Id from youtube
   */
  public String grabberTrailerUrl;

  /**
   * Id of the selected poster
   */
  public String grabberPosterId;

  /**
   * If not <code>null</code> than we are in the edit mode we will set this to
   * the form
   */
  public Long movieToEditId;

}
