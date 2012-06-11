package controllers;

import org.apache.commons.lang.StringUtils;

import com.avaje.ebean.Page;

import forms.DvdListFrom;

import models.Dvd;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.dashboard.listdvds;

@Security.Authenticated(Secured.class)
public class ListDvds extends Controller {

  /**
   * Lists all the dvds
   * 
   * @return
   */
  public static Result listdvds(final Integer page) {

    final DvdListFrom currentSearchForm = DvdListFrom.getCurrentSearchForm();

    if (page != null) {
      currentSearchForm.currentPage = page;
    }

    return ListDvds.returnList(currentSearchForm);
  }

  /**
   * list all dvds we have
   * 
   * @return
   */
  public static Result listAlldvds() {
    final DvdListFrom listFrom = new DvdListFrom();
    return ListDvds.returnList(listFrom);
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

    final DvdListFrom dvdListFrom = new DvdListFrom();
    dvdListFrom.userName = fromUserName;

    return ListDvds.returnList(dvdListFrom);
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

    final DvdListFrom dvdListFrom = new DvdListFrom();
    dvdListFrom.genre = genreName;

    return ListDvds.returnList(dvdListFrom);
  }

  public static Result searchDvd() {
    final String[] strings = Controller.request().queryString().get("searchFor");
    if (strings == null || strings.length != 1) {
      return ListDvds.listAlldvds();
    } else {
      final DvdListFrom listFrom = new DvdListFrom();
      listFrom.searchFor = strings[0];
      return ListDvds.returnList(listFrom);
    }

  }

  /**
   * Returns the dvds for the template
   * 
   * @param dvdListFrom
   * @param ctx
   * @return
   */
  private static Result returnList(final DvdListFrom dvdListFrom) {

    DvdListFrom.setCurrentSearchForm(dvdListFrom);

    final String username = Controller.request().username();
    final Page<Dvd> dvdsByForm = Dvd.getDvdsByForm(dvdListFrom);
    return Results.ok(listdvds.render(new DvdPage(dvdsByForm), username));
  }

}
