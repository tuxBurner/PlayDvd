package helpers;

import org.apache.commons.lang3.StringUtils;
import play.cache.Cache;
import play.mvc.Controller;

/**
 *
 * User: tuxburner
 * Date: 5/9/13
 * Time: 1:33 PM
 */
public class CacheHelper {

  /**
   * Gets an object from the session
   *
   * @param  objectName
   *
   * @return
   */
  public static <T> T getObjectFromCache(final ECacheObjectName objectName) {
    final String uuid = createCacheUUID();
    // Access the cache
    T cacheVal = (T) Cache.get(uuid + objectName.name());

    return cacheVal;
  }

  /**
   * Writes the object to the cache
   * @param objectName
   * @param  obj
   *
   */
  public static void setObjectToCache(final ECacheObjectName objectName, final Object obj) {
    final String uuid = createCacheUUID();

    Cache.set(uuid+objectName.name(), obj, objectName.cacheTime);
  }

  /**
   * Removes the Object from the Cache
   * @param objectName
   */
  public static void removeFromCache(final ECacheObjectName objectName) {
    final String uuid = createCacheUUID();
    Cache.remove(uuid+objectName);
  }


  /**
   * Checks if the user session has a uuid and if not it creates one for the cache
   * @return
   */
  private static String createCacheUUID() {
    // Generate a unique ID
    String uuid= Controller.session("uuid");
    if(StringUtils.isEmpty(uuid)) {
      uuid=java.util.UUID.randomUUID().toString();
      Controller.session("uuid", uuid);
    }

    return uuid;
  }

}
