package be.ac.ulb.infof307.g04.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 * The controller which manages the profile view.
 */
public class ProfileController implements Initializable {

	@FXML private TextField username;
	@FXML private TextField email;
	@FXML private StackPane containerProfile;
	@FXML private HomeController parent;
	@FXML private ImageView modifyEmail;
	@FXML private ImageView modifyUsername;
	@FXML private Button    save;

	/**
	 * This method is called by the FXMLLoader when initialization is complete.
	 * @param arg0 URL.
	 * @param arg1 resources
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		username.setText("username from server");
		email.setText("email from server");
		email.setDisable(true);
		username.setDisable(true);
	}

	/**
	 * /**
	 * Set the instance of the parent controller on the child controller
	 * @param homeController Controller
	 */
	public void setParentController(HomeController homeController) {
		parent = homeController;
	}

	/**
	 * @return the name currently on the user name textfield
	 */
	public String getUsername() {
		return username.getText();
	}

	/**
	 * Change the value of the user name textfield
	 * @param value the new user name
	 */
	public void setUsername(String value) {
		username.setText(value);
	}

	/**
	 * @return the email currently on the email textfield
	 */
	public String getEmail() {
		return email.getText();
	}

	/**
	 * Change the value of the email textfield
	 * @param value the new email
	 */
	public void setEmail(String value) {
		email.setText(value);
	}
}
