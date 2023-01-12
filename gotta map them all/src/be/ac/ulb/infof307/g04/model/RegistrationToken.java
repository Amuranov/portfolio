package be.ac.ulb.infof307.g04.model;


/**
 * Allows to know which user is requesting while he's connected
 */
public class RegistrationToken {
	private int userID;
	private String token;

	public RegistrationToken() { 
		
	}

	public RegistrationToken(int id, String token) {
		this.userID = id;
		this.token = token;
	}

	public int getUserID() {
		return userID;
	}
	public void setUserID(int userID) {
		this.userID = userID;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
}
