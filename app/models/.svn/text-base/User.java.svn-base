package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.data.format.Formats;
import play.data.validation.Constraints.Required;
import play.db.ebean.Model;

@Entity
public class User extends Model {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3236069629608718953L;

	@Id
	public Long id;

	@Required
	@Formats.NonEmpty
	@Column(unique = true)
	public String userName;

	@Required
	@Formats.NonEmpty
	public String password;

	@Required
	@Formats.NonEmpty
	public String email;
	

    public static Model.Finder<String,User> find = new Model.Finder<String, User>(String.class, User.class);
	
	/**
	 * Saves the user to the database
	 * 
	 * @param user
	 */
	public static void create(final User user) {
		user.save();
	}

	public static User authenticate(final String username, final String password) {
		return find.where()
	            .eq("userName", username)
	            .eq("password", password)
	            .findUnique();
	}
	
	/**
	 * Checks if a {@link User} wit the given username exists
	 * @param username
	 * @return
	 */
	public static boolean checkIfUserExsists(final String username) {
		return getUserByName(username) != null;
	}
	
	/**
	 * Gets a {@link User} by the given username
	 * @param username
	 * @return
	 */
	public static User getUserByName(final String username) {
		return find.where().eq("userName", username).findUnique();
	}

}
