package be.ac.ulb.infof307.g04.model.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import be.ac.ulb.infof307.g04.model.LikesStat;
import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.dao.LikesStatDao;

/**
 * The class that handle all the request related to likes or dislikes.
 */
@Path("/likes")
public class ServerLikesStatsHandler {

	public ServerLikesStatsHandler() {	}

	/**
	 * The handler of the insert request.
	 * @param userId The id of the user that liked the signaling.
	 * @param signalisationId The id of the signaling.
	 * @param statType the type of likes given by the user.
	 * @return A response code which indicates if the insertion is succeeded of not.
	 */
	@GET
	@Path("/insert/{userId}/{signalisationId}/{statType}")
	public Response insert(@PathParam("userId") int userId, @PathParam("signalisationId") int signalisationId, @PathParam("statType") int statType) {
		LikesStat likesStat = new LikesStat(userId, signalisationId, statType);
		int response = new LikesStatDao().insert(likesStat);
		return (response != -1 )? Response.ok().build() : Response.status(ResponseCode.SERVER_ERROR.getValue()).build();
	}
	/**
	 * The handler of the delete request.
	 * @param userId The id of the user that liked the signaling.
	 * @param signalisationId The id of the signaling.
	 * @param statType the type of likes given by the user.
	 * @return A response code which indicates if the insertion is succeeded of not.
	 */
	@DELETE
	@Path("/delete/{userId}/{signalisationId}/{statType}")
	public Response delete(@PathParam("userId") int userId, @PathParam("signalisationId") int signalisationId, @PathParam("statType") int statType) {
		LikesStat likesStat = new LikesStat(userId, signalisationId, statType);
		new LikesStatDao().delete(likesStat);
		return Response.ok().build();
	}

	/**
	 * Check the type of like done by the user on the signaling
	 * @param likesStat the like entry who is being checked
	 * @return the type of like done by the user
	 */
	@POST
	@Path("/userLikeType")
	@Consumes(MediaType.APPLICATION_JSON)
	public int userLikeType(LikesStat likesStat) {
		int response = new LikesStatDao().userLikeType(likesStat);
		return response;
	}

	/**
	 * Get the likes number on a signaling
	 * @param likesStat the like entry who is being checked
	 * @return number of likes on the signaling
	 */
	@POST
	@Path("/getLikes")
	@Consumes(MediaType.APPLICATION_JSON)
	public int getLikes(LikesStat likesStat) {
		int response = new LikesStatDao().readLikesNumbers(likesStat);
		return response;
	}

	/**
	 * Get the dislikes number on a signaling
	 * @param likesStat the like entry who is being checked
	 * @return number of dislikes on the signaling
	 */
	@POST
	@Path("/getDislikes")
	@Consumes(MediaType.APPLICATION_JSON)
	public int getDislikes(LikesStat likesStat) {
		int response = new LikesStatDao().readDislikesNumbers(likesStat);
		return response;
	}
}
