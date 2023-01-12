package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import be.ac.ulb.infof307.g04.model.LikesStat;
import be.ac.ulb.infof307.g04.model.SQLParameter;

/**
 * Allows access to all informations concerning SearchFavorite from the database
 */
public class LikesStatDao extends DAO<LikesStatDao> {

	/**
	 * Insert a new likeStat
	 * @param likesStat the like entry to be insert on the database
	 * @return either :
	 * 			 (1) the row count for SQL Data Manipulation Language (DML) statements or
	 * 			 (2) 0 for SQL statements that return nothing
	 * 			 (3) -1 if something went wrong
	 */
	public int insert(LikesStat likesStat) {
		try{
			return db.executeUpdate(
				"INSERT INTO LikesStat(user_id, signalisation_id, stat_type) VALUES(?, ?, ?)",
				new SQLParameter(likesStat.getUserID()),
				new SQLParameter(likesStat.getSignalingId()),
				new SQLParameter(likesStat.getStatType())
			);
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LikeStatDao insert : " + exception);
			return -1;
		}
	}

	/**
	 * Delete a likeStat entry from the Database
	 * @param likeStat the like entry to be deleted from the database
	 * @return either :
	 * 			 (1) the row count for SQL Data Manipulation Language (DML) statements or
	 * 			 (2) 0 for SQL statements that return nothing
	 * 			 (3) -1 If something went wrong
	 */
	public int delete(LikesStat likesStat) {
		try {
			return db.executeUpdate(
				"DELETE FROM LikesStat WHERE user_id = ? AND signalisation_id = ? AND stat_type = ?",
				new SQLParameter(likesStat.getUserID()),
				new SQLParameter(likesStat.getSignalingId()),
				new SQLParameter(likesStat.getStatType())
			);
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LikeStatDao delete : " + exception);
			return -1;
		}
	}

	/**
	 * Read the likes numbers done on a signaling
	 * @param likesStat LikeStat object containing the id of the signaling to be checked
	 * @return number of likes on the signaling
	 */
	public int readLikesNumbers(LikesStat likesStat) {
		try {
			String query = "SELECT COUNT(*) AS total FROM LikesStat WHERE signalisation_id = ? AND stat_type = 1";
			ResultSet rs = db.executeQuery(query, new SQLParameter(likesStat.getSignalingId()));
			if(rs.next()){
				return rs.getInt("total");
			}else{
				return 0;
			}
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LikeStatDao delete : " + exception);
			return -1;
		}
	}

	/**
	 * Read the dislikes numbers done on a signaling
	 * @param likesStat LikeStat object containing the id of the signaling to be checked
	 * @return number of dislikes on the signaling
	 */
	public int readDislikesNumbers(LikesStat likesStat) {
		try {
			String query = "SELECT COUNT(*) AS total FROM LikesStat WHERE signalisation_id = ? AND stat_type = -1";
			ResultSet rs = db.executeQuery(query, new SQLParameter(likesStat.getSignalingId()));
			if(rs.next()){
				return rs.getInt("total");
			}else{
				return 0;
			}
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LikeStatDao delete : " + exception);
			return -1;
		}
	}

	/**
	 * Read likes numbers done on a signaling
	 * @param likesStat LikeStat object containing the id of the signaling and the userId
	 * @return type of likes done by the given user on the signaling
	 */
	public int userLikeType(LikesStat likesStat) {
		try {
			String query = "SELECT * FROM LikesStat WHERE user_id = ? AND signalisation_id = ?";
			ResultSet rs = db.executeQuery(query, new SQLParameter(likesStat.getUserID()),
					new SQLParameter(likesStat.getSignalingId()));
			if(rs.next()){
				return rs.getInt("stat_type");
			}else{
				return 0;
			}
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LikeStatDao delete : " + exception);
			return -1;
		}
	}
}
