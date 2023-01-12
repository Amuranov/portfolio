package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import be.ac.ulb.infof307.g04.model.Database;
import be.ac.ulb.infof307.g04.model.Pokemon;
import be.ac.ulb.infof307.g04.model.SQLParameter;


/**
 *  Allow to access the whole data from a pokemon from the Database
 */
public class PokemonDAO extends DAO<Pokemon> {
	
	/**
	 * @return all the pokemon saved in the database
	 */
	public List<Pokemon> readAll() {
		List<Pokemon> pokemons = new ArrayList<Pokemon>();
		String query = "SELECT pokemons.id AS idpokemon, name, type_1, type_2, t1.descr AS t1_descr, "
							+ "t2.descr AS t2_descr, pv, atk, def, atk_spe, def_spe, speed, img, img_grand "
							+ "FROM pokemons "
							+ "JOIN types t1 ON t1.id = pokemons.type_1 "
							+ "LEFT JOIN types t2 ON t2.id = pokemons.type_2; ";
		
		try(ResultSet result = db.executeQuery(query)) {
			while(result.next()) {
				pokemons.add(new Pokemon(
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
							result.getString("t1_descr"),
							result.getString("t2_descr")
						));
			}	
		} catch (SQLException exception) {
			System.err.println("Unexpected error in PokemonDAO readAll : " + exception);
		}
		return pokemons;
	}
	
	/**
	 * Find a pokemon from the database by his name
	 * @param namePokemon
	 * @return the pokemon
	 */
	public Pokemon findByName(String namePokemon) {
		Pokemon pokemon = null;
		
		try(ResultSet result = db.executeQuery("SELECT * FROM pokemons WHERE name = ?", new SQLParameter(namePokemon))) {			
			if (result.next()) {
				pokemon = new Pokemon(
							result.getInt("id"), 
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
							null, 
							null
						);
			}	
		} catch (SQLException exception) {
			System.err.println("Unexpected error in PokemonDAO findByName : " + exception);
		}
		return pokemon;
	}
	
	/**
	 * find a pokemon by his Id
	 * @param id 
	 * @return the pokemon corresponding to the Id
	 */
	public Pokemon findById(int id) {
		Pokemon pokemon = null;
		try{
			try(ResultSet result = Database.getInstance().executeQuery("SELECT * FROM pokemons WHERE id = ?", new SQLParameter(id)))
			{
				if (result.next()) 
				{
					pokemon = new Pokemon(
								result.getInt("id"), 
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
								null,
								null
					);
				}	
			}
		} catch (SQLException exception) {
			System.err.println("Unexpected error in PokemonDAO findById : " + exception);
		}
		return pokemon;
	}
	
	/**
	 *  Find pokemon by a given type
	 * @param a pokemon type
	 * @return a pokemon from his type
	 */
	public List<Pokemon> findByType(String type) {
		String normalizedType = type.replaceAll("[éèêë]", "e").toLowerCase();
		List<Pokemon> pokemons = new ArrayList<Pokemon>();
		String query = "SELECT pokemons.id AS idpokemon, name, type_1, type_2, t1.descr AS t1_descr, "
							+ "t2.descr AS t2_descr, pv, atk, def, atk_spe, def_spe, "
							+ "speed, img, img_grand FROM pokemons "
						+ "JOIN types t1 ON t1.id = pokemons.type_1 "
						+ "LEFT JOIN types t2 ON t2.id = pokemons.type_2 "
						+ "WHERE LOWER(t1.descr) = ? "
						+ "OR LOWER(t2.descr) = ?";
		
		try(ResultSet result = db.executeQuery(query, new SQLParameter(normalizedType), new SQLParameter(normalizedType))){			
			while(result.next()) {
				pokemons.add(new Pokemon(
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
							result.getString("t1_descr"),
							result.getString("t2_descr")
						));
			}	
		} catch (SQLException exception) {
			System.err.println("Unexpected error in PokemonDAO findByType : " + exception);
		}
		return pokemons;
	}
}
