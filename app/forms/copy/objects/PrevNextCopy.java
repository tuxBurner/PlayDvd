package forms.copy.objects;

import models.Dvd;

/**
 * User: tuxburner
 * Date: 5/12/13
 * Time: 2:52 PM
 */
public class PrevNextCopy {

  public final Long id;

  public final Long movieId;

  public final String title;

  public final Boolean hasPoster;

  public PrevNextCopy(final Dvd copy) {
    this.id = copy.getId();
    this.title = copy.getMovie().getTitle();
    this.movieId = copy.getMovie().getId();
    this.hasPoster = copy.getMovie().getHasPoster();
  }

}
