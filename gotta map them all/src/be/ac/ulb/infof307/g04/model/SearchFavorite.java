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
public class SearchFavorite implements Serializable {
	
	private int id;
	private Integer userID;
	private Integer typeID;
	private Integer pokemonID;
	
	private Date    createdAt;
	private Type    typePokemon;
	private Pokemon pokemon;
	
	/**
	 * @return the typePokemon
	 */
	public Type getTypePokemon() {
		return typePokemon;
	}

	/**
	 * @param typePokemon the typePokemon to set
	 */
	public void setTypePokemon(Type typePokemon) {
		this.typePokemon = typePokemon;
	}

	// we need a default simple constructor for Jersey to run properly.
	public SearchFavorite() {
		
	}
	
	/**
	 * This constructor is used when we DO NOT need to retrieve the name and the type as String value.
	 * It is used when we want to insert a row in the database.
	 * @param userID ID of the user that did the research (null if visitor... But we do not store the searche for a visitor anyway)
	 * @param typeID ID of the pokemon's type.
	 * @param pokemonID ID of the pokemon
	 */
	public SearchFavorite(Integer userID, Integer typeID, Integer pokemonID) {
		this.userID = userID;
		this.typeID = typeID;
		this.pokemonID = pokemonID;
		this.createdAt = Calendar.getInstance().getTime();
	}
	
	/**
	 * Creates a research based on a type and a pokemon. 
	 * @param userID Author of the search
	 * @param typePokemon The pokemon's type
	 * @param namePokemon The pokemon's name
	 */
	public SearchFavorite(Integer userID, String typePokemon, String namePokemon) {
		this.userID = userID;

		this.typePokemon = new TypeDAO().findByName(typePokemon);
		this.typeID = (this.typePokemon != null) ? this.typePokemon.getId() : null;
		
		this.pokemon = new PokemonDAO().findByName(namePokemon);
		this.pokemonID = (this.pokemon != null) ? this.pokemon.getId() : null;
		
		this.createdAt = Calendar.getInstance().getTime();
	}

	/**
	 * @return the userID
	 */
	public Integer getUserID() {
		return userID;
	}

	/**
	 * @param userID the userID to set
	 */
	public void setUserID(Integer userID) {
		this.userID = userID;
	}

	/**
	 * @return the typeID
	 */
	public Integer getTypeID() {
		return typeID;
	}

	/**
	 * @param typeID the typeID to set
	 */
	public void setTypeID(Integer typeID) {
		this.typeID = typeID;
	}

	/**
	 * @return the pokemonID
	 */
	public Integer getPokemonID() {
		return pokemonID;
	}

	/**
	 * @param pokemonID the pokemonID to set
	 */
	public void setPokemonID(Integer pokemonID) {
		this.pokemonID = pokemonID;
	}

	public Pokemon getPokemon() {
		return pokemon;
	}

	public void setPokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
	}
	
	/**
	 * @return the createdAt
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	
	/**
	 * @param createdAt the createdAt to set
	 */
	public void setCreatedAt(String createdAt) {
		try {
			this.createdAt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(createdAt);
		} catch (ParseException e) {
			this.createdAt = null;
		}
	}
}
