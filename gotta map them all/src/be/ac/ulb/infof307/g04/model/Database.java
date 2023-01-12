package be.ac.ulb.infof307.g04.model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.sql.Types;


/**
 * This class manages the connection and the communication between the client to the dataBase.
 * Implements Singleton design pattern.
 */
public class Database {
	
	
	// STATIC PART -----------------------
	private static Database db;
	
	public static Database getInstance() throws SQLException {
		if(db == null)
			db = new Database();
		
		return db;
	}
	

	// OBJECT PART -----------------------
	private Connection connection;

	/**
	 * @throws SQLException 
	 * 
	 */
	private Database() throws SQLException {
		this.connect();
	}
	
	/**
	 * 
	 */
	private void connect() throws SQLException {
	    this.connection = DriverManager.getConnection("jdbc:sqlite:./genieLogiciel.db");
	    System.out.println("Connection to SQLite has been established.");
    }
	
	/**
	 * 
	 * @param query
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SQLFeatureNotSupportedException
	 */
	private PreparedStatement prepare(String query, SQLParameter... args) 
			throws SQLException, SQLFeatureNotSupportedException 
	{
		int nbParams = query.length() - query.replace("?", "").length();
		assert nbParams == args.length : "You should give as much number of args as the numer of '?' in your query." ;
		
		PreparedStatement stmt = connection.prepareStatement(query, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		for(int i = 0; i < args.length; i++) {
			if(args[i].isNullValue())
				stmt.setNull(i, args[i].getType());
			else {
				switch(args[i].getType()) {
					// i+1 because JDBC starts at 1 for index parameter and not 0.
					case Types.INTEGER : stmt.setInt(i+1,    (int)    args[i].getValue()); break;
					case Types.DOUBLE  : stmt.setDouble(i+1, (double) args[i].getValue()); break;
					case Types.VARCHAR : stmt.setString(i+1, (String) args[i].getValue()); break;
					case Types.DATE    : stmt.setDate(i+1,   (Date)   args[i].getValue()); break;
				}
			}
		}
		return stmt;
	}
	
	/**
	 * 
	 * @param query
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 */
	public ResultSet executeQuery(String query, SQLParameter... args) 
		   throws SQLException, SQLTimeoutException 
	{
		return prepare(query, args).executeQuery();
	}
	
	/**
	 * 
	 * @param query
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 */
	public int executeUpdate(String query, SQLParameter... args) 
	       throws SQLException, SQLTimeoutException 
	{
		return prepare(query, args).executeUpdate();
	}
	
	/**
	 * Same as executeUpdate but returns the las inserted ID.
	 * @param query
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 */
	public long executeInsert(String query, SQLParameter... args) 
		       throws SQLException, SQLTimeoutException 
	{
		PreparedStatement stmt =  prepare(query, args);
		long insertedId = -1;
		if(stmt.executeUpdate() > 0) {
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
	            if (generatedKeys.next()) 
	                insertedId = generatedKeys.getLong(1);
	            else 
	                throw new SQLException("Insertion failed.");
	        }
		}
		return insertedId;
	}
	
	/**
	 * 
	 * @param query
	 * @param args
	 * @return
	 * @throws SQLException
	 * @throws SQLTimeoutException
	 */
	public boolean execute(String query, SQLParameter... args) 
		   throws SQLException, SQLTimeoutException 
	{
		return prepare(query, args).execute();
	}
	
}
