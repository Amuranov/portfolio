package be.ac.ulb.infof307.g04.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import be.ac.ulb.infof307.g04.controller.HomeController.TypeMenu;
import be.ac.ulb.infof307.g04.model.dao.SearchFavoriteDAO;
import be.ac.ulb.infof307.g04.model.SearchFavorite;
import be.ac.ulb.infof307.g04.model.client.Session;
import be.ac.ulb.infof307.g04.model.client.UserCommunication;
import be.ac.ulb.infof307.g04.view.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;

/**
 * The controller which manages the login view.
 */
public class LoginController implements Initializable {

	@FXML private TextField      username;
	@FXML private PasswordField  password;
	@FXML private AnchorPane     containerLogin;
	@FXML private HomeController parent;

	/**
	 * This method is called by the FXMLLoader when initialization is complete.
	 * @param fxmlFileLocation URL.
	 * @param resources
	 */
	@Override
	public void initialize(URL arg, ResourceBundle rb) { }

	/**
	 * Set the instance of the parent controller on the child controller
	 * @param parent Controller instance of the parent pane
	 */
	public void setParentController(HomeController parent) {
		this.parent = parent;
	}

	/**
	 * Event handler of the login button.
	 * @param event the event.
	 * @throws IOException if a problem occured in the communication 
	 * 					   between the client and the server.
	 */
	@FXML
	private void onLogin(ActionEvent event) throws IOException {
		if (!username.getText().equals("") && !password.getText().equals("")) {
			UserCommunication clientCom = new UserCommunication();
			Session client = clientCom.login(username.getText(), password.getText());
			
			if(client != null) { 
				// login good
				parent.createSession(client);
				parent.populateProfileDatas();
				parent.goToHome();
				parent.changeContextualMenu(TypeMenu.CONNECTED);
				username.clear();
				password.clear();
				
				// load favorites
				ArrayList<SearchFavorite> sf = new SearchFavoriteDAO().findByUserId(client.getUser().getID());
				parent.setFavorites(sf);
				parent.populateSearcheScrollPane();
				
				
			} else {
				// login failed
				Message.show("Erreur", "Authentification échouée", AlertType.INFORMATION);
			}
		} else {
			Message.show("Erreur au niveau du formulaire", "Veuillez remplir le formulaire convenablement.", AlertType.WARNING);
		}
	}
}
