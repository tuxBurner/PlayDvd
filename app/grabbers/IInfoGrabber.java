package grabbers;

import java.util.List;

import models.Movie;

/**
 * This is the Interface for grabbing {@link Movie} informations from certain
 * services
 * 
 * @author tuxburner
 * 
 */
public interface IInfoGrabber {

  public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException;

}
