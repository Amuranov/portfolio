package be.ac.ulb.infof307.g04.model.client;

import be.ac.ulb.infof307.g04.model.User;

/**
 * This class contains all the information about the session of a client.
 */
public class Session {
	
	private User user;
	
	private String tokenSession;
	private String tokenRefresh;
	
	// A default empty constructor is necessary in order to 
	// convert this object to JSON format by the Jersey library.
	public Session() {
		
	}
	
	/**
	 * Create a client with his username and password.
	 * @param username The name of the user.
	 * @param password The password of the user.
	 * @param email The email of the user.
	 */
	public Session(String username, String password, String email) {
		this("", "", username, password, email);
	}
	
	/**
	 * Create a client with his authentifacation and profile informations.
	 * @param tokenSession A token to acces to the server.
	 * @param tokenRefresh A token to refresh the acces token.
	 * @param username Username of the client.
	 * @param password Password of the client.
	 * @param email Email of the client
	 */
	public Session(String tokenSession, String tokenRefresh, String username, String password, String email) {
		this.tokenSession = tokenSession;
		this.tokenRefresh = tokenRefresh;
		user = new User(username, password, email);
	}
	
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getTokenSession() {
		return tokenSession;
	}

	public void setTokenSession(String tokenSession) {
		this.tokenSession = tokenSession;
	}

	public String getTokenRefresh() {
		return tokenRefresh;
	}

	public void setTokenRefresh(String tokenRefresh) {
		this.tokenRefresh = tokenRefresh;
	}
}
