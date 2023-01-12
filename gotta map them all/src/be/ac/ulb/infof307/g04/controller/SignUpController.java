package be.ac.ulb.infof307.g04.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import be.ac.ulb.infof307.g04.model.ResponseCode;
import be.ac.ulb.infof307.g04.model.client.UserCommunication;
import be.ac.ulb.infof307.g04.view.Message;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

/**
 * The controller which manages the sign up view.
 */
public class SignUpController implements Initializable {
	@FXML private Label titleSignUp;
	@FXML private Label labelCaptcha;

	@FXML private TextField usernameLabel;
	@FXML private TextField passwordLabel;
	@FXML private TextField confirmPasswordLabel;
	@FXML private TextField emailLabel;
	@FXML private TextField resultCaptcha;
	@FXML private HomeController parent;
	// A regex that matches the pattern of a simple email address.
	public static final Pattern EMAIL_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

	// The two operands of the captcha in the sign up view.
	// They are generated randomly in the initialize() method.
	private int operand1;
	private int operand2;

	/**
	 * This method is called by the FXMLLoader when initialization is complete.
	 * @param fxmlFileLocation URL.
	 * @param resources
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// Simulate text-shadow property
		DropShadow ds = new DropShadow();
		ds.setOffsetY(3.0f);
		ds.setColor(Color.color(0.4f, 0.4f, 0.4f));
		titleSignUp.setEffect(ds);
		// Generate a captcha
		operand1 = (int) (Math.random() * 10);
		operand2 = (int) (Math.random() * 10);
		labelCaptcha.setText("Question : Combien font " + operand1 + " + " + operand2 + " ? ");
	}

	/**
	 * Set the instance of the parent controller on the child controller
	 * @param parent Controller instance of the parent pane
	 */
	public void setParentController(HomeController homeController) {
		this.parent = homeController;
	}

	/**
	 * The event handler of the sign up button.
	 * this method signs up the client in the application.
	 * @param event The event.
	 * @throws IOException If a communication error occurred.
	 */
	@FXML
	private void signUp(ActionEvent event) throws IOException {
		try {
			if(isFormValid()) {
				UserCommunication request = new UserCommunication();
				int responseCode = request.signUp(usernameLabel.getText(), passwordLabel.getText(), emailLabel.getText());
				Message.show("", "response : " + responseCode, AlertType.ERROR);
				if(responseCode == ResponseCode.OK.getValue()) {
					// login success
					Message.show("Inscription effectuée avec succés !",
								  "Un lien de confirmation a été envoyé à l'adresse que vous "
								+ "avez spécifié afin d'activer votre compte.", AlertType.INFORMATION);
					parent.goToLogin();
				} else { 
					// login failed
					Message.show("response", "Inscription foirée", AlertType.INFORMATION);
				}
				// After one of the previous message, we reset the informations 
				// and we go to the login page.
				usernameLabel.clear();
				passwordLabel.clear();
				confirmPasswordLabel.clear();
				emailLabel.clear();
				resultCaptcha.clear();

			} else {
				Message.show("Erreur au niveau du formulaire",
						"Veuillez remplir le formulaire convenablement.", AlertType.WARNING);
			}
		} catch(NumberFormatException ex) {
			Message.show("Erreur au niveau du formulaire",
					"Veuillez remplir le formulaire convenablement.", AlertType.WARNING);
		}
	}
	

	/**
	 * Checks if the sign up form is valid of not.
	 * It is valid if all fields are filled and the answer to the captcha is correct.
	 * @return True if it's valid, false otherwise.
	 */
	private boolean isFormValid() {
		return (operand1 + operand2) == Integer.parseInt(resultCaptcha.getText())
				&& !usernameLabel.getText().equals("")
				&& !passwordLabel.getText().equals("")
				&& !confirmPasswordLabel.getText().equals("")
				&& passwordLabel.getText().equals(confirmPasswordLabel.getText())
				&& EMAIL_REGEX.matcher(emailLabel.getText()).find();
	}
	
	/**
	 * Shows the term of use.
	 * I am aware that it's hardcoded but... well...
	 * (In a professional use case, I would surely have placed that huge text in text file or somewhere else).
	 * @param e The event.
	 */
	@FXML
	private void showConditions(MouseEvent e) {
		Message.showScrollableMessage("Conditions d'utilisations",
				"Qu'est-ce que le Lorem Ipsum ?\n\n"
				+ "Le Lorem Ipsum est simplement du faux texte employé "
				+ "dans la composition et la mise en page avant impression. "
				+ "Le Lorem Ipsum est le faux texte standard de l'imprimerie "
				+ "depuis les années 1500, quand un peintre anonyme assembla "
				+ "ensemble des morceaux de texte pour réaliser un livre spécimen "
				+ "de polices de texte. Il n'a pas fait que survivre cinq siécles, "
				+ "mais s'est aussi adapté é la bureautique informatique, sans que "
				+ "son contenu n'en soit modifié. Il a été popularisé dans les années "
				+ "1960 gréce é la vente de feuilles Letraset contenant des passages du "
				+ "Lorem Ipsum, et, plus récemment, par son inclusion dans des applications "
				+ "de mise en page de texte, comme Aldus PageMaker."

				+ "Pourquoi l'utiliser ?\n\n"
				+ "On sait depuis longtemps que travailler avec du texte lisible "
				+ "et contenant du sens est source de distractions, et empéche de "
				+ "se concentrer sur la mise en page elle-méme. L'avantage du Lorem "
				+ "Ipsum sur un texte générique comme 'Du texte. Du texte. Du texte.' "
				+ "est qu'il posséde une distribution de lettres plus ou moins normale,"
				+ " et en tout cas comparable avec celle du franéais standard. "
				+ "De nombreuses suites logicielles de mise en page ou éditeurs de sites "
				+ "Web ont fait du Lorem Ipsum leur faux texte par défaut, et une "
				+ "recherche pour 'Lorem Ipsum' vous conduira vers de nombreux sites qui "
				+ "n'en sont encore qu'é leur phase de construction. Plusieurs versions "
				+ "sont apparues avec le temps, parfois par accident, souvent intentionnellement "
				+ "(histoire d'y rajouter de petits clins d'oeil, voire des phrases embarassantes)."

				+ "D'où vient-il ?\n\n"
				+ "Contrairement é une opinion répandue, le Lorem Ipsum n'est pas simplement "
				+ "du texte aléatoire. Il trouve ses racines dans une oeuvre de la littérature "
				+ "latine classique datant de 45 av. J.-C., le rendant vieux de 2000 ans. "
				+ "Un professeur du Hampden-Sydney College, en Virginie, s'est intéressé é un des "
				+ "mots latins les plus obscurs, consectetur, extrait d'un passage du Lorem Ipsum, "
				+ "et en étudiant tous les usages de ce mot dans la littérature classique, découvrit "
				+ "la source incontestable du Lorem Ipsum. Il provient en fait des sections 1.10.32 "
				+ "et 1.10.33 du \"De Finibus Bonorum et Malorum\" (Des Suprémes Biens et des Suprémes Maux) "
				+ "de Cicéron. Cet ouvrage, trés populaire pendant la Renaissance, est un traité "
				+ "sur la théorie de l'éthique. Les premiéres lignes du Lorem Ipsum, \"Lorem ipsum "
				+ "dolor sit amet...\", proviennent de la section 1.10.32."
				+ "L'extrait standard de Lorem Ipsum utilisé depuis le XVIé siécle "
				+ "est reproduit ci-dessous pour les curieux. Les sections 1.10.32 et 1.10.33 du "
				+ "\"De Finibus Bonorum et Malorum\" de Cicéron sont aussi reproduites dans leur "
				+ "version originale, accompagnée de la traduction anglaise de H. Rackham (1914)."

				+ "Oé puis-je m'en procurer ?\n\n"
				+ "Plusieurs variations de Lorem Ipsum peuvent étre trouvées ici ou lé, mais "
				+ "la majeure partie d'entre elles a été altérée par l'addition d'humour ou de "
				+ "mots aléatoires qui ne ressemblent pas une seconde é du texte standard. Si "
				+ "vous voulez utiliser un passage du Lorem Ipsum, vous devez étre sér qu'il n'y "
				+ "a rien d'embarrassant caché dans le texte. Tous les générateurs de Lorem Ipsum "
				+ "sur Internet tendent é reproduire le méme extrait sans fin, ce qui fait de "
				+ "lipsum.com le seul vrai générateur de Lorem Ipsum. Iil utilise un dictionnaire"
				+ " de plus de 200 mots latins, en combinaison de plusieurs structures de phrases, "
				+ "pour générer un Lorem Ipsum irréprochable. Le Lorem Ipsum ainsi obtenu ne "
				+ "contient aucune répétition, ni ne contient des mots farfelus, "
				+ "ou des touches d'humour.",
				AlertType.INFORMATION);
	}
}
