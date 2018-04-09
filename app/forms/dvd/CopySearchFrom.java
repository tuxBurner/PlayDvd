package forms.dvd;

import com.google.gson.Gson;
import forms.dvd.objects.EDvdListOrderBy;
import forms.dvd.objects.EDvdListOrderHow;
import helpers.CacheHelper;
import helpers.DvdInfoHelper;
import helpers.ECacheObjectName;
import org.apache.commons.lang.StringUtils;
import play.mvc.Controller;
import play.mvc.Http.Context;

/**
 * This holds the filter for listing the dvd
 * 
 * @author tuxburner
 * 
 */
public class CopySearchFrom {

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
   * Age Rating of the movie
   */
  public String ageRating = null;

  /**
   * What field in the database to use to order the list
   */
  public EDvdListOrderBy orderBy = EDvdListOrderBy.DATE;

  /**
   * How to order the result list
   */
  public EDvdListOrderHow orderHow = EDvdListOrderHow.DOWN;

  /**
   * What copy type to display
   */
  public String copyType = null;

  /**
   * Checks if the searchForm should be displayed in the advanced mode
   */
  public static boolean displayAdvancedForm(final CacheHelper cacheHelper) {
    final CopySearchFrom form = CopySearchFrom.getCurrentSearchForm(cacheHelper);
    return (form != null && (StringUtils.isEmpty(form.copyType) == false || form.lendDvd == true));
  }

  /**
   * Gets the current search form from the cache if the cache is empty a new one
   * is created
   * 
   * @return
   */
  public static CopySearchFrom getCurrentSearchForm(final CacheHelper cacheHelper) {

    final Context ctx = Controller.ctx();

    if (ctx == null) {
      return null;
    }

    final CopySearchFrom returnVal = cacheHelper.getSessionObjectOrElse(ECacheObjectName.SEARCHFORM, () -> {
      final CopySearchFrom value = new CopySearchFrom();
      cacheHelper.setSessionObject(ECacheObjectName.SEARCHFORM, value);
      return value;
    });

    return returnVal;
  }

  /**
   * Write the {@link CopySearchFrom} to the cache for the user
   *
   * @param copySearchFrom
   */
  public static void setCurrentSearchForm(final CopySearchFrom copySearchFrom, final CacheHelper cacheHelper) {

    // make sure when set to lend and no username is given set it to the current
    // user
    if (copySearchFrom.lendDvd == true && StringUtils.isEmpty(copySearchFrom.userName) == true) {
      copySearchFrom.userName = Controller.request().username();
    }

    cacheHelper.setSessionObject(ECacheObjectName.SEARCHFORM, copySearchFrom);
  }

  public static String getAgeRatingsAsJson() {
    return new Gson().toJson(DvdInfoHelper.getAgeRatings());
  }

  public static String getCopyTypesJson() {
    return new Gson().toJson(DvdInfoHelper.getCopyTypes());
  }

  public String getSearchFor() {
    return searchFor;
  }

  public void setSearchFor(String searchFor) {
    this.searchFor = searchFor;
  }

  public int getCurrentPage() {
    return currentPage;
  }

  public void setCurrentPage(int currentPage) {
    this.currentPage = currentPage;
  }

  public String getGenre() {
    return genre;
  }

  public void setGenre(String genre) {
    this.genre = genre;
  }

  public String getActor() {
    return actor;
  }

  public void setActor(String actor) {
    this.actor = actor;
  }

  public String getDirector() {
    return director;
  }

  public void setDirector(String director) {
    this.director = director;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public boolean isLendDvd() {
    return lendDvd;
  }

  public void setLendDvd(boolean lendDvd) {
    this.lendDvd = lendDvd;
  }

  public boolean isMoviesToReview() {
    return moviesToReview;
  }

  public void setMoviesToReview(boolean moviesToReview) {
    this.moviesToReview = moviesToReview;
  }

  public String getAgeRating() {
    return ageRating;
  }

  public void setAgeRating(String ageRating) {
    this.ageRating = ageRating;
  }

  public EDvdListOrderBy getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(EDvdListOrderBy orderBy) {
    this.orderBy = orderBy;
  }

  public EDvdListOrderHow getOrderHow() {
    return orderHow;
  }

  public void setOrderHow(EDvdListOrderHow orderHow) {
    this.orderHow = orderHow;
  }

  public String getCopyType() {
    return copyType;
  }

  public void setCopyType(String copyType) {
    this.copyType = copyType;
  }


}
