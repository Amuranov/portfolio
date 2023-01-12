package be.ac.ulb.infof307.g04.model.client;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import be.ac.ulb.infof307.g04.model.LikesStat;

/**
 * This class is an "interface" that let us communicate with the server in a REST way.
 */
public class LikesStatCommunication {
	private Client client;
	private WebResource webResource;
	private LikesStat likesStat;

	public LikesStatCommunication() {
		this.client         = Client.create();
		//client.addFilter(new LoggingFilter(System.out));
		this.likesStat = null;
		this.webResource    = null;
	}

	/**
	 * This method sends an HTTP request to the server in order to insert a likeStats in the database.
	 * @param userID The ID of the user.
	 * @param signalisation_id The ID of the signaling.
	 * @param stat_type The type of like.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int insert(Integer userID, Integer signalisation_id, Integer stat_type){
		webResource = client.resource("http://localhost:8000/server/likes/insert/"+userID+"/"+signalisation_id+"/"+stat_type);
		return webResource.get(ClientResponse.class).getStatus();
	}

	/**
	 * This method sends an HTTP request to the server in order to insert a likeStats in the database.
	 * @param userID The ID of the user.
	 * @param signalisation_id The ID of the signaling.
	 * @param stat_type The type of like.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int delete(Integer userID, Integer pokemonID, Integer stat_type){
		webResource = client.resource("http://localhost:8000/server/likes/delete/"+userID+"/"+pokemonID+"/"+stat_type);
		return webResource.delete(ClientResponse.class).getStatus();
	}

	/**
	 * This method sends an HTTP request to the server in order to get likes number on the signaling.
	 * @param signalisationId The ID of the signaling
	 * @return numbers of likes
	 */
	public String getLikes(Integer signalisationId){
		webResource = client.resource("http://localhost:8000/server/likes/getLikes");
		likesStat = new LikesStat();
		likesStat.setSignalingId(signalisationId);
		return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).post(String.class, likesStat);
	}

	/**
	 * This method sends an HTTP request to the server in order to get dislikes number on the signaling.
	 * @param signalisationId The ID of the signaling
	 * @return numbers of dislikes
	 */
	public String getDislikes(Integer signalisationId){
		webResource = client.resource("http://localhost:8000/server/likes/getDislikes");
		likesStat = new LikesStat();
		likesStat.setSignalingId(signalisationId);
		return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).post(String.class, likesStat);
	}

	/**
	 * This method sends an HTTP request to the server in order to get likes type of the current user on the signaling.
	 * @param userID The ID of the user
	 * @param signalisationId The ID of the signaling
	 * @return type of likes of the user
	 */
	public String userLikeType(Integer userID, Integer signalisationId){
		webResource = client.resource("http://localhost:8000/server/likes/userLikeType");
		likesStat = new LikesStat(userID,signalisationId,0);
		return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).post(String.class, likesStat);
	}
}
