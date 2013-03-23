package grabbers;

import com.omertron.traileraddictapi.TrailerAddictApi;
import com.omertron.traileraddictapi.TrailerAddictException;
import com.omertron.traileraddictapi.model.Trailer;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * User: tuxburner
 * Date: 3/16/13
 * Time: 1:02 PM
 */
public class MovieCombined extends TmdbGrabber {

  @Override
  public GrabberDisplayMovie getDisplayMovie(final String id) throws GrabberException {
    final GrabberDisplayMovie displayMovie = super.getDisplayMovie(id);
    displayMovie.grabber = EGrabberType.MOVIECOMBINED;



    // fetch the trailers from traileraddict
    if(StringUtils.isEmpty(displayMovie.imdbId) == false) {
      try {
        final List<Trailer> trailerList = TrailerAddictApi.getFilmImdb(displayMovie.imdbId,10);

        if(CollectionUtils.isEmpty(trailerList) == false) {
          for(final Trailer trailer :  trailerList) {
            trailer.getLink();
          }
        }

      } catch (TrailerAddictException e) {
        throw new GrabberException(e);
      }
    }
    return displayMovie;
  }
}
