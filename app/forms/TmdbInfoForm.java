package forms;

/**
 * Form which handles all the inputs for adding, editing a dvd
 * 
 * @author tuxburner
 * 
 */
public class TmdbInfoForm {

  /**
   * The id of the selected movieId
   */
  public Integer movieId;

  /**
   * The id of the selected backdrop
   */
  public String tmdbBackDrop;

  /**
   * Id of the selected poster
   */
  public String tmdbPoster;

  /**
   * If not <code>null</code> than we are in the edit mode we will set this to
   * the form
   */
  public Long movieDbId;

}
