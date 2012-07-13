package grabbers;

import static org.junit.Assert.*;
import grabbers.GrabberException;
import grabbers.TmdbGrabber;

import org.junit.Test;


public class TmdbInfoGrabberTest {

	@Test
	public void testSearchForMovie() {
		try {
			TmdbGrabber.searchForMovie("Batman");
		} catch (GrabberException e) {
			fail(e.getMessage());
		}
		
	}

}
