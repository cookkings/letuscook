package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.MalformedException;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedName;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.util.Optional;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * The start view of the Let Us Cook application, displaying welcome message and login options.
 */
class StartView extends View<VBox> {

	/**
	 * Constructs the StartView with the specified stage.
	 *
	 * @param stage The stage for the view.
	 */
	StartView(Stage stage) {
		super(
			stage,
			Views.START,
			new VBox(),
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			null,
			null,
			null
		);

		/*
		 * Logo.
		 */
		var logo = new ImageView(Images.get(Images.LOGO));
		logo.setPreserveRatio(true);
		logo.setFitHeight(200);

		/*
		 * Subtitle.
		 */
		Label subtitle = new Label(
			"Choose a name:");
		subtitle.setWrapText(true);
		subtitle.setFont(Fonts.get(Fonts.UI_PRIMARY));
		subtitle.setStyle("-fx-text-fill: white;");
		subtitle.setPadding(new Insets(0, 0, 0, 0));

		/*
		 * Nickname field.
		 */
		var nickname = Controls.textArea(new Units(2.5).px(), new Units(0.4).px(),
			Client.the().getLoginNickname().toString(),
			false, null, SanitizedName::canContain, SanitizedName.MAX_LENGTH);

		/*
		 * Start button.
		 */
		var start = Controls.primaryButton("Let's Go!", () -> {
			nickname.requestFocus();
			Sounds.CLICK.play();
			try {
				Client.the().setLoginNickname(
					SanitizedName.createOrThrow(nickname.getText()));
			} catch (MalformedException e) {
				Messenger.warn("Ignored Malformed login nickname");
			}
			Client.the().tryConnect();
		});

		/*
		 * Exit button.
		 */
		var exit = Controls.secondaryButton("Quit Game", () -> {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
			alert.setTitle("Exiting Let Us Cook!");
			alert.setHeaderText(null);
			alert.setContentText("Exit the game?");

			ButtonType exitYesButton = new ButtonType("OK");
			ButtonType exitNoButton = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);
			alert.getButtonTypes().setAll(exitYesButton, exitNoButton);

			Optional<ButtonType> result = alert.showAndWait();
			if (result.isPresent() && result.orElseThrow() == exitYesButton) {
				System.exit(1);
			}
		});

		var nameAndStartBox = new VBox(25, subtitle, nickname, start);
		nameAndStartBox.setAlignment(Pos.CENTER);

		var buttons = new VBox(50, nameAndStartBox, exit);
		buttons.setAlignment(Pos.CENTER);

		var vbox = new VBox(75, logo, buttons);
		vbox.prefHeightProperty().bind(this.getScene().heightProperty());
		vbox.setAlignment(Pos.CENTER);
		this.pane.getChildren().addAll(vbox);
	}
}
