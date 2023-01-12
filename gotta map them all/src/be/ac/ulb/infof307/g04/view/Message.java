package be.ac.ulb.infof307.g04.view;

import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class lets us pop up some alert box.
 */
public class Message {
	private static final ImageView imgLogo    = new ImageView(ResourcesProvider.LOGO_64_IMAGE);
	private static final ImageView imgPikachu = new ImageView(ResourcesProvider.PIKACHU_IMAGE);
	private static final ImageView imgPikachuError = new ImageView(ResourcesProvider.PIKACHU_IMAGE_SAD);
	
	/**
	 * Shows a message.
	 * @param title Title of the message.
	 * @param content Content of the message.
	 * @param type  Type of the message.
	 */
	public static void show(String title, String content, AlertType type) {
		Alert alert = new Alert(AlertType.WARNING);
		
		switch(type) {
			case INFORMATION : alert.setGraphic(imgPikachu); break;
			case ERROR       : alert.setGraphic(imgPikachuError); break;
			default          : alert.setGraphic(imgLogo); break;
		}

		alert.setTitle(title);
		alert.setHeaderText(content);
		alert.showAndWait();
	}

	/**
	 * Shows a scrollable message. Typically, when you have a lot of things to say, you can use this static method.
	 * This method is used to displays the terms of use of the application.
	 * @param title Title of the message.
	 * @param content Content of the message.
	 * @param type  Type of the message.
	 */
	public static void showScrollableMessage(String title, String content, AlertType type) {
		// TODO Auto-generated method stub
		Alert alert = new Alert(type);
		
		if (type == AlertType.INFORMATION)
			alert.setGraphic(imgPikachu);
		else
			alert.setGraphic(imgLogo);
		
		alert.setTitle(title);
		alert.setHeaderText(title);
		alert.setContentText("Cliquez sur \"Afficher plus de détails\" pour voir les conditions.");
		
		Label label = new Label("Conditions d'utilisation : ");
		TextArea textArea = new TextArea(content);
		textArea.setEditable(false);
		textArea.setWrapText(true);
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);

		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setContent(expContent);
		
		alert.showAndWait();
	}
	
	/**
	 * Shows a more custom message. If you want to show a complex message 
	 * (typically a dialog pane), then you can use this static method.
	 * @param title			The title of the message
	 * @param customContent The content of the message (notice that it's a {@link Pane} and not a {@link String})
	 * @param decorated		Indicates if the dialog is decorated or not. 
	 * (By decorated, we mean the little bar on the top of a window with the close/minimize/maximize buttons)
	 */
	public static void showCustom(String title, Pane customContent, boolean decorated) {
		Stage window = new Stage();
		window.initModality(Modality.APPLICATION_MODAL);
		
		if(!decorated)
			window.initStyle(StageStyle.UNDECORATED);
		
		window.setTitle(title);
		AnchorPane root = new AnchorPane();
		Scene scene = new Scene(root);
		scene.setFill(null);

		root.getChildren().add(customContent);
		window.setScene(scene);
		window.showAndWait();
	}
}
