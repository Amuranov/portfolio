package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import be.ac.ulb.infof307.g04.model.RegistrationToken;
import be.ac.ulb.infof307.g04.model.SQLParameter;


/**
 * Allows to acces the whole data concerning a RegistrationToken from the Database
 */
public class RegistrationTokenDAO extends DAO<RegistrationToken> {

	/**
	 * retrieves the RegistrationToken of the given username from the database
	 * @param username 
	 * @return the RegistrationToken related to the username
	 */
	public RegistrationToken findByName(String username) {
		RegistrationToken registrationToken = null;
		String query = "SELECT * FROM RegistrationToken rt WHERE userID = (SELECT u.id FROM User u WHERE username = ?)";
		try(ResultSet result = db.executeQuery(query, new SQLParameter(username))) {
			if (result.next())
				registrationToken = new RegistrationToken(result.getInt("userID"), result.getString("token"));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in RegistrationTokenDAO findByName: " + exception);
		}
		return registrationToken;
	}

	/**
	 * Check in the database if there is a row corresponding to an ID and a Token
	 * @param id: the id of the token to retrieve
	 * @param token: the token number related to the ID
	 * @return true if the token exists, false otherwise
	 */
	public boolean exists(int id, String token) {
		String query = "SELECT * FROM RegistrationToken rt WHERE userID = ? AND token = ?";
		try(ResultSet result = db.executeQuery(query, new SQLParameter(id), new SQLParameter(token))) {
			return result.next();
		} catch (SQLException exception) {
			System.err.println("Unexpected error in RegistrationTokenDAO exists: "+exception);
		}
		return false;
	}
	
	/**
	 * Insert a new RegistrationToken to the database
	 * @param registrationToken
	 * @return the RegistrationToken inserted
	 */
	public RegistrationToken insert(RegistrationToken registrationToken) {
		String query = "INSERT INTO RegistrationToken(userID, token) VALUES(?, ?)";
		try {
			int affectedRows = db.executeUpdate(query, 
				new SQLParameter(registrationToken.getUserID()),
				new SQLParameter(registrationToken.getToken())
			);
			if(affectedRows == 0)
				throw new SQLException("Registration Token insert failed.");
			return registrationToken;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in RegistrationTokenDAO insert: " + exception);
			return registrationToken;
		}
	}

	/**
	 * Update a RegistrationToken to the database
	 * @param registrationToken
	 * @return the Registrationtoken updated
	 */
	public RegistrationToken update(RegistrationToken registrationToken) {
		String query = "UPDATE RegistrationToken SET token = ? WHERE userID = ?";
		try {
			int affectedRows = db.executeUpdate(query, 
				new SQLParameter(registrationToken.getToken()),
				new SQLParameter(registrationToken.getUserID())
			);
			if(affectedRows == 0)
				throw new SQLException("The update hasn't affected anything !");
			return registrationToken;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in RegistrationTokenDAO update: "+exception);
			return registrationToken;
		}
	}

	/**
	 * Delete a RegistrationToken from the database
	 * @param registrationToken 
	 */
	public void delete(RegistrationToken registrationToken) {
		String query = "DELETE FROM RegistrationToken WHERE userID = ?";
		try {
			int affectedRows = db.executeUpdate(query, new SQLParameter(registrationToken.getUserID()));
			if(affectedRows == 0)
				throw new SQLException("The update hasn't affected anything !");
		} catch (SQLException exception) {
			System.err.println("Unexpected error in RegistrationTokenDAO delete: "+exception);
		}
	}

	public RegistrationToken findById(int id) {
		RegistrationToken rt = null;
		String query = "SELECT * FROM registrationtoken WHERE userID = ?";
		try(ResultSet r = db.executeQuery(query, new SQLParameter(id))) {
			if(r.next())
				rt = new RegistrationToken(r.getInt("userID"), r.getString("token"));
		} catch(SQLException e) {
			System.err.println("Error Registration Token find by id : " + e);
		}
		return rt;
	}

}
