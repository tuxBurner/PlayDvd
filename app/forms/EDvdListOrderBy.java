package forms;

/**
 * {@link Enum} for ordering stuff the list of dvds
 * 
 * @author tuxburner
 * 
 */
public enum EDvdListOrderBy {

  MOVIE_TITLE("movie.title"),
  DATE("createdDate");

  /**
   * The field in the db this enum orders the list
   */
  public String dbField;

  private EDvdListOrderBy(final String dbField) {
    this.dbField = dbField;
  }

}
