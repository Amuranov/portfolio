package be.ac.ulb.infof307.g04.model;
/**
 * The response codes that our HTTP responses can produce.
 */
public enum ResponseCode {
	OK(200), 
	BAD_REQUEST(400), 
	UNAUTHORIZED(401),
	NOT_FOUND(404), 
	SERVER_ERROR(500);

	private int value;

	ResponseCode(int val) {
		value = val;
	}

	public int getValue() {
		return value;
	}
	
}
