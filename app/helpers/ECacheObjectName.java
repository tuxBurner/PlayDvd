package helpers;

/**
 * User: tuxburner
 * Date: 5/9/13
 * Time: 1:36 PM
 */
public enum  ECacheObjectName {

  SHOPPINGCART(60 * 15);

  public final int cacheTime;

  ECacheObjectName(int cacheTime) {
    this.cacheTime = cacheTime;
  }
}
