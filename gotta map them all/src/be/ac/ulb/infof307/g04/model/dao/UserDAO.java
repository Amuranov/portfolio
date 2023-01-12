package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import be.ac.ulb.infof307.g04.model.SQLParameter;
import be.ac.ulb.infof307.g04.model.User;

/**
 * Allows access to all informations from a user from the database
 */
public class UserDAO extends DAO<User> {

	/**
	 * Insert a new user in the database
	 * @param user
	 * @return the user added
	 */
	public User insert(User user) {
		String query = "INSERT INTO User(username, pwd, mail) VALUES(?, ?, ?)";
		try {		
			int affectedRows = db.executeUpdate(
				query, 
				new SQLParameter(user.getUsername()), 
				new SQLParameter(user.getPwd()), 
				new SQLParameter(user.getMail())
			);
			if(affectedRows == 0)
				throw new SQLException("UserDAO => Insert could not be performed.");
			else
				// In order to update the user after its insertion.
				user = credentialsVerification(user.getUsername(), user.getPwd());
			return user;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in UserDAO insert: " + exception);
			return null;
		}
	}

	/**
	 * Delete an user from the database
	 * @param user
	 */
	public void delete(User user) {
		String query = "DELETE FROM User WHERE username = ?";
		try {
			int affectedRows = db.executeUpdate(query, new SQLParameter(user.getUsername()));
			if(affectedRows == 0)
				throw new SQLException("UserDAO delete could not delete anything with user : " + user.toString());
		} catch (SQLException exception) {
			System.err.println("Unexpected error in UserDAO delete: " + exception);
		}
	}

	/**
	 * Do a credentials verification in the database
	 * @param username the username of the user trying to log in
	 * @param password the password of the user trying to log in
	 * @return the user logged in
	 */
	public User credentialsVerification(String username, String password) {
		User user = null;
		String query = "SELECT * FROM user WHERE username = ? AND pwd = ?";
		try(ResultSet result = db.executeQuery(query, new SQLParameter(username), new SQLParameter(password))) {
			if (result.next())
				user = new User(
					result.getInt("id"), 
					result.getString("username"), 
					result.getString("pwd"), 
					result.getString("mail")
				);
		} catch (SQLException exception)
			{System.err.println("Unexpected error in UserDAO credentialsVerification: "+exception);
		}
		return user;
	}
	
	
	public User findById(int id) {
		User user = null;
		try(ResultSet result = db.executeQuery("SELECT * FROM user WHERE id = ?", new SQLParameter(id))) {
			if (result.next())
				user = new User(
					result.getInt("id"), 
					result.getString("username"), 
					result.getString("pwd"), 
					result.getString("mail")
				);
		} catch (SQLException exception) {
			System.err.println("Unexpected error in UserDAO credentialsVerification: "+exception);
		}
		return user;
	}
}

