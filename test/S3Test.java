import helpers.EImageType;
import helpers.ImageHelper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;

/**
 * User: tuxburner
 * Date: 5/18/13
 * Time: 10:31 AM
 */
public class S3Test {

  @Test
  public void findById() {


    final List<String> plugins = new ArrayList<String>();
        plugins.add("plugins.s3.S3Plugin");

    running(fakeApplication(), new Runnable() {
      public void run() {
        ImageHelper.createFileFromUrl(4711L,"http://d3gtl9l2a4fn1j.cloudfront.net/t/p/original/5kDZS7ChCCXloWsNUctpS4sMal9.jpg", EImageType.POSTER);
      }
    });
  }

}
