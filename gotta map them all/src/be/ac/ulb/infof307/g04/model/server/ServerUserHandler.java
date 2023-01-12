package be.ac.ulb.infof307.g04.model.server;

import java.io.IOException;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import be.ac.ulb.infof307.g04.model.RegistrationToken;
import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.SendData;
import be.ac.ulb.infof307.g04.model.Token;
import be.ac.ulb.infof307.g04.model.TokenManager;
import be.ac.ulb.infof307.g04.model.User;
import be.ac.ulb.infof307.g04.model.client.Session;
import be.ac.ulb.infof307.g04.model.dao.RegistrationTokenDAO;
import be.ac.ulb.infof307.g04.model.dao.UserDAO;

@Path("/users")
public class ServerUserHandler {

	private TokenManager tokenManager;
	private MailHandler mail;
	private ResponseCode responseCode;

	public ServerUserHandler() {
		this.tokenManager = Server.getTokenManager();
		this.mail = new MailHandler();
		this.responseCode = null;
	}

	/**
	 * Create a new user.
	 * @return An http code to send to the client.
	 */
	@POST
	@Path("/signup")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response signUp(Session client) {
		responseCode = ResponseCode.BAD_REQUEST;
		User insertedUser = new UserDAO().insert(new User(client.getUser().getUsername(), client.getUser().getPwd(), client.getUser().getMail()));
		if(insertedUser != null) {
			String token = UUID.nameUUIDFromBytes(insertedUser.getUsername().getBytes()).toString().replace("-", "");
			new RegistrationTokenDAO().insert(new RegistrationToken(insertedUser.getID(), token));

			responseCode = mail.sendMail(client.getUser().getMail(), "Hello, please click here to confirm your email address : ", insertedUser.getID(), token) ? ResponseCode.OK : responseCode;
		}
		return Response.status(responseCode.getValue()).build();
	}

	/**
	 * Manages an e-mail received.
	 * @return An http status to send to the client.
	 */
	@GET
	@Path("/checkmail/{id}/{token}")
	public Response checkMail(@PathParam("id") int id, @PathParam("token") String token) {
		responseCode = ResponseCode.BAD_REQUEST;
		if(new RegistrationTokenDAO().exists(id, token)) {
			new RegistrationTokenDAO().delete(new RegistrationToken(id, token));
			responseCode = ResponseCode.OK;
		}
		return  Response.status(responseCode.getValue()).build();
	}

	/**
	 * Login a user and give him a token for future authentications
	 * @return An http status to send to the client or null if the request is already sent.
	 * @throws IOException If there are an error on the creation of the authentications token.
	 */
	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(Session client){
		Session returnedClient = null;
		Token token = null;
		responseCode = ResponseCode.BAD_REQUEST;
		User user = new UserDAO().credentialsVerification(client.getUser().getUsername().toLowerCase(), client.getUser().getPwd());
		if(user != null) {
			if(new RegistrationTokenDAO().findByName(user.getUsername()) == null) {
				token = tokenManager.addToken(client.getUser().getUsername());
				if(token != null) {
					client.setTokenSession(token.getAccess());
					client.getUser().setID(user.getID());
					responseCode = ResponseCode.OK;
					returnedClient = client;
				}
			}
		}
		return Response.status(responseCode.getValue()).entity(returnedClient).build();
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(Session client) {
		return Response.ok().build();
	}
	/**
	 *
	 * @param body A json contain all the information about the user request.
	 * @return An http status to send to the client.
	 */
	@POST
	@Path("/logout")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response logout(SendData<Object> data) {
		if(Server.getTokenManager().checkToken(data.getUser(), data.getToken())){
			Server.getTokenManager().deleteToken(data.getUser());
			return Response.ok().build();
		}
		return Response.status(ResponseCode.BAD_REQUEST.getValue()).build();

	}

	@DELETE
	@Path("/delete/{username}")
	public Response delete(@PathParam("username") String username) {
		User user = new User();
		user.setUsername(username);
		new UserDAO().delete(user);
		return Response.ok().build();
	}
}
