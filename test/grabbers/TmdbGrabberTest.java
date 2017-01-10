package grabbers;


import com.omertron.themoviedbapi.MovieDbException;
import org.junit.Assert;
import org.junit.Test;


public class TmdbGrabberTest {

  @Test
  public void testSearchForMovie() {
    try {
      final TmdbGrabber tmdbGrabber = new TmdbGrabber();
      tmdbGrabber.searchForMovie("Batman");
    } catch (final GrabberException e) {
      Assert.fail(e.getMessage());
    } catch (MovieDbException e) {
      Assert.fail(e.getMessage());
    }

  }

  @Test
  public void testGetDisplayInfo() {
    try {
      final TmdbGrabber tmdbGrabber = new TmdbGrabber();
      final GrabberDisplayMovie displayMovie = tmdbGrabber.getDisplayMovie("268");
    } catch (final GrabberException e) {
      Assert.fail(e.getMessage());
    } catch (MovieDbException e) {
      Assert.fail(e.getMessage());
    }
  }

}
