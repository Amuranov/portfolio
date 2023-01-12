package be.ac.ulb.infof307.g04.model.server;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import be.ac.ulb.infof307.g04.model.LocalisationPokemon;
import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.SendData;
import be.ac.ulb.infof307.g04.model.dao.LocalisationPokemonDAO;
import javassist.bytecode.stackmap.TypeData.ClassName;

/**
 * The class that handle all the request related to pokemons.
 */
@Path("/pokemons")
public class ServerPokemonHandler {
	private static final Logger LOGGER = Logger.getLogger( ClassName.class.getName() );
	public ServerPokemonHandler() {

	}

	/**
	 * The handler of the insert request.
	 * @param localisationPokemon The {@link LocalisationPokemon} that we must insert in the database.
	 * @return A response code which indicates if the insertion is succeeded of not.
	 */
	@POST
	@Path("/insert")
	@Consumes(MediaType.APPLICATION_JSON)
	public int insert(SendData<LocalisationPokemon> data) {
		int insertedId = -1;
		LOGGER.log(Level.INFO,"User:"+data.getUser()+" token:"+data.getToken());
		if(Server.getTokenManager().checkToken(data.getUser(), data.getToken())){
			try {
				LOGGER.log(Level.INFO,"Token is correct");
				insertedId = new LocalisationPokemonDAO().insert(data.getData());
			} catch (SQLException e) {
				// We do nothing more here because we already have the value of the variable "insertedLocId" as indicator.
				// The client will know that something were wrong by knowing the value of the response code.
				insertedId = -1;
				LOGGER.log(Level.WARNING,"SQL EXCEPTION");
			}
		}else{
			insertedId = -2;
			LOGGER.log(Level.WARNING,"Token is incorrect");
		}
		return insertedId;
	}

	@POST
	@Path("/update")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response update(SendData<LocalisationPokemon> data) {
		int rows = - 2;
		if(Server.getTokenManager().checkToken(data.getUser(), data.getToken())){
			LOGGER.log(Level.INFO,"Token is correct");
			rows = -1;
			try {
				rows = new LocalisationPokemonDAO().update(data.getData());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		else{
			LOGGER.log(Level.WARNING,"Token is incorrect");
		}
		return (rows > 0 )? Response.ok().build() : Response.status(ResponseCode.BAD_REQUEST.getValue()).build();

	}

	@GET
	@Path("/readAll")
	@Produces(MediaType.APPLICATION_XML)
	public Response readAll() {
		List<LocalisationPokemon> pokemonsList = new LocalisationPokemonDAO().readAll();
		GenericEntity<List<LocalisationPokemon>> entity = new GenericEntity<List<LocalisationPokemon>>(pokemonsList){};
		return Response.ok(entity).build();
	}
	@POST
	@Path("/delete")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response delete(SendData<Integer> data){
		LocalisationPokemonDAO loc = new LocalisationPokemonDAO();
		boolean del = loc.delete(data.getData());
		return (del)? Response.ok().build() : Response.status(ResponseCode.BAD_REQUEST.getValue()).build();

	}
}
