package helpers;

import models.Bookmark;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.CacheApi;
import play.cache.SyncCacheApi;
import play.mvc.Controller;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Helper for the cache stuff
 */
@Singleton
public class CacheHelper {

  private final SyncCacheApi cache;

  @Inject
  public CacheHelper(final SyncCacheApi cache) {
    this.cache = cache;
  }


  public <T> T getObject(final ECacheObjectName objectName, final String postFix) {
    return cache.get(createCacheKey(objectName,postFix));
  }

  public  <T> T getObjectOrElse(final ECacheObjectName objectName,final String postFix,final Callable<T> block) {
    try {
      return cache.getOrElseUpdate(createCacheKey(objectName,postFix),block,objectName.cacheTime);
    } catch (Exception e) {
      if(Logger.isErrorEnabled()){
        Logger.error("An error happend while getting the object via key: "+createCacheKey(objectName,postFix),e);
      }
      return null;
    }
  }

  public  void setObject(final ECacheObjectName objectName,final String postFix, final Object obj) {
    cache.set(createCacheKey(objectName,postFix), obj, objectName.cacheTime);
  }

  /**
   * Removes an {@link Object} from the {@link CacheApi}
   * @param objectName
   */
  public  void removeObj(final ECacheObjectName objectName,final String postFix) {
    cache.remove(createCacheKey(objectName,postFix));
  }


  public <T> T getSessionObjectOrElse(final ECacheObjectName objectName,final Callable<T> block) {
     return getObjectOrElse(objectName,createCacheUUID(),block);
  }

  /**
   * Writes an {@link Object} to the {@link CacheApi} for the current session
   * @param objectName
   * @param  obj
   *
   */
  public  void setSessionObject(final ECacheObjectName objectName, final Object obj) {
    final String uuid = createCacheUUID();
    setObject(objectName,uuid,obj);
  }

  /**
   * Removes an {@link Object} from the {@link CacheApi} for the current session
   * @param objectName
   */
  public  void removeSessionObj(final ECacheObjectName objectName) {
    final String uuid = createCacheUUID();
    removeObj(objectName,uuid);
  }

  /**
   * Create the key for the cache
   * @param objectName
   * @param postFix
   * @return
   */
  private  String createCacheKey(final ECacheObjectName objectName, final String postFix) {
    return objectName.name()+StringUtils.trimToEmpty(postFix);
  }

  /**
   * Checks if the user session has a uuid and if not it creates one for the {@link CacheApi}
   * @return
   */
  private  String createCacheUUID() {
    // Generate a unique ID
    String uuid= Controller.session("uuid");
    if(StringUtils.isEmpty(uuid)) {
      uuid=java.util.UUID.randomUUID().toString();
      Controller.session("uuid", uuid);
    }

    return uuid;
  }

  /**
   * Gets all {@Dvd#id} which the user bookedmarked
   * @return
   */
  public Set<Long> getBookmarkedCopyIds() {
    return getSessionObjectOrElse(ECacheObjectName.BOOKMARKS,() -> Bookmark.getBookmarkCopyIdsForUser());
  }

  /**
   * Gets the {@link CacheShoppingCart} from the {@link CacheApi} if it is null a new instance is created
   * @return
   */
  public  CacheShoppingCart  getShoppingCartFromCache(){
    return getSessionObjectOrElse(ECacheObjectName.SHOPPINGCART,() -> new CacheShoppingCart());
  }



}
