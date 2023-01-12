package be.ac.ulb.infof307.g04.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.lynden.gmapsfx.javascript.object.Marker;


/**
 * This class contains all the informations used to localize the pokemon on the map
 */
@XmlRootElement //Need for send a list of pokemon localisation
public class LocalisationPokemon {
	private int locId;
	private int pokemonId;
	private int userId;
	private double longitude;
	private double latitude;
	private String atTime;

	private Pokemon pokemon;
	private User user;

	private Marker marker;
	private boolean onServer;
	private int lifePoints;
	private int defPoints;
	private int attackPoints;

	public int getLifePoints() {
		return lifePoints;
	}

	public void setLifePoints(int lifePoints) {
		this.lifePoints = lifePoints;
	}

	public int getDefPoints() {
		return defPoints;
	}

	public void setDefPoints(int defPoints) {
		this.defPoints = defPoints;
	}

	public int getLocId() {
		return locId;
	}

	public void setLocId(int locId) {
		this.locId = locId;
	}

	public int getAttackPoints() {
		return attackPoints;
	}

	public void setAttackPoints(int attackPoints) {
		this.attackPoints = attackPoints;
	}

	public LocalisationPokemon() {

	}

	public LocalisationPokemon(int pokemonId, int userId, double longitude, double latitude, String atTime) {
		this(null, null, pokemonId, userId, longitude, latitude, atTime, null, false);
	}

	public LocalisationPokemon(int pokemonId, int userId, double longitude, double latitude, String atTime,int att,int def,int life) {
		this(null, null, pokemonId, userId, longitude, latitude, atTime, null, false,att,def,life);
	}

	public LocalisationPokemon(Pokemon pokemon, int userId, double longitude, double latitude, String atTime, Marker marker) {
		this(pokemon, null, pokemon.getId(), userId, longitude, latitude, atTime, marker, false);
	}

	public LocalisationPokemon(int locId, int pokemonId, int userId, double longitude, double latitude, String atTime) {
		this(null, null, pokemonId, userId, longitude, latitude, atTime, null, false);
	}

	public LocalisationPokemon(Pokemon pokemon,  User user, int pokemonId, int userId, double longitude, double latitude, String atTime, Marker marker, boolean onServer) {
		this.pokemonId = pokemonId;
		this.userId = userId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.atTime = atTime;

		this.pokemon = pokemon;
		this.user    = user;

		this.marker = marker;
		this.onServer = onServer;
	}
	public LocalisationPokemon(Pokemon pokemon,  User user, int pokemonId, int userId, double longitude, double latitude, String atTime,
			Marker marker, boolean onServer,int att,int def, int life) {
		this.pokemonId = pokemonId;
		this.userId = userId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.atTime = atTime;

		this.pokemon = pokemon;
		this.user    = user;

		this.marker = marker;
		this.onServer = onServer;
		this.attackPoints= att;
		this.defPoints = def;
		this.lifePoints = life;
	}

	public LocalisationPokemon(int locId, Pokemon pokemon,  User user, int pokemonId, int userId, double longitude, double latitude, String atTime, Marker marker, boolean onServer) {
		this.locId = locId;
		this.pokemonId = pokemonId;
		this.userId = userId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.atTime = atTime;

		this.pokemon = pokemon;
		this.user    = user;

		this.marker = marker;
		this.onServer = onServer;
	}
	public LocalisationPokemon(int locId, Pokemon pokemon,  User user, int pokemonId, int userId, double longitude, double latitude, String atTime,
			Marker marker, boolean onServer,int att,int def, int life) {
		this.locId = locId;
		this.pokemonId = pokemonId;
		this.userId = userId;
		this.longitude = longitude;
		this.latitude = latitude;
		this.atTime = atTime;

		this.pokemon = pokemon;
		this.user    = user;

		this.marker = marker;
		this.onServer = onServer;
		this.attackPoints= att;
		this.defPoints = def;
		this.lifePoints = life;
	}

	// 3 ways of instancing a pokemon on the map

	public int getPokemonId() {
		return pokemonId;
	}
	public void setPokemonId(int pokemonId) {
		this.pokemonId = pokemonId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public String getAtTime() {
		return atTime;
	}
	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}

	public Pokemon getPokemon() {
		return pokemon;
	}

	public void setPokemon(Pokemon pokemon) {
		this.pokemon = pokemon;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Marker getMarker() {
		return marker;
	}

	public void setMarker(Marker marker) {
		this.marker = marker;
	}

	public boolean isOnServer() {
		return onServer;
	}

	public void setOnServer(boolean onServer) {
		this.onServer = onServer;
	}
	// Allows to know if this pokemon is already saved on the server
}
