package models.settings;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

/**
 * This is a simple setting db helper
 * 
 * @author tuxburner
 * 
 */
@Entity
public class Setting extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6036228031179729770L;

	@Id
	public Long id;

	@Column(nullable = false)
	public String bundle;

	@Column(nullable = false, name = "ke_y")
	public String key;

	public String value;

	public static Finder<Long, Setting> find = new Finder<Long, Setting>(
			Long.class, Setting.class);

	/**
	 * Finds a setting for the given bundle
	 * 
	 * @param bundle
	 * @param key
	 * @return
	 */
	public static Setting getSetting(final String bundle, final String key) {
		return find.where().eq("bundle", bundle).eq("key", key).findUnique();
	}
}
