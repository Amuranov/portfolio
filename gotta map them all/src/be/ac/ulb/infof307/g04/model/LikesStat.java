package be.ac.ulb.infof307.g04.model;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import be.ac.ulb.infof307.g04.model.dao.PokemonDAO;
import be.ac.ulb.infof307.g04.model.dao.TypeDAO;

/**
 * This class is used to create a research.
 * It's used when we want to save some research we did before.
 */
public class LikesStat implements Serializable {

	private int id;
	private Integer userID;
	private Integer signalisationId;
	private Integer statType;

	// we need a default simple constructor for Jersey to run properly.
	public LikesStat() {

	}

	/**
	 * used when we want to insert a row in the database.
	 * @param userID ID of the user
	 * @param signalisationId ID of the signaling
	 * @param statType type of like
	 */
	public LikesStat(Integer userID, Integer signalisationId, Integer statType) {
		this.userID = userID;
		this.signalisationId = signalisationId;
		this.statType = statType;
	}

	public Integer getUserID() {
		return userID;
	}

	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	public Integer getSignalingId() {
		return signalisationId;
	}

	public void setSignalingId(Integer signalisationId) {
		this.signalisationId = signalisationId;
	}

	public Integer getStatType() {
		return statType;
	}

	public void setStatType(Integer statType) {
		this.statType = statType;
	}

}
