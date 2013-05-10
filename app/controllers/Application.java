package controllers;

import jsannotation.JSRoutesPlugin;
import jsmessages.JsMessages;
import org.apache.commons.lang.ArrayUtils;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;

@Security.Authenticated(Secured.class)
public class Application extends Controller {

  /**
   * Array of keys which are in general use for js
   */
  private final static  String[] GENERAL_I18N_JS_KEYS = { "btn.close" };


  public static Result index() {
    return redirect(routes.ListDvdsController.listAlldvds());
  }

  /**
   * Register the routes to certain stuff to the javascript routing so we can
   * reach it better from there
   *
   * @return
   */
  public static Result jsRoutes() {
   return JSRoutesPlugin.getJsRoutesResult();
  }

  /**
   * Gets the i18n text for the given keys and adds general keys
   * @param namespace
   * @param keys
   * @return
   */
  public static String getJsI8N(final String namespace, final String... keys){

    final String[] allKeys = (String[]) ArrayUtils.addAll(GENERAL_I18N_JS_KEYS, keys);

    return jsmessages.JsMessages.subset(namespace,allKeys);
  }
}