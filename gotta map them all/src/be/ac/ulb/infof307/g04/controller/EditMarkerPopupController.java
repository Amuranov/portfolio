package be.ac.ulb.infof307.g04.controller;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
/**
 * Controller for the window used to modify markers
 *
 */
public class EditMarkerPopupController implements Initializable{

	@FXML private Button submitButton;
	@FXML private Button closeButton;
	@FXML private Label lifeLabel;
	@FXML private Label attLabel;
	@FXML private Label defLabel;
	@FXML private Slider lifeSlider;
	@FXML private Slider attSlider;
	@FXML private Slider defSlider;
	@FXML private Label addTime;
	@FXML private Label addDate;
	@FXML private ImageView imgSelectedPokemon;
	@FXML private Label type1;
	@FXML private Label type2;
	@FXML private Label nameSelectedPokemon;
	@FXML private ProgressBar attackProgressBar;
	@FXML private ProgressBar defenseProgressBar;
	@FXML private ProgressBar lifeProgressBar;
	@FXML private TextField lifeValue;
	@FXML private TextField attValue;
	@FXML private TextField defValue;

	private String time;
	// A regex that matches the pattern of an hour (user when we want to add a pokemon on the map).
	public static final Pattern HOUR_REGEX = Pattern.compile("^([0-9]{2}|[0-9]):[0-9]{2}:[0-9]{2}$", Pattern.CASE_INSENSITIVE);

	private boolean canceled;
	private String date;
/**
 * Initializes the elements in the view
 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		lifeSlider.setMin(0);
		lifeSlider.setMax(200);
		attSlider.setMin(0);
		attSlider.setMax(200);
		defSlider.setMin(0);
		defSlider.setMax(200);

		closeButton.setOnAction(e -> {
			canceled = true;
			((Stage) closeButton.getScene().getWindow()).close();
		});
		submitButton.setOnAction(e -> {
			((Stage) submitButton.getScene().getWindow()).close();
		});
		lifeSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				lifeValue.setText(Integer.toUnsignedString(new_val.intValue()));
				lifeProgressBar.setProgress(0.5*new_val.doubleValue() /100.0);
			}
		});
		attSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				attValue.setText(Integer.toUnsignedString(new_val.intValue()));
				attackProgressBar.setProgress(0.5*new_val.doubleValue() /100.0);
			}
		});
		defSlider.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov,
					Number old_val, Number new_val) {
				defValue.setText(Integer.toUnsignedString(new_val.intValue()));
				defenseProgressBar.setProgress(0.5*new_val.doubleValue() /100.0);
			}
		});

	}
	/**
	 * Sets the points to be displayed
	 * @param life
	 * @param att
	 * @param def
	 */
	public void setPoints(int life,int att,int def){
		attackProgressBar.setProgress(0.5*att /100.0);
		defenseProgressBar.setProgress(0.5*def /100.0);
		lifeProgressBar.setProgress(0.5*life /100.0);
		lifeValue.setText(Integer.toUnsignedString(life));
		attValue.setText(Integer.toUnsignedString(att));
		defValue.setText(Integer.toUnsignedString(def));
		lifeSlider.setValue(life);
		attSlider.setValue(att);
		defSlider.setValue(def);

	}
	/**
	 * Sets the name and id of the pokemon to be displayed
	 * @param name
	 * @param id
	 */
	public void setPokemonInfo(String name,int id){
		nameSelectedPokemon.setText(name);
		imgSelectedPokemon.setImage(ResourcesProvider.getPokemonImage(id - 1));
	}
	public boolean isCanceled() {
		return canceled;
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
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
	public int getLife(){
		return Integer.parseInt(lifeValue.getText());
	}
	public int getDefense(){
		return Integer.parseInt(defValue.getText());
	}
	public int getAttack(){
		return Integer.parseInt(attValue.getText());
	}
}
