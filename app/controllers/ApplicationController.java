package controllers;


import com.github.tuxBurner.jsAnnotations.JsRoutesComponent;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import jsmessages.japi.Helper;
import org.apache.commons.lang.ArrayUtils;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import play.twirl.api.JavaScript;

import javax.inject.Inject;
import javax.inject.Singleton;

@Security.Authenticated(Secured.class)
@Singleton
public class ApplicationController extends Controller {


    JsRoutesComponent jsRoutesComponent;

    /**
     * Array of keys which are in general use for js
     */
    private final static String[] GENERAL_I18N_JS_KEYS = {"btn.close"};

    //final static JsMessages JS_MESSAGES = jsmessages.JsMessages.create(play.Play.application());


    private static JsMessagesFactory jsMessagesFactory;

    /**
     * Constructor which gets the js message factory injected.
     *
     * @param jsMessagesFactory the js message factory
     * @param jsRoutesComponent the component for the javascript routes
     */
    @Inject
    public ApplicationController(JsMessagesFactory jsMessagesFactory, JsRoutesComponent jsRoutesComponent) {
        this.jsMessagesFactory = jsMessagesFactory;
        this.jsRoutesComponent = jsRoutesComponent;
    }


    public Result index() {
        return redirect(routes.ListCopiesController.listAllCopies());
    }

    /**
     * Register the routes to certain stuff to the javascript routing so we can
     * reach it better from there
     *
     * @return
     */
    public Result jsRoutes() {
        return jsRoutesComponent.getJsRoutesResult();
    }

    /**
     * Gets the i18n text for the given keys and adds general keys
     *
     * @param namespace
     * @param keys
     * @return
     */
    public static String getJsI8N(final String namespace, final String... keys) {
        final String[] allKeys = (String[]) ArrayUtils.addAll(GENERAL_I18N_JS_KEYS, keys);
        final JsMessages jsMessages = jsMessagesFactory.subset(Scala.varargs(allKeys));
        final JavaScript jsScript = jsMessages.apply(Scala.Option("window.Messages"), Helper.messagesFromCurrentHttpContext());
        return jsScript.text();
    }
}