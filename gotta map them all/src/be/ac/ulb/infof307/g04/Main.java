package be.ac.ulb.infof307.g04;

import java.io.IOException;

import be.ac.ulb.infof307.g04.controller.HomeController;
import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import be.ac.ulb.infof307.g04.model.client.UserCommunication;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {    	
    	// Initialize all resources in a thread in order to not freeze the GUI when we ask for images.
		ResourcesProvider.initMiniatures();
		ResourcesProvider.initImages();
        launch(args);
    }

    /**
     * Starts the application.
     */
    @Override 
    public void start(Stage primaryStage) throws IOException {
    	FXMLLoader fxmlLoader = new FXMLLoader();
        Pane home = fxmlLoader.load(Main.class.getResource(ResourcesProvider.HOME_PATH).openStream());
        HomeController homeController = ((HomeController) fxmlLoader.getController());
		Scene scene = new Scene(home);
        primaryStage.getIcons().add(ResourcesProvider.LOGO_IMAGE);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Gotta Map'Em all !");

        primaryStage.setOnCloseRequest(event -> {
			if(homeController.getClient() != null){
				UserCommunication clientCom = new UserCommunication();
				clientCom.logout(homeController.getClient().getTokenSession(),
						homeController.getClient().getUser().getUsername(),homeController.getClient().getUser().getUsername());
			}
		});
        primaryStage.show();
    }
}

