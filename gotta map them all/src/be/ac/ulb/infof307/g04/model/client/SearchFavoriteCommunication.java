package be.ac.ulb.infof307.g04.model.client;

import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

import be.ac.ulb.infof307.g04.model.SearchFavorite;

/**
 * This class is an "interface" that let us communicate with the server in a REST way.
 */
public class SearchFavoriteCommunication {
	private Client client;
	private WebResource webResource;
	private SearchFavorite searchFavorite;

	public SearchFavoriteCommunication() {
		this.client         = Client.create();
		//client.addFilter(new LoggingFilter(System.out));
		this.searchFavorite = null;
		this.webResource    = null;
	}

	/**
	 * This method sends an HTTP request to the server in order to insert a {@link SearchFavorite} in the database.
	 * @param userID The ID of the user.
	 * @param typeID the ID of the selected type of pokemons.
	 * @param pokemonID the ID of the pokemon's name.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int insert(Integer userID, Integer typeID, Integer pokemonID){
		webResource = client.resource("http://localhost:8000/server/favorites/insert");
		searchFavorite = new SearchFavorite(userID, typeID, pokemonID);
		ClientResponse response = webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse.class, searchFavorite);
		return response.getStatus();
	}
}
