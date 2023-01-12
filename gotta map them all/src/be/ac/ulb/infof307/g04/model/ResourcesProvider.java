package be.ac.ulb.infof307.g04.model;

import java.io.IOException;
import java.util.ArrayList;

import be.ac.ulb.infof307.g04.Main;
import be.ac.ulb.infof307.g04.model.dao.PokemonDAO;
import be.ac.ulb.infof307.g04.model.dao.TypeDAO;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * Class that provides resources instead of hardcoding them each time we need
 * one and to avoid a lot of SQL queries and disk access.
 */
public class ResourcesProvider {
	// Definition of some useful PATHS.
	public static final String BASE_PATH  = "/be/ac/ulb/infof307/g04/";
	public static final String VIEW_PATH  = BASE_PATH + "view/";
	public static final String IMAGE_PATH = BASE_PATH + "img/";

	// A read-only list containing all the pokemons in order to avoid a lot of SQL queries.
	private static final ArrayList<Pokemon> allPokemons = (ArrayList<Pokemon>) new PokemonDAO().readAll();
	// A read-only list containing all the types in order to avoid a lot of SQL queries.
	private static final ArrayList<String>  allTypes    = (ArrayList<String>) new TypeDAO().readAll();
	// A read-only list containing all the pokemon's miniature images
	// in order to avoid a lot of disk access during the application.
    private static ArrayList<Image> miniaturesPokemons;
	// A read-only list containing all the pokemon's images. Same reason as above.
    private static ArrayList<Image> imgPokemons;
	// Define path of views
    public static final String HOME_PATH              = VIEW_PATH + "Home.fxml";
    public static final String LOGIN_PATH             = VIEW_PATH + "Login.fxml";
    public static final String SIGN_UP_PATH           = VIEW_PATH + "SignUp.fxml";
    public static final String DEFAULT_PATH           = VIEW_PATH + "Default.fxml";
    public static final String PROFILE_PATH           = VIEW_PATH + "Profile.fxml";
	public static final String ADD_POKEMON_POPUP_PATH = VIEW_PATH + "AddPokemonPopup.fxml";
	public static final String EDIT_MARKER_POPUP_PATH = VIEW_PATH + "EditMarkerPopup.fxml";
	public static final String POKEMON_INFO 		  = VIEW_PATH + "PokemonInfo.fxml";


    // Define (most used) images in order to avoid disk access each time we need to create those images
    public static final Image LOGO_IMAGE    = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "icon.png"));
    public static final Image LOGO_64_IMAGE = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "icon64.png"));
	public static final Image LOGO_IMAGE_32 = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "icon32.png"));
    public static final Image PIKACHU_IMAGE = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "pikachu.png"));
	public static final Image PIKACHU_IMAGE_SAD = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "error.png"));
	public static final Image TYPES_IMAGE   = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "types.png"));
    // The file where we store all the researchs of the client (if he's not connected).
	public static final String FILE_SAVE_PATH = BASE_PATH + "model/favorites.sav";

	public static final Image LIKE   = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "like.png"));
	public static final Image LIKE_BL   = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "likebl.png"));
	public static final Image DISLIKE   = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "dislike.png"));
	public static final Image DISLIKE_BL   = new Image(Main.class.getResourceAsStream(IMAGE_PATH + "dislikebl.png"));


	/**
	 *Assign a color to each existing type
	 *[0] - no type
	 *[1] - Steel (#aaaabb)
	 *...
	 *[n] - color for the nth type
	 */
	public static final String[] typeColor = {
			"transparent",
			"#aaaabb", "#bb5544", "#7867ee",
			"#3399ff", "#cba922", "#ffaaff",
			"#ff4624", "#77ddff", "#abbc22",
			"#bbbbaa", "#559833", "#783467",
			"#ff579a", "#ba9955", "#ddbb55",
			"#56569a", "#775544", "#6597fd",
			"#abbc22"
	};

	/**
	 * Creates a Pane according to the given view URL.
	 * @param url The URL of the view
	 * @return The view corresponding to the given URL.
	 * @throws IOException if the view can't be opened or found.
	 */
	public static Pane createPane(String url) throws IOException {
		return FXMLLoader.load(ResourcesProvider.class.getResource(url));
	}

	/**
	 * This is not a "getter" (it's a static function).
	 * Returns a read-only list containing all the pokemons.
	 * @return Returns a read-only list containing all the pokemons.
	 */
	public static ArrayList<Pokemon> getAllPokemons() {
		return allPokemons;
	}

    /**
     * That function is not a "getter" (it's static).
     * It creates a list (if it's null) containing all the pokemon's miniature images (32x32 px)
     * before sending the one we are interested in.
     * @param index the index of the desired pokemon's miniature.
     * @return The miniature of the desired pokemon.
     */
	public static Image getPokemonMiniature(int index) {
		if(miniaturesPokemons == null)
			initMiniatures();
		return miniaturesPokemons.get(index);
	}

    /**
     * That function is not a "getter" (it's static).
     * It creates a list (if it's null) containing all the pokemon's images
     * before sending the one we are interested in.
     * @param index the index of the desired pokemon's image.
     * @return The image of the desired pokemon.
     */
	public static Image getPokemonImage(int index) {
		if(miniaturesPokemons == null)
			initMiniatures();
		return imgPokemons.get(index);
	}

	/**
	 * Initializes the list of pokemon's miniature images.
	 */
	public static void initMiniatures() {
		miniaturesPokemons = new ArrayList<>();
		int nbPokemons = 722;
		for(int i = 1; i <= nbPokemons; i++) {
			miniaturesPokemons.add(new Image(Main.class.getResourceAsStream(String.format(IMAGE_PATH + "%03d", i).concat(".png"))));
		}
	}

	/**
	 * Initializes the list of pokemon's images.
	 */
	public static void initImages() {
		imgPokemons = new ArrayList<>();
		int nbPokemons = 722;
		for(int i = 1; i <= nbPokemons; i++) {
			imgPokemons.add(new Image(Main.class.getResourceAsStream(String.format(IMAGE_PATH + "grands/%03d", i).concat(".png"))));
		}
	}

	/**
	 * Getter of all types
	 * @return all types
	 */
	public static ArrayList<String> getAlltypes() {
		return allTypes;
	}
}
