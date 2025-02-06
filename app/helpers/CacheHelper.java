package helpers;

import models.Bookmark;
import objects.shoppingcart.CacheShoppingCart;
import org.apache.commons.lang3.StringUtils;
import play.Logger;
import play.cache.SyncCacheApi;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
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
    return (T) cache.get(createCacheKey(objectName, postFix)).get();
  }

  public <T> T getObjectOrElse(final ECacheObjectName objectName, final String postFix, final Callable<T> block) {
    try {
      return cache.getOrElseUpdate(createCacheKey(objectName, postFix), block, objectName.cacheTime);
    } catch (Exception e) {
      if (Logger.isErrorEnabled()) {
        Logger.error("An error happend while getting the object via key: " + createCacheKey(objectName, postFix), e);
      }
      return null;
    }
  }

  public void setObject(final ECacheObjectName objectName, final String postFix, final Object obj) {
    cache.set(createCacheKey(objectName, postFix), obj, objectName.cacheTime);
  }

  /**
   * Removes an {@link Object} from the Cache
   *
   * @param objectName
   */
  public void removeObj(final ECacheObjectName objectName, final String postFix) {
    cache.remove(createCacheKey(objectName, postFix));
  }


  public <T> T getSessionObjectOrElse(final ECacheObjectName objectName, final Http.Request request, final Callable<T> block) {
    return getObjectOrElse(objectName, createCacheUUID(request), block);
  }

  /**
   * Writes an {@link Object} to the Cache for the current session
   *
   * @param objectName
   * @param obj
   */
  public void setSessionObject(final ECacheObjectName objectName, final Object obj, final Http.Request request) {
    final String uuid = createCacheUUID(request);
    setObject(objectName, uuid, obj);
  }

  /**
   * Removes an {@link Object} from the Cache for the current session
   *
   * @param objectName
   */
  public void removeSessionObj(final ECacheObjectName objectName, final Http.Request request) {
    final String uuid = createCacheUUID(request);
    removeObj(objectName, uuid);
  }

  /**
   * Create the key for the cache
   *
   * @param objectName
   * @param postFix
   * @return
   */
  private String createCacheKey(final ECacheObjectName objectName, final String postFix) {
    return objectName.name() + StringUtils.trimToEmpty(postFix);
  }

  /**
   * Checks if the user session has a uuid and if not it creates one for the {@link CacheApi}
   *
   * @return
   */
  private String createCacheUUID(Http.Request request) {
    // Generate a unique ID
    Optional<String> uuid = request.session().get("uuid");
    if (uuid.isEmpty()) {
      uuid = Optional.of(java.util.UUID.randomUUID().toString());
      request.session().adding("uuid", uuid.get());
    }


    return uuid.get();
  }

  /**
   * Gets all {@Dvd#id} which the user bookedmarked
   *
   * @return
   */
  public Set<Long> getBookmarkedCopyIds(final Http.Request request) {
    return getSessionObjectOrElse(ECacheObjectName.BOOKMARKS, request, () -> Bookmark.getBookmarkCopyIdsForUser(request));
  }

  /**
   * Gets the {@link CacheShoppingCart} from the Cache if it is null a new instance is created
   *
   * @return
   */
  public CacheShoppingCart getShoppingCartFromCache(final Http.Request request) {
    return getSessionObjectOrElse(ECacheObjectName.SHOPPINGCART, request, () -> new CacheShoppingCart());
  }


}
