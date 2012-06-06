package models.settings;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple cache holding the settings
 * @author tuxburner
 *
 */
public class SettingCache {
	
	private static SettingCache instance = null;
	
	private Map<String, Map<String, String>> cache = new HashMap<String, Map<String,String>>();
	
	public static SettingCache singleton() {
		if(instance == null) {
			instance = new SettingCache();
		}
		
		return instance;
	}
	
	/**
	 * Loads a setting from the database or from the internal cahce if already loaded
	 * @param bundle
	 * @param key
	 * @return
	 */
	public String getSetting(final String bundle, final String key) {
		// first we check the cache
		if(cache.containsKey(bundle) == false) {
			cache.put(bundle, new HashMap<String, String>());
		}
		
		// not in the cache ?? well 
		Map<String, String> bundleCache = cache.get(bundle);
		if(bundleCache.containsKey(key) == false) {
			Setting setting = Setting.getSetting(bundle, key);
			if(setting == null) {
				return null;
			} 
			
			bundleCache.put(key, setting.value);
		}
		
		return bundleCache.get(key);
	}

}
