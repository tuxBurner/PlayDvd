package controllers;

import models.Dvd;
import play.mvc.Controller;
import play.mvc.Security;

/**
 * This {@link Controller} handles all the edit and add {@link Dvd} magic
 * 
 * @author tuxburner
 * 
 */
@Security.Authenticated(Secured.class)
public class DvdController extends Controller {

}
