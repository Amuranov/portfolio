package be.ac.ulb.infof307.g04.controller;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import be.ac.ulb.infof307.g04.model.Pokemon;
import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import be.ac.ulb.infof307.g04.view.Message;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.time.LocalDateTime;

/**
 * The controller of the AddpokemonPopup view.
 * This is the controller of the little popup that appears when we click on the google map.
 */
public class AddPokemonPopupController implements Initializable {
	
	// Definition of some UI elements of the fxml.
	@FXML private DatePicker datePokemon;
	@FXML private ComboBox<Pokemon> selectPokemon;
	@FXML private Button closePopup;
	@FXML private Button addPokemon;
	@FXML private Label nameSelectedPokemon;
	@FXML private ImageView imgSelectedPokemon;
	@FXML private ProgressBar life;
	@FXML private ProgressBar attack;
	@FXML private ProgressBar defense;
	@FXML private Label lifeLabel;
	@FXML private Label attackLabel;
	@FXML private Label defenseLabel;
	@FXML private Label type1;
	@FXML private Label type2;
	@FXML private TextField hour;
	private String time;
	// A regex that matches the pattern of an hour (user when we want to add a pokemon on the map).
	public static final Pattern HOUR_REGEX = Pattern.compile("^([0-9]{2}|[0-9]):[0-9]{2}:[0-9]{2}$", Pattern.CASE_INSENSITIVE);

	private boolean canceled;
	private String pokemonURL;
	private int pokemonId;
	private String date;
	private int locId;
	
	public int getLocId() {
		return locId;
	}

	public void setLocId(int locId) {
		this.locId = locId;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		LocalDateTime now = LocalDateTime.now();
		time = now.format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ENGLISH));
		this.hour.setText(time);
		this.selectPokemon.getItems().addAll(ResourcesProvider.getAllPokemons());
		this.selectPokemon.setCellFactory(customComboBox);
		this.imgSelectedPokemon.setImage(ResourcesProvider.LOGO_IMAGE);
		
		// The event for when we select a pokemon in the popup.
		// It shows the pokemon stats, its picture and its types.
		this.selectPokemon.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Pokemon>() {
			@Override
			public void changed(ObservableValue<? extends Pokemon> observable, Pokemon oldPokemon, Pokemon newPokemon) {
				nameSelectedPokemon.setText("#" + String.format("%03d", newPokemon.getId()) + " " + newPokemon.getName());
				imgSelectedPokemon.setImage(ResourcesProvider.getPokemonImage(newPokemon.getId() - 1));
				// display stats
				life.setProgress(0.5*newPokemon.getLife() /100.0);
				attack.setProgress(0.5*newPokemon.getAttack() / 100.0);
				defense.setProgress(0.5*newPokemon.getDefense() / 100.0);
				// display types
				type1.setText(newPokemon.getStringType1());
				if(newPokemon.getType2() != 0)
					type2.setText(newPokemon.getStringType2());
				else
					type2.setText("");
				
				updateProgressColor(life, attack, defense);
				updateColorType();
			}
		});
		
		closePopup.setOnAction(e -> {
			canceled = true;
			((Stage) closePopup.getScene().getWindow()).close();
		});
		
		// Event for when we click on "Ajouter" button in the popup.
		// The date and time must be in a correct format and a pokemon must be selected.
		addPokemon.setOnAction(e -> {
			if(datePokemon.getValue() != null && HOUR_REGEX.matcher(hour.getText()).find())
			{
				pokemonURL = ResourcesProvider.IMAGE_PATH + String.format("%03d", selectPokemon.getSelectionModel().getSelectedItem().getId())+".png";
				pokemonId = selectPokemon.getSelectionModel().getSelectedItem().getId();
				date = new SimpleDateFormat("dd/MM/yyyy").format(Date.from(datePokemon.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()));
				time = hour.getText();
				((Stage) closePopup.getScene().getWindow()).close();
			} else {
				Message.show("Erreur", "Veuillez remplir le formulaire convenablement. Regardez bien le pattern d�sir� pour la date et l'heure !", AlertType.ERROR);
			}

		});

	}

	/**
	 * We change the color of each {@link ProgressBar} that we receive in parameter.
	 * Each range of value has a color. For instance, if the progressbar is at 50%, then its color will be #2ecc71 (kind of green).
	 * @param p
	 */
	private void updateProgressColor(ProgressBar... p) {
		for(int i = 0; i < p.length; i++) {
			double value = p[i].getProgress();
			if (0 <= value && value <= 0.1)
				p[i].setStyle("-fx-accent: #c0392b"); // fire red
			else if (0.1 < value && value <= 0.2)
				p[i].setStyle("-fx-accent: #e74c3c"); // light red
			else if (0.2 < value && value <= 0.4)
				p[i].setStyle("-fx-accent: #f39c12"); // pee yellow
			else if (0.4 < value && value <= 0.6)
				p[i].setStyle("-fx-accent: #2ecc71"); // grass (really green)
			else if (0.6 < value && value <= 0.8)
				p[i].setStyle("-fx-accent: #27ae60"); // spinach green
			else
				p[i].setStyle("-fx-accent: #663399"); // dark purple chaos
		}
	}

	/**
	 * Used to set the background color of the pokemon's type in the popup
	 */
	private void updateColorType() {
		type1.setStyle("-fx-background-color:" + ResourcesProvider.typeColor[getSelectedPokemon().getType1()]);
		type2.setStyle("-fx-background-color:" + ResourcesProvider.typeColor[getSelectedPokemon().getType2()]);
	}

	/**
	 * This is a combobox that we've made because the standard one can't displays images in the options.
	 */
	private Callback<ListView<Pokemon>, ListCell<Pokemon>> customComboBox =
			new Callback<ListView<Pokemon>, ListCell<Pokemon>>() {
		@Override
		public ListCell<Pokemon> call(ListView<Pokemon> p) {
			return new ListCell<Pokemon>() {
				@Override
				protected void updateItem(Pokemon item, boolean empty) {
					super.updateItem(item, empty);
					if (item == null || empty)
						setGraphic(null);
					else {
						setText(item.getName());
						setGraphic(new ImageView(ResourcesProvider.getPokemonMiniature(item.getId() - 1)));
					}
				}
			};
		}
	};


	public String getFirstType() {
		return selectPokemon.getSelectionModel().getSelectedItem().getStringType1();
	}
	
	public String getSecondType() {
		return selectPokemon.getSelectionModel().getSelectedItem().getStringType2();
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
	}

	public String getPokemonURL() {
		return pokemonURL;
	}

	public void setPokemonURL(String pokemonURL) {
		this.pokemonURL = pokemonURL;
	}


	public int getPokemonId() {
		return pokemonId;
	}

	public void setPokemonId(int pokemonId) {
		this.pokemonId = pokemonId;
	}

	public Pokemon getSelectedPokemon() {
		return selectPokemon.getSelectionModel().getSelectedItem();
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}


	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
}
