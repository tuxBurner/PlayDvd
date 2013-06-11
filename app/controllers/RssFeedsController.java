package controllers;

import models.Dvd;
import org.w3c.dom.Document;
import play.mvc.BodyParser;
import play.mvc.Controller;
import play.mvc.Result;
import play.mvc.With;

import java.util.List;

/**
 * User: tuxburner
 * Date: 6/9/13
 * Time: 10:55 PM
 */
public class RssFeedsController extends Controller {

  @With(RssSecurityAction.class)

  public static Result gettLastAddedCopies() {


    final List<Dvd> asList = Dvd.find.findPagingList(10).getAsList();



    return ok();
  }

}
