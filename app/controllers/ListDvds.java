package controllers;

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Page;

import models.Dvd;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dashboard.listdvds;

@Security.Authenticated(Secured.class)
public class ListDvds extends Controller {

  /**
   * Lists all the dvds the user has
   * 
   * @return
   */
  public static Result listdvds(final Integer page) {
    final Page<Dvd> dvds = Dvd.getDvds(page);
    return ListDvds.returnList(dvds);
  }

  /**
   * Lists the dvds by the user
   * 
   * @param fromUserName
   * @return
   */
  public static Result listByUser(final String fromUserName, final Integer page) {
    if (StringUtils.isEmpty(fromUserName)) {
      return Results.internalServerError("No Username given");
    }

    final Page<Dvd> dvds = Dvd.getUserDvds(fromUserName, page);
    return ListDvds.returnList(dvds);
  }

  /**
   * List the dvds by the genre
   * 
   * @param genreName
   * @return
   */
  public static Result listByGenre(final String genreName, final Integer pageNr) {
    if (StringUtils.isEmpty(genreName)) {
      return Results.internalServerError("No Genrename given");
    }

    final Page<Dvd> dvds = Dvd.getByGenre(genreName, pageNr);

    return ListDvds.returnList(dvds);
  }

  /**
   * Returns the dvds for the template
   * 
   * @param page
   * @return
   */
  private static Result returnList(final Page<Dvd> page) {
    final String username = Controller.request().username();
    return Results.ok(listdvds.render(new DvdPage(page), username));
  }

}
