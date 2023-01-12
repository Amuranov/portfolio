package be.ac.ulb.infof307.g04.model.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import be.ac.ulb.infof307.g04.model.LocalisationPokemon;
import be.ac.ulb.infof307.g04.model.SendData;

/**
 * This class is an "interface" that let us communicate with the server in a REST way.
 */
public class PokemonCommunication {

	// The sender
	private Client client;
	private WebResource webResource;
	private LocalisationPokemon localisationPokemon;

	public PokemonCommunication() {
		this.client         = Client.create();
		localisationPokemon = null;
		webResource         = null;
	}

	/**
	 * This method sends an HTTP request to the server in order to insert a {@link LocalisationPokemon} in the database.
	 * @param token 
	 * @param pokemonId The ID of the concerned pokemon.
	 * @param userId The ID of the user that marked the pokemon on the map.
	 * @param latitude Latitude of the pokemon.
	 * @param longitude Longitude of the pokemon.
	 * @param atTime When the pokemon has been added to the map.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public String insert(String token, String user, int pokemonId, int userId, double latitude, double longitude, String atTime) {
		webResource = client.resource("http://localhost:8000/server/pokemons/insert");
		localisationPokemon = new LocalisationPokemon(pokemonId, userId, latitude, longitude, atTime);
		SendData tmp = new SendData<LocalisationPokemon>(token,user,localisationPokemon);
		return webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.TEXT_PLAIN).post(String.class,tmp);
	}
	public int update(String token, String user,int pokemonId, int userId, double latitude, double longitude, String atTime,
			int attack,int defense,int life) {
		webResource = client.resource("http://localhost:8000/server/pokemons/update");
		localisationPokemon = new LocalisationPokemon(pokemonId, userId, latitude, longitude, atTime,
				attack,defense,life);
		SendData tmp = new SendData<LocalisationPokemon>(token,user,localisationPokemon);
		return webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse	.class, tmp).getStatus();
	}
	/**
	 * @see the other update method.
	 * @param localisationPokemon the {@link LocalisationPokemon}.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public int update(String token, String user,LocalisationPokemon localisationPokemon) {
		return update(token,user,localisationPokemon.getPokemonId(),
					  localisationPokemon.getUserId(),
					  localisationPokemon.getLatitude(),
					  localisationPokemon.getLongitude(),
					  localisationPokemon.getAtTime(),
					  localisationPokemon.getAttackPoints(),
					  localisationPokemon.getDefPoints(),
					  localisationPokemon.getLifePoints());
	}
	/**
	 * @param token 
	 * @see the other insert method.
	 * @param localisationPokemon the {@link LocalisationPokemon}.
	 * @return The response code of the server (typically 200 for OK, 500 for server error, etc.).
	 */
	public String insert(String token,String user, LocalisationPokemon localisationPokemon) {
		return insert(token, user,localisationPokemon.getPokemonId(),
					  localisationPokemon.getUserId(),
					  localisationPokemon.getLatitude(),
					  localisationPokemon.getLongitude(),
					  localisationPokemon.getAtTime());
	}

	public List<LocalisationPokemon> readAll(){
		webResource = client.resource("http://localhost:8000/server/pokemons/readAll");
	    List<LocalisationPokemon> pokemonsList = webResource.accept(MediaType.APPLICATION_XML).get(new GenericType<List<LocalisationPokemon>>(){});
		return pokemonsList;
	}

	public int delete(String token, String user,int locId){
		webResource = client.resource("http://localhost:8000/server/pokemons/delete");
		SendData tmp = new SendData<Integer>(token,user,locId);
		return webResource.type(MediaType.APPLICATION_JSON).post(ClientResponse	.class, tmp).getStatus();

	}

}
