package be.ac.ulb.infof307.g04.model.server;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.message.filtering.SelectableEntityFilteringFeature;
import org.glassfish.jersey.server.ResourceConfig;

import be.ac.ulb.infof307.g04.model.TokenManager;

/**
 * The server which handles all the requests sent by the client.
 */
public class Server {

	private static final String URL  = "http://localhost/server";
	private static final int    PORT = 8000;
	private static TokenManager tokenManager;

	public static void main(String[] args) throws Exception {
		tokenManager = new TokenManager();
		URI serverURI = UriBuilder.fromUri(URL).port(PORT).build();
		// Add all server classes
		ResourceConfig serverConfig = new ResourceConfig(getClasses());
		// Convert a json of clientObj to clientObj automatically.
		serverConfig.property(SelectableEntityFilteringFeature.QUERY_PARAM_NAME, "select");
		JdkHttpServerFactory.createHttpServer(serverURI, serverConfig);
		System.out.println("The server is ready!");
	}

	/**
	 * Returns all the class which can handle requests.
	 * @return The set of all the classes which can handle the requests sent by the user.
	 */
	private static Set<Class<?>> getClasses() {
		Set<Class<?>> s = new HashSet<Class<?>>();
		s.add(ServerSearchFavoriteHandler.class);  // handle searchFavorites requests
		s.add(ServerUserHandler.class);            // handle user request
		s.add(ServerPokemonHandler.class);         // handle pokemon request
		s.add(ServerLikesStatsHandler.class);
		return s;
	}

	public static TokenManager getTokenManager() {
		return tokenManager;
	}

	public static void setTokenManager(TokenManager tokenManager) {
		Server.tokenManager = tokenManager;
	}
}