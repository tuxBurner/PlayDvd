package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Singleton;
import dao.DvdDao;
import dao.UserDao;
import forms.ExternalImageForm;
import forms.LendForm;
import forms.UnLendForm;
import forms.dvd.CopySearchFrom;
import forms.dvd.objects.CopyInfo;
import forms.dvd.objects.PrevNextCopies;
import helpers.*;
import models.CopyReservation;
import models.Dvd;
import models.User;
import models.ViewedCopy;
import net.coobird.thumbnailator.Thumbnails;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.db.ebean.Transactional;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.*;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Security.Authenticated(Secured.class)
@Singleton
public class DashboardController extends Controller {


  private final FormFactory formFactory;

  private final CacheHelper cacheHelper;

  private final MessagesApi messagesApi;

  @Inject
  DashboardController(final FormFactory formFactory, final CacheHelper cacheHelper, final MessagesApi messagesApi) {
    this.formFactory = formFactory;
    this.cacheHelper = cacheHelper;
    this.messagesApi = messagesApi;
  }

  /**
   * Display the dvd and its informations in a popup
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result displayDvd(final Long dvdId, final Http.Request request) {
    return getInfoDvd(dvdId, true, request);
  }


  /**
   * Display the dvd and its informations on a page
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result displayCopyOnPage(final Long dvdId, final Http.Request request) {
    return getInfoDvd(dvdId, false, request);
  }

  /**
   * Gets the {@link CopyInfo} for the given id
   *
   * @param copyId
   * @return
   */
  private Result getInfoDvd(final Long copyId, final boolean popup, final Http.Request request) {

    final Dvd copy = Dvd.FINDER.byId(copyId);

    if (copy == null) {
      if (Logger.isErrorEnabled() == true) {
        Logger.error("Could not find copy with id: " + copyId);
      }
      return Results.badRequest("Copy with the given id was not found");
    }


    final CopyInfo copyInfo = new CopyInfo(copy);

    final CopySearchFrom currentSearchForm = CopySearchFrom.getCurrentSearchForm(cacheHelper, request);
    final PrevNextCopies nextAndPrev = DvdDao.getNextAndPrev(copy, currentSearchForm);

    final CacheShoppingCart shoppingCartFromCache = cacheHelper.getShoppingCartFromCache(request);
    final Set<Long> bookmarkedCopyIds = cacheHelper.getBookmarkedCopyIds(request);
    final List<ViewedCopy> copyViewed = ViewedCopy.getCopyViewed(copy, request);

    final Messages messages = this.messagesApi.preferred(request);
    final var usernameStatic = Secured.getUsernameStatic(request);
    if (popup == true) {
      return Results.ok(views.html.dashboard.displaydvdPopup.render(copyInfo, usernameStatic, request, messages));
    } else {
      return Results.ok(views.html.dashboard.displaydvd.render(copyInfo, usernameStatic, nextAndPrev, shoppingCartFromCache, bookmarkedCopyIds, copyViewed, request, messages));
    }
  }

  /**
   * Displays the dialog content for lending a dvd to a another {@link User}
   *
   * @return
   */
  @JSRoute
  public Result lendDialogContent(final Long dvdId, final Http.Request request) {
    // check if the user may see the dvd
    final String userName = Secured.getUsernameStatic(request);
    final Dvd dvdForUser = DvdDao.getDvdForUser(dvdId, userName);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    final List<Dvd> dvdForUserInSameHull = DvdDao.getDvdUnBorrowedSameHull(dvdForUser);
    final Map<String, String> reservationsForCopy = CopyReservation.getReservationsForCopy(dvdId);

    final Form<LendForm> form = formFactory.form(LendForm.class);
    final Messages messages = this.messagesApi.preferred(request);
    return Results.ok(views.html.dashboard.lendform.render(form, dvdForUser, dvdForUserInSameHull, reservationsForCopy, UserDao.loadOtherUserNames(request), request, messages));
  }

  /**
   * Displays the dialog content for unlending a dvd
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result unLendDialogContent(final Long dvdId, final Http.Request request) {
    // check if the user may see the dvd
    final String userName = Secured.getUsernameStatic(request);
    final Dvd dvdForUser = DvdDao.getDvdForUser(dvdId, userName, true);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    if (dvdForUser.borrowDate == null) {
      final String message = "The Dvd: " + dvdForUser + " is not borrowed to anybody !";
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      Logger.error(message);
      return Results.internalServerError(message);
    }

    final List<Dvd> dvdBorrowedSameHull = DvdDao.getDvdBorrowedSameHull(dvdForUser);
    final Messages messages = this.messagesApi.preferred(request);

    return Results.ok(views.html.dashboard.unlendform.render(formFactory.form(UnLendForm.class), dvdForUser, dvdBorrowedSameHull, request, messages));

  }

  /**
   * This actually lends a dvd to a user this is called via ajax
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result lendDvd(final Long dvdId, final Http.Request request) {

    final Form<LendForm> form = formFactory.form(LendForm.class).bindFromRequest(request);

    // check if the form is okay
    final LendForm lendForm = form.get();
    String userName = StringUtils.trimToNull(lendForm.userName);
    final String freeName = StringUtils.trimToNull(lendForm.freeName);
    final String reservation = StringUtils.trimToNull(lendForm.reservation);

    if (StringUtils.isEmpty(userName) == true && StringUtils.isEmpty(freeName) == true && StringUtils.isEmpty(reservation) == true) {
      Logger.error("Could not lend dvd because no user, reservation or freename is given ");

      return Results.internalServerError();
    }

    if (StringUtils.isEmpty(reservation) == false && StringUtils.isNumeric(reservation) == true) {
      final String reservationBorrowerName = CopyReservation.getReservationBorrowerName(Long.valueOf(reservation));
      if (StringUtils.isEmpty(reservationBorrowerName) == false) {
        userName = reservationBorrowerName;
      }
    }

    final String ownerName = request.session().get(Secured.AUTH_SESSION).get();
    DvdDao.lendDvdToUser(dvdId, ownerName, userName, freeName, lendForm.alsoOthersInHull);

    return Results.ok();
  }

  /**
   * This actually lends a dvd to a user this is called via ajax
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result unlendDvd(final Long dvdId, final Http.Request request) {

    final Form<UnLendForm> form = formFactory.form(UnLendForm.class).bindFromRequest(request);

    // check if the form is okay
    final UnLendForm unlendForm = form.get();

    final String ownerName = request.session().get(Secured.AUTH_SESSION).get();
    DvdDao.unlendDvdToUser(dvdId, ownerName, unlendForm.alsoOthersInHull);

    return Results.ok();
  }

  /**
   * This renders the content for the delete dvd content dialog
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result deleteDialogContent(final Long dvdId, final Http.Request request) {

    final String userName = Secured.getUsernameStatic(request);
    final Dvd dvdForUser = DvdDao.getDvdForUser(dvdId, userName);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    final Messages messages = this.messagesApi.preferred(request);
    return Results.ok(views.html.dashboard.deletedvd.render(dvdForUser, request, messages));
  }

  /**
   * Actually deletes the dvd
   *
   * @param dvdId
   * @return
   */
  @JSRoute
  public Result deleteDvd(final Long dvdId, final Http.Request request) {
    final String userName = Secured.getUsernameStatic(request);
    final Dvd dvdForUser = DvdDao.getDvdForUser(dvdId, userName);
    if (dvdForUser == null) {
      return Results.forbidden();
    }

    //Ebean.deleteManyToManyAssociations(dvdForUser, "attributes");
    dvdForUser.attributes.clear();
    // TODO: LIFT
    //Ebean.save(dvdForUser);
    dvdForUser.delete();

    return Results.ok();
  }

  /**
   * Streams an image which is bundled with the given {@link Dvd}
   *
   * @param copyId
   * @param imgType
   * @param imgSize
   * @return
   */
  @JSRoute
  public Result streamImage(final Long copyId, final String imgType, final String imgSize, final Http.Request request) {
    return getStreamImage(copyId, imgType, imgSize, request);
  }

  /**
   * Static helper for getting the correct image.
   *
   * @param copyId
   * @param imgType
   * @param imgSize
   * @return
   */
  public static Result getStreamImage(final Long copyId, final String imgType, final String imgSize, final Http.Request request) {
    final String url = ImageHelper.getImageFile(copyId, EImageType.valueOf(imgType), EImageSize.valueOf(imgSize));
    if (StringUtils.isEmpty(url) == true) {
      return Results.notFound();
    }

    final File file = new File(url);


    final String etag = ETagHelper.getEtag(file);
    final String nonMatch = (request.header(IF_NONE_MATCH).isPresent()) ? request.header(IF_NONE_MATCH).get() : "";
    if (etag.equals(nonMatch) == true) {
      return status(304);
    }

    return Results.ok(file).as("image/png").withHeader(ETAG, etag).withHeader("Content-Length", String.valueOf(file.length()));


  }

  /**
   * This opens an external image and resizes it when needed
   *
   * @return
   */
  public Result streamExternalImage(final Http.Request request) {
    final Form<ExternalImageForm> form = formFactory.form(ExternalImageForm.class).bindFromRequest(request);

    if (form.hasErrors()) {
      return Results.badRequest("Failure");
    }

    final EImageSize imageSize = EImageSize.valueOf(form.get().imgSize);

    try {
      final String urlFixed = StringUtils.replace(form.get().url, " ", "%20");
      final BufferedImage asBufferedImage = Thumbnails.of(new URL(urlFixed)).size(imageSize.getWidth(), imageSize.getHeight()).asBufferedImage();

      final ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(asBufferedImage, "png", os);
      final InputStream is = new ByteArrayInputStream(os.toByteArray());


      return Results.ok(is).as("image/png");
    } catch (final IOException e) {
      Logger.error("Failure while creating external image:", e);
      return Results.badRequest("Failure");
    }

  }

  /**
   * Gets the gravatar for the user
   *
   * @return
   */
  public Result gravatar(final Integer size, final String userName, final Http.Request request) {

    final String ownerName = (userName == null) ? Secured.getUsernameStatic(request) : userName;
    final User userByName = UserDao.findUserByName(ownerName);

    final String gravatarEmail = (userByName == null) ? "" : userByName.email;

    final String etag = ETagHelper.getEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size);
    final String nonMatch = request.header(IF_NONE_MATCH).get();
    if (etag != null && etag.equals(nonMatch) == true) {
      return status(304);
    }

    byte[] gravatarBytes = cacheHelper.getObject(ECacheObjectName.GRAVATAR_IMAGES, gravatarEmail + size);
    if (gravatarBytes == null) {
      gravatarBytes = GravatarHelper.getGravatarBytes(gravatarEmail, size);
      cacheHelper.setObject(ECacheObjectName.GRAVATAR_IMAGES, gravatarEmail + size, gravatarBytes);
      ETagHelper.removeEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size);
      ETagHelper.createEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size, gravatarBytes);
    }

    return Results.ok(gravatarBytes).as("image/png").withHeader(ETAG, ETagHelper.getEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size));

  }


}
