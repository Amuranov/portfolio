package be.ac.ulb.infof307.g04.model.client;

import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import be.ac.ulb.infof307.g04.model.SendData;

/**
 * This class is an "interface" that let us send requests related to the user session.
 */
public class UserCommunication {

	private Client client;
	private WebResource webResource;
	private Session session;

	/**
	 * Constructs a new {@link UserCommunication} object.
	 */
	public UserCommunication() {
		this.client = Client.create();
		//client.addFilter(new LoggingFilter(System.out));
		session     = null;
		webResource = null;
	}

	/**
	 * Sends an HTTP request to the server in order to sign up a new user.
	 * @param username The username.
	 * @param password The password.
	 * @param email    The email.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int signUp(String username, String password, String email) {
		webResource = client.resource("http://localhost:8000/server/users/signup");
		session = new Session(username, password, email);
		return webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, session).getStatus();
	}

	/**
	 * Sends an HTTP request to the server in order to log in a user.
	 * @param username The username.
	 * @param password The password.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public Session login(String username, String password) {
		webResource = client.resource("http://localhost:8000/server/users/login");
		session = new Session(username, password,"");
		try{
			session =  (Session) webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(Session.class, session);
		}catch(Exception e){
			session = null;
		}
		return session;
	}

	/**
	 * Sends an HTTP request to the server in order to modify the profile of a user.
	 * @param username The username.
	 * @param password The password.
	 * @param email    The email.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int update(String username, String password, String email) {
		webResource = client.resource("http://localhost:8000/server/users/update");
		session = new Session(username, password, email);
		return webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, session).getStatus();
	}

	/**
	 * Sends an HTTP request to the server in order to log out a user.
	 * @param username The username.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int logout(String token, String user,String username) {
		webResource = client.resource("http://localhost:8000/server/users/logout");
		SendData tmp = new SendData<Object>(token,user,null);
		return webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class,tmp).getStatus();
	}

	/**
	 * Sends an HTTP request to the server in order to delete a user.
	 * @param username The username.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int delete(String username) {
		webResource = client.resource("http://localhost:8000/server/users/delete/"+username);
		return webResource.delete(ClientResponse.class).getStatus();
	}
}
