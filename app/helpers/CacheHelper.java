package helpers;

import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.Cache;
import play.mvc.Controller;

import java.util.concurrent.Callable;

/**
 * Helper for the cache stuff
 */
public class CacheHelper {


  public static  <T> T getObject(final ECacheObjectName objectName, final String postFix) {
    return (T) Cache.get(createCacheKey(objectName,postFix));
  }

  public static  <T> T getObjectOrElse(final ECacheObjectName objectName,final String postFix,final Callable<T> block) {
    try {
      return Cache.getOrElse(createCacheKey(objectName,postFix),block,objectName.cacheTime);
    } catch (Exception e) {
      if(Logger.isErrorEnabled()){
        Logger.error("An error happend while getting the object via key: "+createCacheKey(objectName,postFix),e);
      }
      return null;
    }
  }

  public static void setObject(final ECacheObjectName objectName,final String postFix, final Object obj) {
    Cache.set(createCacheKey(objectName,postFix), obj, objectName.cacheTime);
  }

  /**
   * Removes an {@link Object} from the {@link Cache}
   * @param objectName
   */
  public static void removeObj(final ECacheObjectName objectName,final String postFix) {
    Cache.remove(createCacheKey(objectName,postFix));
  }

  /**
   * Gets an {@link Object} from the {@link Cache} for the current session
   *
   * @param  objectName
   *
   * @return
   */
  public static <T> T getSessionObject(final ECacheObjectName objectName) {
    final String uuid = createCacheUUID();
    return getObject(objectName,uuid);
  }

  public static <T> T getSessionObjectOrElse(final ECacheObjectName objectName,final Callable<T> block) {
     return getObjectOrElse(objectName,createCacheUUID(),block);
  }

  /**
   * Writes an {@link Object} to the {@link Cache} for the current session
   * @param objectName
   * @param  obj
   *
   */
  public static void setSessionObject(final ECacheObjectName objectName, final Object obj) {
    final String uuid = createCacheUUID();
    setObject(objectName,uuid,obj);
  }

  /**
   * Removes an {@link Object} from the {@link Cache} for the current session
   * @param objectName
   */
  public static void removeSessionObj(final ECacheObjectName objectName) {
    final String uuid = createCacheUUID();
    removeObj(objectName,uuid);
  }

  /**
   * Create the key for the cache
   * @param objectName
   * @param postFix
   * @return
   */
  private static String createCacheKey(final ECacheObjectName objectName, final String postFix) {
    return objectName.name()+StringUtils.trimToEmpty(postFix);
  }

  /**
   * Checks if the user session has a uuid and if not it creates one for the {@link Cache}
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
