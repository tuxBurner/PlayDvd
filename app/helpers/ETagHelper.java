package helpers;

import play.Logger;
import play.api.libs.Codecs;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is a helper for creating and managing etags
 * User: tuxburner
 * Date: 3/29/13
 * Time: 1:45 PM
 */
public class ETagHelper {

  private static Map<String, Long> FILES_LAST_MODIFIED = new ConcurrentHashMap<String, Long>();
  private static Map<String, String> ETAGS = new ConcurrentHashMap<String, String>();

  public static String getEtag(final File file) {
    // check etag
    final long lastModified = file.lastModified();
    final String absolutePath = file.getAbsolutePath();
    if (FILES_LAST_MODIFIED.containsKey(absolutePath) == false) {
      FILES_LAST_MODIFIED.put(absolutePath, lastModified);
      ETAGS.put(absolutePath, createEtag(file));
    } else {
      final Long storedModified = FILES_LAST_MODIFIED.get(absolutePath);
      if(storedModified.equals(lastModified) == false) {
        if(Logger.isDebugEnabled() == true) {
          Logger.debug("ETAG changed for: "+absolutePath);
        }
        FILES_LAST_MODIFIED.put(absolutePath, lastModified);
        ETAGS.put(absolutePath, createEtag(file));
      }
    }
    return ETAGS.get(absolutePath);
  }

  /**
   * Creates an Etag for the given {@link File}
   *
   * @param file
   * @return
   */
  private static String createEtag(final File file) {
    return "\""+Codecs.sha1(file.getAbsolutePath() + file.lastModified())+"\"";
  }

  public static String getEtag(final String identIfier) {
    return ETAGS.get(identIfier);
  }

  public static void createEtag(final String identIfier, final byte[] bytes) {
    ETAGS.put(identIfier,createEtag(bytes));
  }

  public static void removeEtag(final String identIfier) {
    ETAGS.remove(identIfier);
  }

  private static String createEtag(final byte[] bytes) {
    return "\""+Codecs.sha1(bytes)+"\"";
  }

}
