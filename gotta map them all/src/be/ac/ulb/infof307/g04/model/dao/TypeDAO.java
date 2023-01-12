package be.ac.ulb.infof307.g04.model.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import be.ac.ulb.infof307.g04.model.SQLParameter;
import be.ac.ulb.infof307.g04.model.Type;

/**
 * Allows access to all type Data from the database
 */
public class TypeDAO extends DAO<Type> {
	
	/**
	 * Search a type by his name
	 * @param nameType
	 * @return Type
	 */
	public Type findByName(String nameType) {
		Type type = null;
		try(ResultSet r = db.executeQuery("SELECT * FROM types WHERE descr = ?", new SQLParameter(nameType))) {
			type = r.next() ? new Type(r.getInt("id"), r.getString("descr")) : null;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in TypeDAO findByName : " + exception);
		}
		return type;
	}
	
	/**
	 * Search a type by his ID
	 * @param type ID
	 * @return type
	 */
	public Type findById(int id) {
		Type type = null;
		try(ResultSet r = db.executeQuery("SELECT * FROM types WHERE id = ?",  new SQLParameter(id))) {
			type = r.next() ? new Type(r.getInt("id"), r.getString("descr")) : null;
		} catch (SQLException exception) {
			System.err.println("Unexpected error in TypeDAO findById : " + exception);
		}
		return type;
	}
	
	/**
	 * Read all the types from the database
	 * @return all the types
	 */
	public ArrayList<String> readAll() {
		ArrayList<String> types = new ArrayList<String>();
		try(ResultSet r = db.executeQuery("SELECT * FROM types")) {
			while(r.next()) 
				types.add(r.getString("descr"));
		} catch (SQLException exception) {
			System.err.println("Unexpected error in TypeDAO readAll : " + exception);
		}
		return types;
	}

}
