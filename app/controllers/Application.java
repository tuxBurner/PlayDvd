package controllers;


import jsmessages.JsMessagesFactory;
import org.apache.commons.lang.ArrayUtils;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.Security;
import jsAnnotations.JsRoutesComponentImpl;

import javax.inject.Inject;

@Security.Authenticated(Secured.class)
public class Application extends Controller
{

  @Inject
  static JsMessagesFactory jsMessagesFactory;

  /**
   * Array of keys which are in general use for js
   */
  private final static String[] GENERAL_I18N_JS_KEYS = {"btn.close"};

  //final static JsMessages JS_MESSAGES = jsmessages.JsMessages.create(play.Play.application());


  public static Result index()
  {
    return redirect(routes.ListDvdsController.listAlldvds());
  }

  /**
   * Register the routes to certain stuff to the javascript routing so we can
   * reach it better from there
   *
   * @return
   */
  public static Result jsRoutes()
  {
    return JsRoutesComponentImpl.getJsRoutesResult();
  }

  /**
   * Gets the i18n text for the given keys and adds general keys
   *
   * @param namespace
   * @param keys
   * @return
   */
  public static String getJsI8N(final String namespace, final String... keys)
  {

    final String[] allKeys = (String[]) ArrayUtils.addAll(GENERAL_I18N_JS_KEYS, keys);


    //jsMessagesFactory.subset(keys);

    //return JS_MESSAGES.subset(play.Play.application(), allKeys).generate(namespace).toString();
    return "";


  }
}