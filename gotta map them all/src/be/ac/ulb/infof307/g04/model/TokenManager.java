package be.ac.ulb.infof307.g04.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * Manages the token System. Associates a user with a token.
 *
 */
public class TokenManager{
	/**
	 * Map that associates a key to a user. The key is the username and the value a token string.
	 */
	Map<String, Token> map = new HashMap<String,Token>();

	/**
	 * Assigns a token to a user.
	 * @param username
	 * @return the newly created token
	 * @throws IllegalArgumentException if the user already has a token
	 */
	public Token addToken(String username){
		Token token = null;
		if(!map.containsKey(username)){
			Date expire = new Date();
			expire.setTime(System.currentTimeMillis()+(60*60*1000));
			token = new Token(generateAccessToken(username),expire);
			map.put(username,token);
		}
		return token;
	}
	/**
	 * generates an Access token
	 * @param username
	 * @return the access token
	 */
	public String generateAccessToken(String username){
		Random random = new Random(0);
		int x =  1 + random.nextInt((Integer.MAX_VALUE - 1) + 1);
		Date creationDate = new Date();
		String sequence = UUID.nameUUIDFromBytes((username+creationDate+x).getBytes()).toString();
		sequence = sequence.replace("-", "");
		return sequence;
	}
	/**
	 * generates a request token
	 * @param username
	 * @return the request token
	 */
	public String generateRefreshToken(String username){
		Random random = new Random(0);
		int x =  1 + random.nextInt((Integer.MAX_VALUE - 1) + 1);
		Date creationDate = new Date();
		String str = UUID.randomUUID().toString();
		String sequence = UUID.nameUUIDFromBytes((str+username+creationDate+x).getBytes()).toString();
		sequence = sequence.replace("-", "");
		return sequence;
	}
	/**
	 * updates the token given in parameters by replacing the access and refresh token, and the expiration date
	 * @param token
	 * @param username
	 * @return
	 */
	private Token updateToken(Token token,String username){
		token.setAccess(generateAccessToken(username));
		Date expire=new Date();
		expire.setTime(System.currentTimeMillis()+(60*60*1000));
		token.setExpires(expire);
		map.put(username,token);
		return token;
	}
	/**
	 * Checks if the user has a token
	 * @param username
	 * @return True if the user has a token, false otherwise
	 */
	public boolean userExists(String username){
		return map.containsKey(username);
	}
	/**
	 * Checks if the token associated with the user has expired
	 * @param username
	 * @return True if the token has expired, false otherwise
	 */
	public boolean hasExpired(String username){
		Token token = (Token) map.get(username);
		Date now = new Date();
		return now.compareTo(token.getExpires()) > 0;
	}
	/**
	 * Checks if the token and user given as parameters are valid
	 * @param username
	 * @param userToken
	 * @return True if the user effectively has the token given as a parameter, false otherwise
	 */
	public boolean checkToken(String username, String userToken){
		Token token = (Token) map.get(username);
		if (token == null){
			return false;
		}
		return token.isAccessEqual(userToken);
	}
	/**
	 * Deletes a token associated with the user given as a parameter
	 * @param username
	 * @return True if the token has been deleted successfully, false otherwise
	 */
	public boolean deleteToken(String username){
		return userExists(username) && map.remove(username) != null;
	}
}


