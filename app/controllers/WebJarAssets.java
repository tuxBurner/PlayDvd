package controllers;

import play.api.mvc.Action;
import play.api.mvc.AnyContent;
import play.mvc.Controller;

public class WebJarAssets extends Controller {

  public static Action<AnyContent> at(final String file) {
    return Assets.at("/META-INF/resources/webjars", file);
  }

}