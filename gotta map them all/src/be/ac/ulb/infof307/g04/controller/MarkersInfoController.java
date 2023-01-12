package be.ac.ulb.infof307.g04.controller;

import java.net.URL;
import java.util.ResourceBundle;

import be.ac.ulb.infof307.g04.model.ResourcesProvider;
import be.ac.ulb.infof307.g04.model.client.LikesStatCommunication;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MarkersInfoController implements Initializable{

	@FXML private AnchorPane infoContainer;
	@FXML private ImageView pokemonImg;
	@FXML private Label user;
	@FXML private Label date;
	@FXML private Label time;
	@FXML private Label pokemonName;
	@FXML private Label pv;
	@FXML private Label attackPoint;
	@FXML private Label defensePoint;
	@FXML private ImageView likeView;
	@FXML private ImageView dislikeView;
	@FXML private Button closeButton;
	@FXML private Label likeNumbers;
	@FXML private Label dislikeNumbers;
	@FXML private ProgressBar pvProgressBar;
	@FXML private ProgressBar atkProgressBar;
	@FXML private ProgressBar defProgressBar;

	private int signalisation_id;
	private boolean isAuth;
	private int userId;
	private String pathinfo;
	private String userinfo;
	private String dateinfo;
	private String timeinfo;
	private String pokemonNameinfo;
	private int pvinfo;
	private int attackPointinfo;
	private int defensePointinfo;

	private LikesStatCommunication likesStatCommunication;
	private int userLikeType;

	public MarkersInfoController(int signalisation_id, boolean isAuth, HomeController parent, String pathinfo, String userinfo, String dateinfo, String timeinfo,
			String pokemonNameinfo, int pvinfo, int attackPointinfo, int defensePointinfo) {
		super();
		this.signalisation_id = signalisation_id;
		this.isAuth = isAuth;
		if(isAuth){
			this.userId = parent.getClient().getUser().getID();
		}
		this.pathinfo = pathinfo;
		this.userinfo = userinfo;
		this.dateinfo = dateinfo;
		this.timeinfo = timeinfo;
		this.pokemonNameinfo = pokemonNameinfo;
		this.pvinfo = pvinfo;
		this.attackPointinfo = attackPointinfo;
		this.defensePointinfo = defensePointinfo;
		likesStatCommunication = new LikesStatCommunication();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Image image = new Image(pathinfo);
		this.pokemonImg.setImage(image);
		this.user.setText(userinfo);
		this.date.setText(dateinfo);
		this.time.setText(timeinfo);
		this.pokemonName.setText(pokemonNameinfo);
		this.pv.setText(""+pvinfo);
		this.attackPoint.setText(""+attackPointinfo);
		this.defensePoint.setText(""+defensePointinfo);
		this.pvProgressBar.setProgress(0.5*pvinfo /100.0);
		this.atkProgressBar.setProgress(0.5*attackPointinfo /100.0);
		this.defProgressBar.setProgress(0.5*defensePointinfo /100.0);		
		updateProgressColor(pvProgressBar, atkProgressBar, defProgressBar);
		initializePokemonLikesStat();

		likeView.setPickOnBounds(true);
		dislikeView.setPickOnBounds(true);

		closeButton.setOnAction(e -> {
			((Stage)closeButton.getScene().getWindow()).close();
		});
	}

	
	
	/**
	 * initialize view information of the clicked signaling
	 */
	private void initializePokemonLikesStat(){
		updateStatView();
		if(!isAuth) {
			likeView.setImage(ResourcesProvider.LIKE);
			dislikeView.setImage(ResourcesProvider.DISLIKE);
		} else {
			userLikeType = Integer.parseInt(likesStatCommunication.userLikeType(userId, signalisation_id));
			if(userLikeType == 0) {
				likeView.setImage(ResourcesProvider.LIKE_BL);
				dislikeView.setImage(ResourcesProvider.DISLIKE_BL);
			} else {
				if(userLikeType == 1) {
					likeView.setImage(ResourcesProvider.LIKE);
					dislikeView.setImage(ResourcesProvider.DISLIKE_BL);
				} else {
					likeView.setImage(ResourcesProvider.LIKE_BL);
					dislikeView.setImage(ResourcesProvider.DISLIKE);
				}
			}
			likeView.setOnMouseClicked(e -> {
				likeClicked();
	        });
			dislikeView.setOnMouseClicked(e -> {
				dislikeClicked();
	        });
		}
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
	
	private void likeClicked(){
		if(isAuth){
			if(userLikeType == 0 || userLikeType == -1){
				likesStatCommunication.insert(userId, signalisation_id, 1);
				likeView.setImage(ResourcesProvider.LIKE);
				if(userLikeType == -1){
					likesStatCommunication.delete(userId, signalisation_id, -1);
					dislikeView.setImage(ResourcesProvider.DISLIKE_BL);
				}
				userLikeType = 1;
			}else{
				likesStatCommunication.delete(userId, signalisation_id, 1);
				likeView.setImage(ResourcesProvider.LIKE_BL);
				userLikeType = 0;
			}
			updateStatView();
		}
	}

	private void dislikeClicked(){
		if(isAuth){
			if(userLikeType == 0 || userLikeType == 1){
				likesStatCommunication.insert(userId, signalisation_id, -1);
				dislikeView.setImage(ResourcesProvider.DISLIKE);
				if(userLikeType == 1){
					likesStatCommunication.delete(userId, signalisation_id, 1);
					likeView.setImage(ResourcesProvider.LIKE_BL);
				}
				userLikeType = -1;
			}else{
				likesStatCommunication.delete(userId, signalisation_id, -1);
				dislikeView.setImage(ResourcesProvider.DISLIKE_BL);
				userLikeType = 0;
			}
			updateStatView();
		}
	}

	/**
	 * update likes and dislikes numbers of the signaling
	 */
	private void updateStatView(){
		likeNumbers.setText(""+likesStatCommunication.getLikes(signalisation_id));
		dislikeNumbers.setText(""+likesStatCommunication.getDislikes(signalisation_id));
	}

}
