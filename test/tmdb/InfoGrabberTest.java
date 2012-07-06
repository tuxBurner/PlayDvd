package tmdb;

import static org.junit.Assert.*;
import grabbers.GrabberException;
import grabbers.TmdbInfoGrabber;

import org.junit.Test;


public class InfoGrabberTest {

	@Test
	public void testSearchForMovie() {
		try {
			TmdbInfoGrabber.searchForMovie("Batman");
		} catch (GrabberException e) {
			fail(e.getMessage());
		}
		
	}

}
