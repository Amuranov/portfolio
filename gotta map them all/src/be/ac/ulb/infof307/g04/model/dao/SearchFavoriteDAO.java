package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import be.ac.ulb.infof307.g04.model.SQLParameter;
import be.ac.ulb.infof307.g04.model.SearchFavorite;

/**
 * Allows access to all informations concerning SearchFavorite from the database
 */
public class SearchFavoriteDAO extends DAO<SearchFavorite> {
	
	/**
	 * Insert a new Favorite 
	 * @param searchFavorite
	 * @return either :
	 * 			 (1) the row count for SQL Data Manipulation Language (DML) statements or 
	 * 			 (2) 0 for SQL statements that return nothing
	 * 			 (3) -1 if something went wrong
	 */
	public int insert(SearchFavorite searchFavorite) {
		try{
			return db.executeUpdate(
				"INSERT INTO SearchFavorites(user_id, type_id, pokemon_id) VALUES(?, ?, ?)", 
				new SQLParameter(searchFavorite.getUserID()),
				new SQLParameter(searchFavorite.getTypeID()),
				new SQLParameter(searchFavorite.getPokemonID())
			);
		} catch (SQLException exception) {
			System.err.println("Unexpected error in SearchFavoriteDAO insert : " + exception);
			return -1;
		}
	}
	
	/**
	 * Delete a SearchFavorite from the Database
	 * @param searchFavorite
	 * @return either :
	 * 			 (1) the row count for SQL Data Manipulation Language (DML) statements or 
	 * 			 (2) 0 for SQL statements that return nothing
	 * 			 (3) -1 If something went wrong
	 */
	public int delete(SearchFavorite searchFavorite) {
		try {
			return db.executeUpdate(
				"DELETE FROM SearchFavorites WHERE user_id = ? AND type_id = ? AND pokemon_id = ?", 
				new SQLParameter(searchFavorite.getUserID()),
				new SQLParameter(searchFavorite.getTypeID()),
				new SQLParameter(searchFavorite.getPokemonID())
			);
		} catch (SQLException exception) {
			System.err.println("Unexpected error in SearchFavoriteDAO delete : " + exception);
			return -1;
		}
	}
	
	/**
	 * Search all the favorites from a user name
	 * @param username
	 * @return the favorites from this user
	 */
	public ArrayList<SearchFavorite> findByUsername(String username) {
		ArrayList<SearchFavorite> response = new ArrayList<SearchFavorite>();
		String query = "SELECT user_id, type_id, pokemon_id, created_at " + 
						   "FROM searchfavorites " + 
					   	   "JOIN user on user.id = user_id " + 
						   "WHERE username = ?";
		SearchFavorite sf;
		try(ResultSet r = db.executeQuery(query, new SQLParameter(username))) {
			while (r.next()) {
				sf = new SearchFavorite(r.getInt("user_id"), r.getInt("type_id"), r.getInt("pokemon_id"));
				sf.setCreatedAt(r.getString("created_at"));
				sf.setPokemon(new PokemonDAO().findById(r.getInt("pokemon_id")));
				sf.setTypePokemon(new TypeDAO().findById(r.getInt("type_id")));
				response.add(sf);
			}
		} catch (SQLException exception)
			{System.err.println("Unexpected error in SearchFavoriteDAO findByUser: "+exception);
		}
		return response;
	}
	
	/**
	 * Search all the favorites from a userID
	 * @param id
	 * @return his favorites
	 */
	public ArrayList<SearchFavorite> findByUserId(int id){
		ArrayList<SearchFavorite> response = new ArrayList<SearchFavorite>();
		SearchFavorite sf;
		String query = "SELECT user_id, type_id, pokemon_id, created_at " + 
						   "FROM searchfavorites " + 
						   "WHERE user_id = ?";
		try(ResultSet r = db.executeQuery(query, new SQLParameter(id))) {
			while (r.next()) {
				sf = new SearchFavorite(r.getInt("user_id"), r.getInt("type_id"), r.getInt("pokemon_id"));
				sf.setCreatedAt(r.getString("created_at"));
				sf.setPokemon(new PokemonDAO().findById(r.getInt("pokemon_id")));
				sf.setTypePokemon(new TypeDAO().findById(r.getInt("type_id")));
				// SQL transforms null Integers to 0. So, to avoid errors
				// in our application, we change zeros to null.
				sf.setPokemonID(r.getInt("pokemon_id") == 0? null : r.getInt("pokemon_id"));
				sf.setTypeID(r.getInt("type_id") == 0? null : r.getInt("type_id"));
				response.add(sf);
			}
		} catch (SQLException exception) {
			System.err.println("Unexpected error in SearchFavoriteDAO findByUserId : " + exception);
		}
		return response;
	}
}
