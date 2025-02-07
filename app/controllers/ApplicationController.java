package controllers;


import com.github.tuxBurner.jsAnnotations.JsRoutesComponent;
import jsmessages.JsMessages;
import jsmessages.JsMessagesFactory;
import org.apache.commons.lang3.ArrayUtils;
import play.i18n.MessagesApi;
import play.libs.Scala;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Security;

import javax.inject.Inject;
import javax.inject.Singleton;

@Security.Authenticated(Secured.class)
@Singleton
public class ApplicationController extends Controller {


  private static MessagesApi messagesApi;

  JsRoutesComponent jsRoutesComponent;

  /**
   * Array of keys which are in general use for js
   */
  private final static String[] GENERAL_I18N_JS_KEYS = {"btn.close"};


  private static JsMessagesFactory jsMessagesFactory;

  /**
   * Constructor which gets the js message factory injected.
   *
   * @param jsMessagesFactory the js message factory
   * @param jsRoutesComponent the component for the javascript routes
   */
  @Inject
  public ApplicationController(final JsMessagesFactory jsMessagesFactory, final JsRoutesComponent jsRoutesComponent, final MessagesApi messagesApi) {
    this.jsMessagesFactory = jsMessagesFactory;
    this.jsRoutesComponent = jsRoutesComponent;
    this.messagesApi = messagesApi;
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
  public Result jsRoutes(Http.Request request) {
    return jsRoutesComponent.getJsRoutesResult(request);
  }

  /**
   * Gets the i18n text for the given keys and adds general keys
   * *
   *
   * @param keys
   * @return
   */
  public static String getJsI8N(final Http.Request request, final String... keys) {
    final String[] allKeys = ArrayUtils.addAll(GENERAL_I18N_JS_KEYS, keys);
    final JsMessages jsMessages = jsMessagesFactory.subset(Scala.varargs(allKeys));
    final var jsScript = jsMessages.apply(Scala.Option("window.Messages"), messagesApi.preferred(request).messages());
    return jsScript.text();
  }
}