package helpers;

import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import play.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple static helper for configuration vars
 * User: tuxburner
 * Date: 5/1/13
 * Time: 11:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class ConfigurationHelper {

  public static <T> Map<String,T> createValMap(final String configKey) {

    ConfigObject configObject = ConfigFactory.load().getObject(configKey);
    if(configObject == null) {
      if( Logger.isErrorEnabled() == true) {
        Logger.error("Could not find configkey: "+configKey);
      }
      return null;
    }

    final Map<String,T> map = new HashMap<String, T>();
    for (Map.Entry<String, Object> entry : configObject.unwrapped().entrySet()) {
      final Object value = entry.getValue();
      map.put(entry.getKey(), (T) value);
    }

    return map;
  }

}
