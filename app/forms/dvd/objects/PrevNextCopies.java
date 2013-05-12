package forms.dvd.objects;

import models.Dvd;

/**
 * Simply holds the next and the prev {@link models.Dvd}'s for the currently viewed {@link models.Dvd}
 *
 * User: tuxburner
 * Date: 5/12/13
 * Time: 2:51 PM
 */
public class PrevNextCopies {

  final public PrevNextCopy prev;

  final public PrevNextCopy next;

  public PrevNextCopies(final Dvd prevCopy, final Dvd nextCopy) {
    prev = (prevCopy == null) ? null : new PrevNextCopy(prevCopy);
    next = (nextCopy == null) ? null : new PrevNextCopy(nextCopy);
  }

}
