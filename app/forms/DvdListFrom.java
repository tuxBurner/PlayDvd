package forms;

import controllers.Secured;
import play.cache.Cache;
import play.mvc.Controller;
import play.mvc.Http.Context;

/**
 * This holds the filter for listing the dvd
 * 
 * @author tuxburner
 * 
 */
public class DvdListFrom {

  /**
   * If the user searched for a dvd
   */
  public String searchFor = null;

  /**
   * The currently viewed page
   */
  public int currentPage = 0;

  /**
   * If the user only wants to see a specific genre
   */
  public String genre = null;

  /**
   * If the user wants to see movies by an actor
   */
  public String actor = null;

  /**
   * If the user wants to see a movie by a director
   */
  public String director = null;

  /**
   * If the user wants to see dvds from a user
   */
  public String userName = null;

  /**
   * If set to true and username is set only lend dvds will be shown
   */
  public boolean lendDvd = false;

  /**
   * If set to true only dvds linked with movies to review are displayed
   */
  public boolean moviesToReview = false;

  /**
   * Gets the current search form from the cache if the cache is empty a new one
   * is created
   * 
   * @return
   */
  public static DvdListFrom getCurrentSearchForm() {

    final Context ctx = Controller.ctx();

    final Object object = Cache.get(ctx.session().get(Secured.AUTH_SESSION) + ".dvdlistform");
    DvdListFrom returnVal = null;
    if (object == null || object instanceof DvdListFrom == false) {
      returnVal = new DvdListFrom();
      Cache.set(ctx.session().get(Secured.AUTH_SESSION) + ".dvdlistform", returnVal);
    } else {
      returnVal = (DvdListFrom) object;
    }

    return returnVal;
  }

  /**
   * Write the {@link DvdListFrom} to the cache for the user
   * 
   * @param ctx
   * @param dvdListFrom
   */
  public static void setCurrentSearchForm(final DvdListFrom dvdListFrom) {
    Cache.set(Controller.ctx().session().get(Secured.AUTH_SESSION) + ".dvdlistform", dvdListFrom);
  }

}
