package be.ac.ulb.infof307.g04.model.dao;

import java.sql.SQLException;

import be.ac.ulb.infof307.g04.model.Database;

public abstract class DAO<T> {
	protected Database db;
	
	/**
	 * Constructs a new generic DAO.
	 */
	public DAO() {
		try {
			this.db = Database.getInstance();
		} catch (SQLException e) {
			System.err.println("TypeDAO => Cannot connect to the database. Error : " + e);
		}
	}
}
