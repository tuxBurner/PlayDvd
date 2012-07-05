package forms;

import models.Dvd;

/**
 * Holds a dvd for displaying it in the info panel for viewing dvds in the same
 * box
 * 
 * @author tuxburner
 * 
 */
public class CollectionDvd {

  public Boolean hasPoster;
  public String title;
  public Long id;
  public Long movieId;

  public CollectionDvd(final Dvd dvd) {
    hasPoster = dvd.movie.hasPoster;
    title = dvd.movie.title;
    movieId = dvd.movie.id;
    id = dvd.id;
  }

}
