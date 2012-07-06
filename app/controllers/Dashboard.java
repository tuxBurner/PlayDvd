package controllers;

import helpers.EImageSize;
import helpers.EImageType;
import helpers.ImageHelper;

import java.io.File;
import java.util.List;

import jgravatar.Gravatar;
import jgravatar.GravatarDefaultImage;
import jgravatar.GravatarRating;
import models.Dvd;
import models.EMovieAttributeType;
import models.MovieAttribute;
import models.User;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;
import views.html.genremenu;
import views.html.dashboard.displaydvd;
import views.html.dashboard.lendform;
import forms.InfoDvd;
import forms.LendForm;

@Security.Authenticated(Secured.class)
public class Dashboard extends Controller {

  /**
   * Display the dvd and its informations
   * 
   * @param dvdId
   * @return
   */
  public static Result displayDvd(final Long dvdId) {
    final Dvd dvd = Dvd.find.byId(dvdId);

    if (dvd == null) {
      return Results.badRequest("Dvd with the given Id was not found");
    }

    final InfoDvd infoDvd = new InfoDvd(dvd);

    return Results.ok(displaydvd.render(infoDvd));
  }

  /**
   * Displays the dialog content for lending a dvd to a another {@link User}
   * 
   * @return
   */
  public static Result lendDialogContent(final Long dvdId) {
    // check if the user may see the dvd
    final String userName = Controller.ctx().session().get(Secured.AUTH_SESSION);
    final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    final Form<LendForm> form = Controller.form(LendForm.class);
    return Results.ok(lendform.render(form, dvdForUser));
  }

  /**
   * This actually lends a dvd to a user this is called via ajax
   * 
   * @param dvdId
   * @return
   */
  public static Result lendDvd(final Long dvdId) {

    final Form<LendForm> form = Controller.form(LendForm.class).bindFromRequest();

    // check if the form is okay
    final LendForm lendForm = form.get();
    final String userName = StringUtils.trimToNull(lendForm.userName);
    final String freeName = StringUtils.trimToNull(lendForm.freeName);
    
    if ((StringUtils.isEmpty(userName) == true && StringUtils.isEmpty(freeName) == true)) {
    	Logger.error("Could not lend dvd because no user or freename is given: "+"username: " + userName + " freename: " + freeName);
      
      return Results.internalServerError();
    }

    final String ownerName = Controller.ctx().session().get(Secured.AUTH_SESSION);
    Dvd.lendDvdToUser(dvdId, ownerName, userName, freeName,lendForm.alsoOthersInHull);

    return Results.TODO;
  }

  /**
   * This is called from the mainmenu.scala.html to have all the genres in the
   * menu
   * 
   * @return
   */
  public static Result menuGenres() {
    final List<MovieAttribute> genres = MovieAttribute.getAllByType(EMovieAttributeType.GENRE);
    return Results.ok(genremenu.render(genres));
  }

  public static Result streamImage(final Long dvdId, final String imgType, final String imgSize) {
    final File file = ImageHelper.getImageFile(dvdId, EImageType.valueOf(imgType), EImageSize.valueOf(imgSize));
    if (file != null) {
      return Results.ok(file);
    }
    return Results.ok();
  }

  /**
   * Gets the gravatar for the user
   * 
   * @return
   */
  public static Result gravatar(final Integer size, final String userName) {

    final String ownerName = (userName == null) ? Controller.ctx().session().get(Secured.AUTH_SESSION) : userName;
    final User userByName = User.getUserByName(ownerName);

    if (userByName == null) {
      return Results.ok();
    }

    final Gravatar gravatar = new Gravatar();
    gravatar.setSize(size);
    gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
    gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
    final byte[] jpg = gravatar.download(userByName.email);
    return Results.ok(jpg);

  }

  /**
   * Displays the user profile mask
   * 
   * @return
   */
  public static Result profile() {
    return Results.TODO;
  }

}
