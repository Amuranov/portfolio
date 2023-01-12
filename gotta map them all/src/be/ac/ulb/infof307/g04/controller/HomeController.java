package be.ac.ulb.infof307.g04.controller;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import be.ac.ulb.infof307.g04.Main;
import be.ac.ulb.infof307.g04.model.dao.LocalisationPokemonDAO;
import be.ac.ulb.infof307.g04.model.LocalisationPokemon;
import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.SearchFavorite;
import be.ac.ulb.infof307.g04.model.client.SearchFavoriteCommunication;
import be.ac.ulb.infof307.g04.model.client.Session;
import be.ac.ulb.infof307.g04.model.client.UserCommunication;
import be.ac.ulb.infof307.g04.view.Message;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * The controller which manages the home view. 
 * (the one which contains a sidebar, a main view and the the search pokemon view.)
 */
public class HomeController implements Initializable {

	// user session
	private Session client;

	// Components of the view
	@FXML private ImageView logo;
	@FXML private ImageView logIn;
	@FXML private ImageView signUp;
	@FXML private ImageView logOut;
	@FXML private ImageView profile;
	@FXML private ImageView search;
	@FXML private BorderPane root;
	@FXML private VBox sidebar;
	@FXML private VBox searchPanel;
	@FXML private Button saveSearch;
	@FXML private Button helpSearch;
	
	// Search pokemon panel
	@FXML private ScrollPane markersScrollPane;
	@FXML private ScrollPane searchesScrollPane;
	@FXML private ComboBox<String> searchPokemonType;
	@FXML private ComboBox<String> searchPokemonName;
	private ObservableList<String> typesOnMap;
	private ObservableList<String> namesOnMap;
	// Make sure it doesn't fire both events from the ComboBox
	boolean eventSearchBox = false; 
	
	private ImageView selectedMenu;
	private Pane loginView;
	private Pane signUpView;
	private Pane defaultView;
	private Pane profileView;
	private ProfileController profileController;
	private FXMLLoader fxmlLoader;
	private MapController defaultController;
	
	// The searches that the client has saved.
	// It's populed in the initialize() method.
	private ArrayList<SearchFavorite> favorites;
	
	/**
	 * This enumeration contains all the possible contextual menus
	 */
	public enum TypeMenu {
		// The contextual menu when we aren't logged in.
		DISCONNECTED,
		// The contextual menu when we aren't logged in.
		CONNECTED
	}

	/**
	 * Constructs a home controller.
	 */
	public HomeController() {
		try {
			fxmlLoader = new FXMLLoader();
			loginView  = fxmlLoader.load(Main.class.getResource(ResourcesProvider.LOGIN_PATH).openStream());
			// Used to get access to parent controller from child.
			((LoginController) fxmlLoader.getController()).setParentController(this);
			fxmlLoader   = new FXMLLoader();
			signUpView   = fxmlLoader.load(Main.class.getResource(ResourcesProvider.SIGN_UP_PATH).openStream());
			((SignUpController) fxmlLoader.getController()).setParentController(this);
			fxmlLoader   = new FXMLLoader();
			defaultView  = fxmlLoader.load(Main.class.getResource(ResourcesProvider.DEFAULT_PATH).openStream());
			defaultController = ((MapController) fxmlLoader.getController());
			defaultController.setParentController(this);
			fxmlLoader   = new FXMLLoader();
			profileView  = fxmlLoader.load(Main.class.getResource(ResourcesProvider.PROFILE_PATH).openStream());
			profileController = fxmlLoader.getController();
			((ProfileController) fxmlLoader.getController()).setParentController(this);
			this.favorites = new ArrayList<>();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is called by the FXMLLoader when initialization is complete.
	 * @param fxmlFileLocation URL.
	 * @param resources
	 */
	@Override
	public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
		// initialize the logic here : all @FXML variables will have been injected
		root.setRight(null);
		selectedMenu = signUp;
		loadPage(signUpView, signUp);
		profile.setOnMouseClicked((e) -> loadPage(profileView, profile));
		logo.setOnMouseClicked((e)    -> loadPage(defaultView, logo));
		signUp.setOnMouseClicked((e)  -> loadPage(signUpView, signUp));
		logIn.setOnMouseClicked((e)   -> loadPage(loginView, logIn));

		saveSearch.setOnMouseClicked(e -> saveSearch());

		// When we click on the search icon (in the sidebar), 
		// we toggle the showing of the research panel.
		search.setOnMouseClicked((e)  ->  {
			// if the research panel is displayed in right side of the borderpane, 
			// then we hide it by nulling the right pane. 
			// Otherwise, we display the research panel if the right side of the borderpane is null.
			root.setRight(root.getRight() == null ? searchPanel : null);
		});

		logOut.setOnMouseClicked((e)  -> {
			loadPage(loginView, logIn);
			changeContextualMenu(TypeMenu.DISCONNECTED);
			UserCommunication clientCom = new UserCommunication();
			int responseCode = clientCom.logout(client.getTokenSession(),client.getUser().getUsername(),
					client.getUser().getUsername());
			if(responseCode == ResponseCode.OK.getValue())
				client = null;
			else 
				Message.show("Erreur", "Erreur lors de la dï¿½connexion", AlertType.ERROR);
		});
		
		// Launchs a web page with some help about the save search's feature.
		helpSearch.setOnMouseClicked(e -> {
			WebView webpage = new WebView();
			WebEngine html = webpage.getEngine();
			AnchorPane a = new AnchorPane();
			a.getChildren().add(webpage);
			html.loadContent("<h2> Lorsque vous n'êtes pas connecté</h2>" + 
				"<p> Les recherches sont sauvegardées dans un fichier en local. " +
				"Lors du lancement de l'application, ce fichier est directement chargé.</p>" +
				"<h2>Lorsque vous êtes connecté</h2>" +
				"<p> Les recherches sont sauvegardées sur un serveur et non dans le fichier local. " +
				"Lorsque vous vous connectez, ces sauvegardes sont immédiatement chargées et " +
				"remplacent celles qui sont sauvegardées en local.</p>");
			Message.showCustom("Aide", a, true);
		});
		
		
		initComboboxPokemonTypes();
		initComboboxPokemonNames();
		initMarkersOnScrollPane();
		initSearchesScrollPane();
		changeContextualMenu(TypeMenu.DISCONNECTED);
	}

	/**
	 * Save the current research. If we're connected, we save the research in the server's database.
	 * Otherwise, in a local file.
	 */
	private void saveSearch() {

		String selectedType    = searchPokemonType.getSelectionModel().getSelectedItem();
		String selectedPokemon = searchPokemonName.getSelectionModel().getSelectedItem();	
		SearchFavorite search  = new SearchFavorite(isAuth()? client.getUser().getID() : 0, selectedType, selectedPokemon);
		this.favorites.add(search);
		
		// If the guy is connected, we save his searches on the server.
		if(isAuth()) {
			SearchFavoriteCommunication sfc = new SearchFavoriteCommunication();
			int response = sfc.insert(search.getUserID(), search.getTypeID(), search.getPokemonID());
			if(response == ResponseCode.OK.getValue()) {
				Message.show("Succès !", "Votre recherche a été synchronisée avec le serveur avec succès !", AlertType.INFORMATION);
				addToSearchScrollPane(search);
			} else
				Message.show("Erreur de communication", "Oups... Une erreur de communication est survenue !", AlertType.ERROR);			
		} else {
			// If the guy is not connected, we save his searches in a file.
			try {
				FileOutputStream fos   = new FileOutputStream(Main.class.getResource(ResourcesProvider.FILE_SAVE_PATH).getPath());
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(this.favorites);
				oos.close();
				addToSearchScrollPane(search);
			} catch (IOException e) {
				Message.show("Erreur d'écriture", 
						"Oups... La sauvegarde de la recherche n'a pas pu se faire localement. "
						+ "Néanmoins, la recherche est sauvegardé en RAM, ne vous inquiétez pas ;-). "
						+ "Essayez de vous connecter pour que la sauvegarde se fasse sur un serveur distant.", 
						AlertType.ERROR);
			}
		}
	}

	/**
	 * Adds a {@link SearchFavorite} in the scrollpane which contains the saved searches.
	 * @param search The search.
	 */
	private void addToSearchScrollPane(SearchFavorite search) {
		Label label = new Label();
		HBox  line  = new HBox(10);
		ImageView img, types = new ImageView(ResourcesProvider.TYPES_IMAGE);
		
		line.setAlignment(Pos.CENTER_LEFT);
		line.setStyle("-fx-padding: 10;" + 
	                  "-fx-cursor: hand;");
		
		
		if(search.getTypeID() != null) {
			label.setText(search.getTypePokemon().getDescription() + " - ");
			label.setStyle("-fx-text-fill: " + ResourcesProvider.typeColor[search.getTypeID()]);
			// When we click on a saved search, we select the corresponding filters.
			line.setOnMouseClicked(e -> {
				searchPokemonType.getSelectionModel().select(search.getTypePokemon().getDescription());
				searchPokemonName.getSelectionModel().select(0);
			});
		} else
			label.setText("Tous les types - ");
		
		line.getChildren().add(types);
		line.getChildren().add(label);
		
		label = new Label();
		if(search.getPokemonID() != null) {
			img = new ImageView(ResourcesProvider.getPokemonMiniature(search.getPokemonID() - 1));
			label.setText(search.getPokemon().getName());
			label.setStyle("-fx-text-fill: " + ResourcesProvider.typeColor[search.getPokemon().getType1()]);
			// When we click on a saved search, we select the corresponding filters.
			line.setOnMouseClicked(e -> {
				searchPokemonName.getSelectionModel().select(search.getPokemon().getName());
				searchPokemonType.getSelectionModel().select(0);
			});
		} else {
			img = new ImageView(ResourcesProvider.LOGO_IMAGE_32);
			label.setText("Tous les pokemons");
		}
		line.getChildren().add(img);
		line.getChildren().add(label);
		
		ObservableList<Node> searchesInScrollPane = ((VBox) searchesScrollPane.getContent()).getChildren();
		searchesInScrollPane.add(line);
		
	}

	/**
	 * Indicates if the user is connected.
	 * @return true is (s)he's connected, false otherwise.
	 */
	public boolean isAuth() {
		return client != null && client.getUser() != null;
	}

	/**
	 * Loads all the markers in the scrollpane of the research pane.
	 * When we click on a marker on that scrollpane, the map is centered on that marker.
	 */
	private void initMarkersOnScrollPane() {
		markersScrollPane.setStyle("-fx-background: #34495e; -fx-background-color: #112233;"); // this color looks like asphalt
		List<LocalisationPokemon> locList = new LocalisationPokemonDAO().readAll();
		populateMarkersScrollPane(locList);
	}

	/**
	 * This function is called in the initialize() function of this class. 
	 * It loads the locally saved searches.
	 */
	private void initSearchesScrollPane() {
		searchesScrollPane.setStyle("-fx-background: #34495e; -fx-background-color: #112233;"); // this color looks like asphalt
		try {
			FileInputStream fis = new FileInputStream(Main.class.getResource(ResourcesProvider.FILE_SAVE_PATH).getPath());
			ObjectInputStream ois = new ObjectInputStream(fis);
			this.favorites = (ArrayList<SearchFavorite>) ois.readObject();
			ois.close();
			populateSearcheScrollPane();
		} catch(FileNotFoundException   | EOFException e) {
			// On n'informe pas le client qu'il n'y a pas de fichier de sauvegarde
			System.err.println("INFO : Le fichier de sauvegarde est vide ou n'est pas présent.");
		} catch (ClassNotFoundException | IOException e) {
			// If the save can't be found, we do not prevent the user. 
			// The scrollpane will simply be empty.
			System.err.println("Error : " + e);
		}
	}
	
	/**
	 * Populates the favorites researches scrollpane.
	 */
	public void populateSearcheScrollPane() {
		((VBox) searchesScrollPane.getContent()).getChildren().clear();
		for(SearchFavorite sf : this.favorites) 
			addToSearchScrollPane(sf);
	}

	/**
	 * Populates the scrollpane of the research pane with all the elements given in parameter.
	 * @param locList
	 */
	private void populateMarkersScrollPane(List<LocalisationPokemon> locList) {
		ObservableList<Node> markersInScrollPane = ((VBox) markersScrollPane.getContent()).getChildren();
		markersInScrollPane.clear();
		HBox line;
		ImageView img;
		String descr;
		for(LocalisationPokemon loc : locList) {
			line = new HBox(10);
			line.setAlignment(Pos.CENTER_LEFT);
			line.setStyle("-fx-cursor: hand;");
			descr = loc.getPokemon().getName() + " -> ( " 
						+ new DecimalFormat("#.00000").format(loc.getLatitude()) 
						+ "... , " 
						+ new DecimalFormat("#.00000").format(loc.getLongitude())
						+ "... )";
			img = new ImageView(ResourcesProvider.getPokemonMiniature(loc.getPokemonId() - 1));
			line.getChildren().add(img);
			line.getChildren().add(new Label(descr));
			markersInScrollPane.add(line);
		
			line.setPadding(new Insets(5, 10, 10, 5));
			// When we click on a marker in the scroll pane, 
			// it centers the google map on it.
			line.setOnMouseClicked(e -> {
				goToHome();
				defaultController.getMapView().setCenter(loc.getLatitude(), loc.getLongitude());
			});
		}
	}

	/**
	 * Goes to home panel
	 */
	public void goToHome() {
		loadPage(defaultView, logo);
	}

	/**
	 * Goes to login panel
	 */
	public void goToLogin() {
		loadPage(loginView, logIn);
	}

	/**
	 * Changes the current contextual side bar menu.
	 * @param typeMenu The new contextual menu.
	 */
	public void changeContextualMenu(TypeMenu typeMenu) {
		hideAllMenus();
		showMenu(logo);
		switch(typeMenu) {
			case CONNECTED:
				showMenu(profile);
				showMenu(logOut);
				break;
			case DISCONNECTED:
				showMenu(logIn);
				showMenu(signUp);
				break;
			default:
				throw new IllegalArgumentException("Please put a contextual menu type.");
		}
	}

	/**
	 * Hides all menu in the sidebar.
	 */
	private void hideAllMenus() {
		logo.getParent().setManaged(false);
		logo.getParent().setVisible(false);
		logIn.getParent().setManaged(false);
		logIn.getParent().setVisible(false);
		logOut.getParent().setManaged(false);
		logOut.getParent().setVisible(false);
		signUp.getParent().setManaged(false);
		signUp.getParent().setVisible(false);
		profile.getParent().setVisible(false);
		profile.getParent().setManaged(false);
	}

	/**
	 * Shows the desired menu in the sidebar
	 * @param menu The desired menu
	 */
	private void showMenu(ImageView menu) {
		menu.getParent().setManaged(true);
		menu.getParent().setVisible(true);
	}

	/**
	 * Changes the current page.
	 * @param newPane The new current page
	 * @param destination The icon on the sidebar that corresponds the new page.
	 */
	private void loadPage(Pane newPane, ImageView destination) {
		root.setCenter(newPane);
		selectedMenu.getParent().getStyleClass().remove("menuClicked");
		destination.getParent().getStyleClass().add("menuClicked");
		selectedMenu = destination;
	}

	/**
	 * Creates a session in order to be connected to the app.
	 * @param session
	 */
	public void createSession(Session session) {
		client = session;
		defaultController.sendLocalPokemonToServer();
	}

	public Session getClient() {
		return client;
	}

	/**
	 * Returns the client's id if he's connected. -1 otherwise.
	 * @return
	 */
	public int getClientId() {
		return isAuth() ? client.getUser().getID() : -1;
	}
	
	public String getClientUsername() {
		return isAuth() ? client.getUser().getUsername() : "Visiteur (vous)";
	}
	
	/**
	 * Initializes the combobox containing the pokemon types (it is displayed in the search panel).
	 */
	private void initComboboxPokemonTypes() {
		List<String> pokemonTypes = new LocalisationPokemonDAO().readAllPokemonTypes();
		pokemonTypes.add(0, "Tous les types");
		typesOnMap = FXCollections.observableList(pokemonTypes);
		searchPokemonType.setItems(typesOnMap);
		searchPokemonType.getSelectionModel().select(0);
		// Event: if we change the ComboBox to filter the markers according to the type of a Pokemon
		searchPokemonType.setOnAction(e -> {
			if(!eventSearchBox) {
				eventSearchBox = true;
				searchPokemonName.getSelectionModel().select(0);
				eventSearchBox = false;

				String type = searchPokemonType.getSelectionModel().getSelectedItem();
				List<LocalisationPokemon> displayedMarkers;
				if(searchPokemonType.getSelectionModel().isSelected(0)) {
					displayedMarkers = defaultController.displayAllMarkers();
					populateMarkersScrollPane(displayedMarkers);
				}
				else {
					displayedMarkers = defaultController.displayPokemonType(type);
					populateMarkersScrollPane(displayedMarkers);
				}
			}
		});
	}

	/**
	 * Initializes the combobox containing the pokemon names that are currently on the map (it is displayed in the search panel).
	 */
	private void initComboboxPokemonNames() {
		List<String> pokemonNames = new LocalisationPokemonDAO().readAllPokemonNames();
		pokemonNames.add(0, "Tous les noms");
		namesOnMap = FXCollections.observableList(pokemonNames);
		searchPokemonName.setItems(namesOnMap);
		searchPokemonName.getSelectionModel().select(0);

		// Event: if we change the ComboBox to filter the markers according to the name of a Pokemon
		searchPokemonName.setOnAction(e -> {
			if(!eventSearchBox) {
				eventSearchBox = true;
				searchPokemonType.getSelectionModel().select(0);
				eventSearchBox = false;

				String name = searchPokemonName.getSelectionModel().getSelectedItem();
				List<LocalisationPokemon> displayedMarkers;
				if(searchPokemonName.getSelectionModel().isSelected(0)) {
					// We display all the markers and receive which ones are displayed.
					displayedMarkers = defaultController.displayAllMarkers();
					populateMarkersScrollPane(displayedMarkers);
				} 
				else {
					displayedMarkers = defaultController.displayPokemonName(name);
					populateMarkersScrollPane(displayedMarkers);
				}
			}
		});
	}

	/**
	 * Adds a type in the combobox containing the pokemon types if it's not already there.
	 * @param type1 The first type of the pokemon.
	 * @param type2 The second type of the pokemon.
	 */
	public void addPokemonType(String type1, String type2) {
		if (!typesOnMap.contains(type1))
			typesOnMap.add(type1);
		if (type2!=null && !typesOnMap.contains(type2)) 
			typesOnMap.add(type2);
		searchPokemonType.setItems(typesOnMap);
	}

	/**
	 * Adds a name in the combobox containing the pokemon names if it's not already there.
	 * @param name The name of the pokemon.
	 */
	public void addPokemonName(String name) {
		if (!namesOnMap.contains(name))
			namesOnMap.add(name);
		searchPokemonName.setItems(namesOnMap);
	}

	/**
	 * Update view client information
	 */
	public void populateProfileDatas() {
		if(client == null)
			throw new IllegalStateException("Session not set");
		else { 
			// We populate fields on the profile view profile
			profileController.setUsername(client.getUser().getUsername());
			profileController.setEmail(client.getUser().getMail());
		}
	}

	/**
	 * Returns the root BorderPane
	 * @return the root element
	 */
	public BorderPane getRoot() {
		return root;
	}
	
	/**
	 * @return the favorites
	 */
	public ArrayList<SearchFavorite> getFavorites() {
		return favorites;
	}

	/**
	 * @param favorites the favorites to set
	 */
	public void setFavorites(ArrayList<SearchFavorite> favorites) {
		this.favorites = favorites;
	}

}