package be.ac.ulb.infof307.g04.model;

import java.sql.Types;

/**
 * This class is used to pass nullable parameters to the queries.
 *  Why did I create this class ? 
 * 	  ----------------------------
 *     Well, this is because I wanted to create a kind of generic method which can handle null values as parameter.
 * 	   In SQL, we can send nulls in our SELECT, UPDATE, etc.
 * 	   And I did not want to check for each given parameters if it's null.
 * 	   Furthermore, how could I send a null value if I don't know its type since 
 *     it's null and the setNull(index, type) method need the type of the null ?
 *     
 *     I then created this class in order to handle the type of nulls.
 *
 */
public class SQLParameter {
	private Object value;
	private int    type;
	
	public SQLParameter(Object value) {
		this.value = value;
		findAndSetType(value);
	}

	private void findAndSetType(Object value) {
		switch(value.getClass().getSimpleName())
		{
			case "Integer" : this.type = Types.INTEGER; break;
			case "String"  : this.type = Types.VARCHAR; break;
			case "Double"  : this.type = Types.DOUBLE;  break;
			case "Date"    : this.type = Types.DATE;    break;
			default: 
				throw new IllegalArgumentException("Invalid type of argument. "
												 + "Accepted parameters : String, Double, Integer, Date"); 
		}
	}
	
	public Object getValue() {
		return value;
	}
	
	public void setValue(Object value) {
		this.value = value;
		findAndSetType(value);
	}
	
	public int getType() {
		return type;
	}
	
	public boolean isNullValue() {
		return this.value == null;
	}
}