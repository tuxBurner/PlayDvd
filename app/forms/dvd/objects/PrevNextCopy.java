package forms.dvd.objects;

import models.Dvd;

/**
 * User: tuxburner
 * Date: 5/12/13
 * Time: 2:52 PM
 */
public class PrevNextCopy {

  public final Long id;

  public final Long movieId;

  public  final String title;

  public final Boolean hasPoster;

  public  PrevNextCopy(final Dvd copy) {
    this.id = copy.id;
    this.title = copy.movie.title;
    this.movieId = copy.movie.id;
    this.hasPoster = copy.movie.hasPoster;
  }

}
