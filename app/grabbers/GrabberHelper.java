package grabbers;

import com.omertron.themoviedbapi.MovieDbException;
import play.Logger;

/**
 * A helper class for the grabbers
 * Created by tuxburner on 21.01.17.
 */
public class GrabberHelper {

    /**
     * Gets the grabber for the given type
     *
     * @param grabberType the type of the grabber to get the data from
     * @return the grabber
     */
    public static IInfoGrabber getGrabber(final EGrabberType grabberType) {

        if (EGrabberType.TMDB.equals(grabberType)) {
            try {
                return new TmdbGrabber();
            } catch (MovieDbException e) {
                if (Logger.isErrorEnabled() == true) {
                    Logger.error("An error happened while loading the: " + TmdbGrabber.class.getName(), e);
                }
            }
        }

        if (EGrabberType.THETVDB.equals(grabberType)) {
            return new TheTvDbGrabber();
        }

        if (EGrabberType.MOVIECOMBINED.equals(grabberType)) {
            try {
                return new MovieCombined();
            } catch (MovieDbException e) {
                if (Logger.isErrorEnabled() == true) {
                    Logger.error("An error happened while loading the: " + TmdbGrabber.class.getName(), e);
                }
            }
        }

        throw new RuntimeException("No Grabber was found for: "+grabberType.name());
    }
}
