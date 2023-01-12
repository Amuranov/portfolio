package be.ac.ulb.infof307.g04.model.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.SearchFavorite;
import be.ac.ulb.infof307.g04.model.dao.SearchFavoriteDAO;

/**
 * The class that handle all the request related to favorites.
 */
@Path("favorites")
public class ServerSearchFavoriteHandler {

	public ServerSearchFavoriteHandler() { 	}

	/**
	 * The handler of the insert request.
	 * @param favorite The {@link SearchFavorite} that we must insert in the database.
	 * @return A response code which indicates if the insertion is succeeded of not.
	 */
	@POST
	@Path("insert")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response insert(SearchFavorite favorite) {
		int response = new SearchFavoriteDAO().insert(favorite);
		return (response != -1 )? Response.ok().build() : Response.status(ResponseCode.SERVER_ERROR.getValue()).build();
	}
}
