package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulb.infof307.g04.model.LocalisationPokemon;
import be.ac.ulb.infof307.g04.model.Pokemon;
import be.ac.ulb.infof307.g04.model.SQLParameter;
import be.ac.ulb.infof307.g04.model.User;


/**
 *  Allows to access all the informations concerning {@link LocalisationPokemon} from the database
 */
public class LocalisationPokemonDAO extends DAO<LocalisationPokemon> {

	/**
	 * Insert a new paramater in the DB
	 * @param localisationPokemonDAO param which will be inserted in the database
	 * @return Number of rows affected. (Or -1 if the LocalisationPokemon is already in the database)
	 * @throws SQLException
	 */
	public int insert(LocalisationPokemon localisationPokemonDAO) throws SQLException {
		if (alreadyInDB(localisationPokemonDAO))
			return -1;

		String query = "INSERT INTO LocalisationPokemons(pokemon_id, user_id, longitude, "
				+ "latitude, at_time, life, attack, defense) VALUES (?, ?, ?, ?, ?, ? , ? , ? )";
		return (int) db.executeInsert(query,
				new SQLParameter(localisationPokemonDAO.getPokemonId()),
				new SQLParameter(localisationPokemonDAO.getUserId()),
				new SQLParameter(localisationPokemonDAO.getLongitude()),
				new SQLParameter(localisationPokemonDAO.getLatitude()),
				new SQLParameter(localisationPokemonDAO.getAtTime()),
				new SQLParameter(localisationPokemonDAO.getLifePoints()),
				new SQLParameter(localisationPokemonDAO.getAttackPoints()),
				new SQLParameter(localisationPokemonDAO.getDefPoints())
				);
	}
	public int update(LocalisationPokemon localisationPokemonDAO) throws SQLException {
		String query = "UPDATE LocalisationPokemons SET attack = ?, defense = ? , life = ? "
				+ "WHERE pokemon_id = ? AND user_id = ? AND longitude = ? AND latitude = ? AND "
				+ "at_time = ?";
		return (int) db.executeUpdate(query,
				new SQLParameter(localisationPokemonDAO.getAttackPoints()),
				new SQLParameter(localisationPokemonDAO.getDefPoints()),
				new SQLParameter(localisationPokemonDAO.getLifePoints()),
				new SQLParameter(localisationPokemonDAO.getPokemonId()),
				new SQLParameter(localisationPokemonDAO.getUserId()),
				new SQLParameter(localisationPokemonDAO.getLongitude()),
				new SQLParameter(localisationPokemonDAO.getLatitude()),
				new SQLParameter(localisationPokemonDAO.getAtTime())
				);
	}

	/**
	 * read all the pokemon that have been put on the map
	 * @return a list containing all the pokemon that are on the map
	 */
	public List<LocalisationPokemon> readAll() {
		List<LocalisationPokemon> localisationPokemon = new ArrayList<LocalisationPokemon>();
		String query = "SELECT pokemons.id AS idpokemon, LocalisationPokemons.id AS locId, name, type_1, type_2, pv, atk, "
				+ "def, atk_spe, def_spe, speed, img, img_grand,  "
				+ "user.id AS iduser, username, pwd, mail, "
				+ "t1.descr AS type1_descr, t2.descr AS type2_descr, "
				+ "pokemon_id, user_id, longitude, latitude, at_time, life, attack, defense "
				+ "FROM LocalisationPokemons "
				+ "JOIN USER     ON USER.id     = LocalisationPokemons.user_id "
				+ "JOIN POKEMONS ON POKEMONS.id = LocalisationPokemons.pokemon_id "
				+ "JOIN TYPES t1 ON POKEMONS.type_1 = t1.id "
				+ "LEFT JOIN TYPES t2 ON POKEMONS.type_2 = t2.id";
		try(ResultSet result = db.executeQuery(query)) {
			while(result.next())
				localisationPokemon.add(new LocalisationPokemon(
						result.getInt("locId"),
						new Pokemon(
								result.getInt("idpokemon"),
								result.getString("name"),
								result.getInt("type_1"),
								result.getInt("type_2"),
								result.getInt("pv"),
								result.getInt("atk"),
								result.getInt("def"),
								result.getInt("atk_spe"),
								result.getInt("def_spe"),
								result.getInt("speed"),
								result.getString("img"),
								result.getString("img_grand"),
								result.getString("type1_descr"),
								result.getString("type2_descr")
								),
						new User(
								result.getInt("iduser"),
								result.getString("username"),
								result.getString("pwd"),
								result.getString("mail")
								),
						result.getInt("pokemon_id"),
						result.getInt("user_id"),
						result.getDouble("longitude"),
						result.getDouble("latitude"),
						result.getString("at_time"),
						null,
						true,
						result.getInt("attack"),
						result.getInt("defense"),
						result.getInt("life")
						));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon readAll : " + exception);
		}
		return localisationPokemon;
	}

	/**
	 * find all the pokemon inserted by a user
	 * @param id from a user
	 * @return all the pokemon inserted by a chosen user
	 */
	public List<LocalisationPokemon> findByUserId(int id) {
		List<LocalisationPokemon> localisationPokemon = new ArrayList<LocalisationPokemon>();
		String query = "SELECT * FROM LocalisationPokemons WHERE user_id = ?";
		try(ResultSet result = db.executeQuery(query, new SQLParameter(id))) {
			while(result.next())
				localisationPokemon.add(new LocalisationPokemon(
						result.getInt("id"),
						result.getInt("pokemon_id"),
						result.getInt("user_id"),
						result.getDouble("longitude"),
						result.getDouble("latitude"),
						result.getString("at_time")
						));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon findByType : " + exception);
		}
		return localisationPokemon;
	}

	/**
	 *  Read all names from inserted pokemon
	 * @return a list of strings containing the name of all the pokemon inserted on the map
	 */
	public List<String> readAllPokemonNames() {
		List<String> pokemonNames = new ArrayList<String>();
		String query = "SELECT DISTINCT name " +
				"FROM LocalisationPokemons " +
				"JOIN pokemons ON pokemons.id = LocalisationPokemons.pokemon_id " +
				"ORDER BY name";
		try(ResultSet result = db.executeQuery(query)) {
			while(result.next())
				pokemonNames.add(result.getString("name"));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon readAllPokemonNames : " + exception);
		}
		return pokemonNames;
	}

	/**
	 *  read all pokemon type from the map
	 * @return a list containing the type from all the pokemon inserted on the map
	 */
	public List<String> readAllPokemonTypes() {
		List<String> pokemonNames = new ArrayList<String>();
		String query = "SELECT DISTINCT descr "
				+ "FROM LocalisationPokemons "
				+ "JOIN pokemons ON pokemons.id = LocalisationPokemons.pokemon_id "
				+ "JOIN types ON (pokemons.type_1 = types.id OR pokemons.type_2 = types.id) "
				+ "ORDER BY descr";
		try(ResultSet result = db.executeQuery(query)) {
			while(result.next())
				pokemonNames.add(result.getString("descr"));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon readAllPokemonTypes : " + exception);
		}
		return pokemonNames;
	}

	/**
	 * Checks if a {@link LocalisationPokemon} is already on the database (conflict management)
	 * @param localisationPokemon
	 * @return true if it is the case. False otherwise.
	 */
	private boolean alreadyInDB(LocalisationPokemon localisationPokemon) {
		double twentyMeters = 0.0002;
		boolean response = false;
		String query = "select * from localisationpokemons where pokemon_id = ? " +
				" and abs(latitude - ?) <= "  + twentyMeters +
				" and abs(longitude - ?) <= " + twentyMeters +
				" and at_time = ?";
		try(ResultSet result = db.executeQuery(query,
				new SQLParameter(localisationPokemon.getPokemonId()),
				new SQLParameter(localisationPokemon.getLatitude()),
				new SQLParameter(localisationPokemon.getLongitude()),
				new SQLParameter(localisationPokemon.getAtTime())))
		{
			return result.next();
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon alreadyInDB : " + exception);
		}
		return response;
	}

	/**
	 * Deletes a {@link LocalisationPokemon} based on its id.
	 * @param id id of the {@link LocalisationPokemon} entity.
	 * @return true if the deletion succeeded, false otherwise.
	 */
	public boolean delete(int id) {
		String query = "DELETE FROM LocalisationPokemons WHERE id = ?";
		try {
			return db.executeUpdate(query, new SQLParameter(id)) > 0;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in LocalisationPokemon delete : " + exception);
		}
		return false;
	}
}