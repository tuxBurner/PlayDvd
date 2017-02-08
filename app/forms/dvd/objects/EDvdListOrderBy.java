package forms.dvd.objects;

import com.google.gson.Gson;

/**
 * {@link Enum} for ordering stuff the list of dvds
 * 
 * @author tuxburner
 * 
 */
public enum EDvdListOrderBy {

  MOVIE_TITLE("movie.title"),
  DATE("createdDate"),
  IMDB("movie.imdbRating");
  //BORROW_DATE("borrowDate");

  /**
   * The field in the db this enum orders the list
   */
  public String dbField;

  EDvdListOrderBy(final String dbField) {
    this.dbField = dbField;
  }

  public static String getAsJson() {
    return new Gson().toJson(EDvdListOrderBy.values());
  }

}
