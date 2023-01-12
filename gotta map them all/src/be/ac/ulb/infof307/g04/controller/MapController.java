package be.ac.ulb.infof307.g04.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.lynden.gmapsfx.ClusteredGoogleMapView;
import com.lynden.gmapsfx.MapComponentInitializedListener;
import com.lynden.gmapsfx.javascript.event.UIEventType;
import com.lynden.gmapsfx.javascript.object.ClusteredGoogleMap;
import com.lynden.gmapsfx.javascript.object.DirectionsPane;
import com.lynden.gmapsfx.javascript.object.InfoWindow;
import com.lynden.gmapsfx.javascript.object.InfoWindowOptions;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import com.lynden.gmapsfx.javascript.object.Marker;
import com.lynden.gmapsfx.javascript.object.MarkerOptions;
import com.lynden.gmapsfx.service.directions.DirectionStatus;
import com.lynden.gmapsfx.service.directions.DirectionsRenderer;
import com.lynden.gmapsfx.service.directions.DirectionsRequest;
import com.lynden.gmapsfx.service.directions.DirectionsResult;
import com.lynden.gmapsfx.service.directions.DirectionsService;
import com.lynden.gmapsfx.service.directions.DirectionsServiceCallback;
import com.lynden.gmapsfx.service.directions.DirectionsWaypoint;
import com.lynden.gmapsfx.service.directions.TravelModes;
import com.lynden.gmapsfx.util.MarkerImageFactory;

import be.ac.ulb.infof307.g04.model.LocalisationPokemon;
import be.ac.ulb.infof307.g04.model.Pokemon;
import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import be.ac.ulb.infof307.g04.model.client.PokemonCommunication;
import be.ac.ulb.infof307.g04.view.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.AnchorPane;
import netscape.javascript.JSObject;

/**
 * The controller which manages the default view (where there is the google map)
 */
public class MapController implements Initializable, MapComponentInitializedListener, DirectionsServiceCallback  {

	// @FXML private Button button;
	@FXML private AnchorPane mapViewAnchorPane;
	@FXML private HomeController parent;
	@FXML private CheckBox directionCheckBox;
	@FXML private ClusteredGoogleMapView mapView; 	//for the clustering of the markers

	private ArrayList<LocalisationPokemon> localisationPokemons;
	private boolean isGetDirection;	//if checkbox direction is selected
	private DirectionsService directionsService;
	private DirectionsPane directionsPane;
	private List<DirectionsWaypoint> wayPoints = new ArrayList<DirectionsWaypoint>(); //list of intermediate points
	private DirectionsRenderer directionsRenderer = null; //display direction
	private PokemonCommunication pokemonCommunication;
	private ClusteredGoogleMap map;					//for the clustering of the markers

	boolean eventSearchBox = false; // Make sure it doesn't fire both events from the ComboBox


	@Override
	public void initialize(URL url, ResourceBundle rb) {
		mapView = new ClusteredGoogleMapView(null, Locale.getDefault().getLanguage(), "AIzaSyCyPveo-_fKaqlWEJVKfqqdO33wnntPmmU", false);
		mapView.addMapInializedListener(this);
		mapViewAnchorPane.getChildren().add(mapView);
		mapView.prefWidthProperty().bind(mapViewAnchorPane.widthProperty());
		mapView.prefHeightProperty().bind(mapViewAnchorPane.heightProperty());
		pokemonCommunication = new PokemonCommunication();
		localisationPokemons = new ArrayList<>();
	}

	@Override
	public void mapInitialized() {
		map = mapView.createMap(initMapOptions(), false);

		directionCheckBox.setOnAction(e -> handleGetDirectionCheckBox(e));
		directionsService  = new DirectionsService();
		directionsPane     = mapView.getDirec();
		directionsRenderer = new DirectionsRenderer(true, mapView.getMap(), directionsPane);

		// Placer les pins sur la map
		placeAllMarkers();

		// Handle click event on map
		map.addUIEventHandler(UIEventType.click, (JSObject obj) -> {
			try {
				FXMLLoader fxmlLoader = new FXMLLoader();
				AnchorPane pane = fxmlLoader.load(MapController.class.getResource(ResourcesProvider.ADD_POKEMON_POPUP_PATH).openStream());
				AddPokemonPopupController addPokemon = ((AddPokemonPopupController) fxmlLoader.getController());
				Message.showCustom("Sï¿½lectionnez votre pokï¿½mon", pane, false);
				if(! addPokemon.isCanceled()) {
					LatLong position = new LatLong((JSObject) obj.getMember("latLng"));
					Marker marker = createMarker(addPokemon.getSelectedPokemon().getImageMiniature(), position);
					String userName = parent.getClientUsername();
					int life = addPokemon.getSelectedPokemon().getLife();
					int att = addPokemon.getSelectedPokemon().getAttack();
					int def = addPokemon.getSelectedPokemon().getDefense();
					int locId = sendLocalisationToServer(addPokemon, position, marker);
					markerClickEvent(marker, locId, userName, addPokemon.getDate(), addPokemon.getTime(),
							addPokemon.getSelectedPokemon(), position,att,def,life);
					map.addClusterableMarker(marker);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/**
	 * Set the instance of the parent controller on the child controller
	 * @param parent Controller instance of the parent pane
	 */
	public void setParentController(HomeController parent) {
		this.parent = parent;
	}

	/**
	 * Set the action when we select the "GetDirection" checkbox.
	 * @param e event of checkbox
	 */
	private void handleGetDirectionCheckBox(ActionEvent e) {
		if(directionCheckBox.isSelected()){
			isGetDirection = true;
		} else {
			isGetDirection = false;
			wayPoints.clear();
		}
	}

	/**
	 * Initializes the options of the map.
	 * @return The {@link MapOptions}.
	 */
	private MapOptions initMapOptions(){
		MapOptions mapOptions = new MapOptions();
		mapOptions.center(new LatLong(50.8129955, 4.3788422))
		.mapType(MapTypeIdEnum.ROADMAP)
		.overviewMapControl(true)
		.panControl(true)
		.rotateControl(true)
		.scaleControl(true)
		.streetViewControl(false)
		.zoomControl(false)
		.zoom(16);
		return mapOptions;
	}

	/**
	 * Places all markers on the map.
	 */
	private void placeAllMarkers() {
		localisationPokemons = (ArrayList<LocalisationPokemon>) pokemonCommunication.readAll();
		for (LocalisationPokemon locPokemon : localisationPokemons) {
			LatLong position = new LatLong(locPokemon.getLatitude(), locPokemon.getLongitude());
			Marker marker = createMarker(locPokemon.getPokemon().getImageMiniature(), position);
			locPokemon.setMarker(marker);
			markerClickEvent(marker, locPokemon.getLocId(), locPokemon.getUser().getUsername(), locPokemon.getAtTime().toString(), "not yet defined",
					locPokemon.getPokemon(), position,locPokemon.getAttackPoints(),
					locPokemon.getDefPoints(),locPokemon.getLifePoints());
			map.addClusterableMarker(marker);
		}
	}

	/**
	 * Creates a marker and add it on the map.
	 * @param iconPath The path of the icon.
	 * @param position The pokemon's localization.
	 * @return The created marker.
	 */
	private Marker createMarker(String iconPath, LatLong position) {
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.icon(MarkerImageFactory.createMarkerImage(iconPath, "png"));
		markerOptions.position(position);
		return new Marker(markerOptions);
	}

	/**
	 * The handler of the marker's click event.
	 * @param marker The marker.
	 * @param username
	 * @param date
	 * @param time
	 * @param pokemon
	 * @param position
	 */
	private void markerClickEvent(Marker marker, int locId, String username, String date, String time, Pokemon pokemon,
			LatLong position,int att,int def,int life) {

		map.addUIEventHandler(marker, UIEventType.click, (JSObject event) -> {
			if(!isGetDirection) {
				if(parent.isAuth() && parent.getClient().getUser().getUsername().equals(username)){
					editMarkerEvent(marker, username, date, time, pokemon,position,att,def,life);
				}
				else{
					markerPokemonInfosEvent(marker, locId, username, date, time, pokemon,att,def,life);
				}
			} else {
				wayPoints.add(new DirectionsWaypoint(position.getLatitude() + ", " + position.getLongitude()));
				markerDirectionEvent();
			}
		});
	}
	/**
	 *Set the route on the map
	 */
	private void markerDirectionEvent() {
		DirectionsRequest request = new DirectionsRequest("50.8129955, 4.3788422", "50.8129955, 4.3788422", TravelModes.WALKING, wayPoints.toArray(new DirectionsWaypoint[wayPoints.size()]));
		directionsService.getRoute(request, this, directionsRenderer);
		directionsRenderer.setMap(null);
	}

	/**
	 * Create the pop up when click on the marker (if checkbox not selected)
	 * @param marker
	 * @param username
	 * @param date
	 * @param time
	 * @param pokemon
	 */
	private void markerPokemonInfosEvent(Marker marker, int locId, String username, String date, String time, Pokemon pokemon,int att,int def, int life) {
		String img = pokemon.getImageBig();
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource(ResourcesProvider.POKEMON_INFO));
			MarkersInfoController controller = new MarkersInfoController(locId, parent.isAuth(),parent,img,username,date,time,pokemon.getName(),life,att,def);
			loader.setController(controller);
			AnchorPane pane = loader.load();
			Message.showCustom("pokemon info", pane, false);
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * Describes the behaviour when an editable marker is clicked
	 * @param marker
	 * @param username 
	 * @param date date at which the marker was placed
	 * @param time time at which the marker was placed
	 * @param pokemon
	 * @param position where the marker is located on the map
	 * @param att the new attack points
	 * @param def the new defense points
	 * @param life the new life points
	 */
	private void editMarkerEvent(Marker marker, String username, String date, String time,
			Pokemon pokemon,LatLong position,int att,int def,int life){
		try {
			FXMLLoader loader = new FXMLLoader();
			AnchorPane pane = loader.load(MapController.class.getResource(ResourcesProvider.EDIT_MARKER_POPUP_PATH).openStream());
			EditMarkerPopupController editMarker = ((EditMarkerPopupController) loader.getController());
			editMarker.setPokemonInfo(pokemon.getName(),pokemon.getId());
			editMarker.setPoints(life,att,def);   
			Message.showCustom("", pane, false);
			if(!editMarker.isCanceled()) {
				boolean success = sendEditedMarkerToServer(editMarker, pokemon, position, marker, date);

				if(success) {
					refresh();
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
	}

	/**
	 * Sends localisation to server
	 * @param addPokemon
	 * @param position
	 */
	private int sendLocalisationToServer(AddPokemonPopupController addPokemon, LatLong position, Marker marker){
		int locId = 0;
		LocalisationPokemon localisationPokemon = new LocalisationPokemon(addPokemon.getSelectedPokemon() ,-1,
				position.getLatitude(), position.getLongitude(), addPokemon.getDate(), marker);

		if(parent.isAuth()) {
			PokemonCommunication pokemonCommunication = new PokemonCommunication();
			localisationPokemon.setUserId(parent.getClientId());
			locId = Integer.parseInt(pokemonCommunication.insert(parent.getClient().getTokenSession(),
					parent.getClientUsername(),localisationPokemon));
			localisationPokemon.setOnServer(true);
			if(locId <= 0) {
				Message.show("Erreur", "Ce mï¿½me pokemon a deja ï¿½tï¿½ placï¿½ dans les environs", AlertType.ERROR);
			}
		}
		localisationPokemons.add(localisationPokemon);
		parent.addPokemonType(addPokemon.getFirstType(), addPokemon.getSecondType());
		parent.addPokemonName(addPokemon.getSelectedPokemon().getName());
		return locId;

	}
	/**
	 * Sends the edited markers to the server
	 * @param editMarkerController The controller for the edit marker view
	 * @param pokemon The pokemon concerned with the modification
	 * @param position The position of the marker
	 * @param marker the GMapsFX marker
	 * @param date the date when the marker was added
	 * @return
	 */
	private boolean sendEditedMarkerToServer(EditMarkerPopupController editMarkerController,Pokemon pokemon,LatLong position, Marker marker,String date){
		boolean success = false;
		LocalisationPokemon localisationPokemon = new LocalisationPokemon(pokemon.getId() ,parent.getClientId(),
				position.getLatitude(), position.getLongitude(),date,
				editMarkerController.getAttack(),editMarkerController.getDefense(),editMarkerController.getLife());
		int responseStatus = pokemonCommunication.update(parent.getClient().getTokenSession(),
				parent.getClientUsername(),localisationPokemon);
		if(responseStatus != 200) {
			Message.show("Erreur", "La signalisation n'a pas pu etre modifiée", AlertType.ERROR);
		} else{
			success = true;
		}
		return success;
	}
	/**
	 * Sends local pokemons to server
	 */
	public void sendLocalPokemonToServer(){
		if(parent.isAuth() && localisationPokemons.size() > 0) {
			for (LocalisationPokemon pokemon : localisationPokemons){
				if(!pokemon.isOnServer()) {
					pokemon.setUserId(parent.getClient().getUser().getID());
					pokemon.setUser(parent.getClient().getUser());
					pokemonCommunication.insert(parent.getClient().getTokenSession(),
							parent.getClientUsername(),pokemon);
					pokemon.setOnServer(true);
				}
			}
			refresh();
		}
	}


	/**
	 * Displays on the map all the markers.
	 * @return Returns all the {@link LocalisationPokemon} that correspond to the displayed markers on the map.
	 */
	public List<LocalisationPokemon> displayAllMarkers() {
		removeAllMarkers(); // remove all the markers in order to have only one marker for one Pokemon
		List<LocalisationPokemon> correspondingMarkers = new ArrayList<>();
		for(LocalisationPokemon locPokemon : localisationPokemons) {
			map.addClusterableMarker(locPokemon.getMarker());
			correspondingMarkers.add(locPokemon);
		}
		refreshMap();
		return correspondingMarkers;
	}
	/**
	 * Refreshes the markers on the map
	 */
	private void refresh(){
		for(LocalisationPokemon element :  localisationPokemons){
			map.removeClusterableMarker(element.getMarker());
			element = null;
		}
		localisationPokemons = null;
		placeAllMarkers();
	}
	private void removeAllMarkers() {
		for(LocalisationPokemon locPokemon : localisationPokemons) {
			map.removeClusterableMarker(locPokemon.getMarker());
		}
	}

	/**
	 * Displays on the map all the markers that correspond to the desired pokemon's type.
	 * @param type The type of the pokemon
	 * @return Returns all the {@link LocalisationPokemon} that correspond to the displayed markers on the map.
	 */
	public List<LocalisationPokemon> displayPokemonType(String type) {
		removeAllMarkers(); // remove all the markers in order to have only one marker for one Pokemon
		List<LocalisationPokemon> correspondingMarkers = new ArrayList<>();
		for(LocalisationPokemon locPokemon : localisationPokemons) {
			Pokemon pokemon = locPokemon.getPokemon();
			if(pokemon.getStringType1().equals(type) || (pokemon.getStringType2() != null && pokemon.getStringType2().equals(type))) {
				map.addClusterableMarker(locPokemon.getMarker());
				correspondingMarkers.add(locPokemon);
			}
		}
		refreshMap();
		return correspondingMarkers;
	}

	/**
	 * Displays on the map all the markers that correspond to the desired pokemon's name.
	 * @param name The name of the pokemon
	 * @return Returns all the {@link LocalisationPokemon} that correspond to the displayed markers on the map.
	 */
	public List<LocalisationPokemon> displayPokemonName(String name) {
		removeAllMarkers(); // remove all the markers in order to have only one marker for one Pokemon
		List<LocalisationPokemon> correspondingMarkers = new ArrayList<>();
		for(LocalisationPokemon locPokemon : localisationPokemons) {
			if(locPokemon.getPokemon().getName().equals(name)) {
				map.addClusterableMarker(locPokemon.getMarker());
				correspondingMarkers.add(locPokemon);
			}
		}
		refreshMap();
		return correspondingMarkers;
	}
	/**
	 * Resets the map to its initial state
	 */
	private void refreshMap() {
		int currentZoom = map.getZoom();
		map.setZoom( currentZoom - 1 );
		map.setZoom( currentZoom );
	}

	public ClusteredGoogleMapView getMapView() {
		return mapView;
	}

	@Override
	public void directionsReceived(DirectionsResult results, DirectionStatus status) {
		// TODO Auto-generated method stub
		
	}
}