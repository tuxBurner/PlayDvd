package grabbers;

import java.util.List;

import models.Movie;
import forms.MovieForm;
import forms.GrabberInfoForm;

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
   * @throws GrabberException
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
   * @param posterId
   * @param backdropId
   * @return
   * @throws GrabberException
   */
  public MovieForm filleInfoToMovieForm(final GrabberInfoForm grabberInfoForm, String posterId, String backdropId) throws GrabberException;

}
