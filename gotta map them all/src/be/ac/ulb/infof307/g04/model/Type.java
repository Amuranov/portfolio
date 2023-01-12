package be.ac.ulb.infof307.g04.model;

import java.io.Serializable;

/**
 * Contains all the informations concerning a pokemon type
 */
public class Type implements Serializable {
	
	private int id;
	private String description;
	
	public Type(int id, String description) {
		this.id = id;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}	
}
