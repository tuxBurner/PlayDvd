package grabbers;

import java.util.List;

import forms.grabbers.GrabberInfoForm;
import models.Movie;
import forms.MovieForm;

/**
 * This is the Interface for grabbing {@link Movie} informations from certain
 * services
 * 
 * @author tuxburner
 * 
 */
public interface IInfoGrabber {

  /**
   * This is called when the user searches for a movie with the grabber
   * 
   * @param searchTerm
   * @return
   * @throws grabbers.GrabberException
   */
  public List<GrabberSearchMovie> searchForMovie(final String searchTerm) throws GrabberException;

  /**
   * This displays the movie the user picked from the search result and displays
   * all avaible backdrops and urls
   * 
   * @param id
   * @return
   * @throws GrabberException
   */
  public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException;

  /**
   * This fills the information from the movie to the {@link MovieForm} which is
   * used to edit or add the informations
   * 
   * @param grabberInfoForm
   * @return
   * @throws GrabberException
   */
  public MovieForm fillInfoToMovieForm(final GrabberInfoForm grabberInfoForm) throws GrabberException;

}
