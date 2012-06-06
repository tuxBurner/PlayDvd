package tmdb;

import static org.junit.Assert.*;

import org.junit.Test;

public class InfoGrabberTest {

	@Test
	public void testSearchForMovie() {
		try {
			InfoGrabber.searchForMovie("Batman");
		} catch (GrabberException e) {
			fail(e.getMessage());
		}
		
	}

}
