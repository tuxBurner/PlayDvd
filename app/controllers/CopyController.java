package controllers;

import com.github.tuxBurner.jsAnnotations.JSRoute;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import forms.MovieForm;
import forms.dvd.CopyForm;
import forms.grabbers.GrabberInfoForm;
import grabbers.EGrabberType;
import grabbers.GrabberHelper;
import grabbers.IInfoGrabber;
import grabbers.amazonwebcrawler.AmazonMovieWebCrawler;
import grabbers.amazonwebcrawler.AmazonResult;
import helpers.RequestToCollectionHelper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import models.Dvd;
import models.DvdAttribute;
import models.EDvdAttributeType;
import models.Movie;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.Security;

/**
 * This {@link Controller} handles all the edit and add {@link Dvd} magic
 *
 * @author tuxburner
 */
@Security.Authenticated(Secured.class)
@Singleton
public class CopyController extends Controller {

    public static final String DVD_FORM_ADD_MODE = "add";

    public static final String DVD_FORM_EDIT_MODE = "edit";

    public static final Long NO_COPY_SELECTED_ID = new Long(-1);

    final FormFactory formFactory;

    @Inject
    public CopyController(FormFactory formFactory) {
        this.formFactory = formFactory;
    }


    /**
     * Shows the add Dvd form
     *
     * @return
     */
    public Result showAddDvd() {

        final Form<CopyForm> form = formFactory.form(CopyForm.class);
        return Results.ok(views.html.dvd.dvdform.render(form.fill(new CopyForm()), CopyController.DVD_FORM_ADD_MODE));
    }

    /**
     * Shows the edit dvd form
     *
     * @return
     */
    public Result showEditDvd(final Long dvdId) {

        final Dvd dvdToEdit = Dvd.getDvdForUser(dvdId, Controller.request().username());

        if (dvdToEdit == null) {
            return Results.badRequest("U ARE NOT ALLOWED TO EDIT :) ");
        }

        final Form<CopyForm> form = formFactory.form(CopyForm.class);

        return Results.ok(views.html.dvd.dvdform.render(form.fill(CopyForm.dvdToDvdForm(dvdToEdit)), CopyController.DVD_FORM_EDIT_MODE));
    }

    /**
     * This is called when the user submits the add Dvd Form
     *
     * @return
     */
    public Result addDvd(final String mode) {

        final Map<String, String> map = RequestToCollectionHelper.requestToFormMap(Controller.request(), "audioTypes");
        final Form<CopyForm> dvdForm = formFactory.form(CopyForm.class).bind(map);
        if (dvdForm.hasErrors()) {
            return Results.badRequest(views.html.dvd.dvdform.render(dvdForm, mode));
        } else {

            try {

                final String userName = Secured.getUsername();

                if (CopyController.DVD_FORM_ADD_MODE.equals(mode) == true) {
                    final Dvd createFromForm = Dvd.createFromForm(userName, dvdForm.get());
                    Controller.flash("success", "Dvd: " + createFromForm.movie.title + " added");
                }

                if (CopyController.DVD_FORM_EDIT_MODE.equals(mode) == true) {
                    final Dvd editFromForm = Dvd.editFromForm(userName, dvdForm.get());
                    Controller.flash("success", "Dvd: " + editFromForm.movie.title + " edited");
                }

            } catch (final Exception e) {
                e.printStackTrace();
                return Results.badRequest(views.html.dvd.dvdform.render(dvdForm, mode));
            }

            return Results.redirect(routes.ListCopiesController.listCopies(null));
        }
    }

    /**
     * Searches a movie via amazon with the given code
     *
     * @param code   the code to lookup
     * @param copyId if set we search for an existing copy
     * @return
     */
    @JSRoute
    public Result searchAmazonByCode(final String code, final Long copyId) {
        Optional<AmazonResult> result = null;
        List<Movie> movies = null;
        if (StringUtils.isEmpty(code) == false) {
            result = AmazonMovieWebCrawler.lookUpByCode(code);
            if (result .isPresent() && StringUtils.isEmpty(result.get().title) == false) {
                movies = Movie.searchLikeAndAmazoneCode(result.get().title, result.get().ean);
            }
        }

        return ok(views.html.dvd.dvdAmazonPopUp.render(result.orElseGet(null), code, copyId, movies));
    }

    /**
     * Starts a search for the given title on amazon.
     *
     * @param title the title to search for.
     * @return
     */
    @JSRoute
    public Result searchAmazonByTitle(final String title) {

        List<AmazonResult> amazonResults = null;

        if (StringUtils.isBlank(title) == false) {
            //amazonResults = AmazonMovieLookuper.findByName(title);
            amazonResults = AmazonMovieWebCrawler.findByName(title);
        }

        return ok(views.html.dvd.searchAmazonByTitlePopUp.render(amazonResults, title));
    }

    /**
     * Adds a {@link Movie}
     *
     * @param grabberType
     * @return
     */
    @JSRoute
    public Result addMovieByGrabber(final String grabberType) {
        try {
            final Form<GrabberInfoForm> grabberInfoForm = formFactory.form(GrabberInfoForm.class).bindFromRequest();

            final IInfoGrabber grabber = GrabberHelper.getGrabber(EGrabberType.valueOf(grabberType));

            final MovieForm movieForm = grabber.fillInfoToMovieForm(grabberInfoForm.get());
            final Movie movie = Movie.editOrAddFromForm(movieForm,true);

            if (movie == null) {
                return Results.badRequest("An error happend while creating the new movie");
            }

            return ok(String.valueOf(movie.id));

        } catch (final Exception e) {
            if (Logger.isErrorEnabled()) {
                Logger.error("Internal Error happened", e);
            }
            return Results.badRequest("Internal Error happened");
        }
    }

    /**
     * Shows the add edit Dvd form with the results from the {@link AmazonMovieLookuper}
     *
     * @param code
     * @param movieId
     * @param copyId
     * @return
     */
    @JSRoute
    public Result showDvdByAmazonAndMovie(final String code, final Long movieId, final Long copyId) {

        if (StringUtils.isEmpty(code) == true || movieId == null) {
            return badRequest();
        }

        Optional<AmazonResult> amazonResult = AmazonMovieWebCrawler.lookUpByCode(code);
        if (amazonResult == null) {
            if (Logger.isDebugEnabled() == true) {
                Logger.error("Error adding dvd with amazonecode: " + code);
            }
            return badRequest();
        }

        Movie movie = Movie.FINDER.byId(movieId);
        if (movie == null) {
            if (Logger.isDebugEnabled() == true) {
                Logger.error("Error adding dvd with movie: " + movieId);
            }
            return badRequest();
        }

        String mode = DVD_FORM_ADD_MODE;
        Dvd copy = null;
        if (copyId.equals(NO_COPY_SELECTED_ID) == false) {
            mode = DVD_FORM_EDIT_MODE;
            copy = Dvd.getDvdForUser(copyId, Secured.getUsername());
        }

        final Form<CopyForm> form = formFactory.form(CopyForm.class);
        final CopyForm copyForm = CopyForm.amazonAndMovieToDvdForm(amazonResult.orElseGet(null), movieId, copy);

        return Results.ok(views.html.dvd.dvdform.render(form.fill(copyForm), mode));
    }

    /**
     * Just fills the informations from the amazon lookup to the copy form an returns it
     */
    public Result showCopyFormWithAmazonInfo(final String code, final Long copyId) {
        if (StringUtils.isEmpty(code) == true) {
            if (Logger.isErrorEnabled() == true) {
                Logger.error("No code is given for looking up amazon infos.");
            }
            return badRequest();
        }

        Dvd copy = null;
        if (copyId.equals(NO_COPY_SELECTED_ID) == false) {
            copy = Dvd.getDvdForUser(copyId, Controller.request().username());
            if (copy == null) {
                if (Logger.isDebugEnabled() == true) {
                    Logger.debug("Could not find copy with id: " + copyId + " for user: " + Controller.request().username());
                }
                return badRequest();
            }
        }

        final Optional<AmazonResult> amazonResult = AmazonMovieWebCrawler.lookUpByCode(code);
        String mode = DVD_FORM_ADD_MODE;
        final Form<CopyForm> form = formFactory.form(CopyForm.class);
        final CopyForm copyForm = CopyForm.amazonAndCopyToForm(copy, amazonResult.orElse(null));
        form.fill(copyForm);


        if (copy != null) {
            mode = DVD_FORM_EDIT_MODE;
        }

        return Results.ok(views.html.dvd.dvdform.render(form.fill(copyForm), mode));
    }

    /**
     * Searches for {@link DvdAttribute} returns a json with
     * {@link DvdAttribute} and {@link DvdAttribute#value}
     *
     * @param term
     * @param attrType
     * @return
     */
    @JSRoute
    public Result searchForCopyAttribute(final String term, final String attrType) {
        try {
            final EDvdAttributeType eattrType = EDvdAttributeType.valueOf(attrType);
            final String result = DvdAttribute.searchAvaibleAttributesAsJson(eattrType, term);
            return Results.ok(result);
        } catch (final Exception e) {
            Logger.error("An error happend while getting: " + attrType + " " + EDvdAttributeType.class.getName() + " with search term: " + term, e);
        }

        return Results.badRequest();
    }

}
