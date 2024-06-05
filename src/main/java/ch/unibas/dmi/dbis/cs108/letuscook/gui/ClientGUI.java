package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Schedule;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.stage.Stage;

/**
 * Represents the graphical user interface for the Let Us Cook client application.
 */
public class ClientGUI extends Application {

	/**
	 * Frames per second.
	 */
	public static final int FPS = 30;

	private static final AtomicBoolean viewChanged = new AtomicBoolean(true);

	private static final AtomicBoolean popUpViewChanged = new AtomicBoolean(false);

	private static volatile ClientGUI the;

	private static volatile Views requestedView = Views.START;

	private static volatile Views requestedPopUpView = null;

	public double width = 0;

	public double height = 0;

	/**
	 * The current popup view.
	 */
	private View<?> popUpView = null;

	/**
	 * The current frame.
	 */
	private int frame = 0;

	private MediaPlayer musicPlayer;

	private MediaPlayer angrySoundPlayer;

	private MediaPlayer buttonSoundPlayer;

	private MediaPlayer interactSoundPlayer;

	private MediaPlayer expireSoundPlayer;

	private MediaPlayer updateSoundPlayer;

	/**
	 * The current view.
	 */
	private volatile View<?> activeView = null;

	/**
	 * The stage.
	 */
	private Stage stage;

	/**
	 * Create the client GUI.
	 */
	public ClientGUI() {
		assert !ClientGUI.exists();
	}

	public static ClientGUI the() {
		assert ClientGUI.exists() : "clientgui not initialized";

		return ClientGUI.the;
	}

	public static boolean exists() {
		return ClientGUI.the != null;
	}

	public static void changeRequestedView(Views requestedView) {
		ClientGUI.requestedView = requestedView;
		ClientGUI.viewChanged.set(true);
	}

	public static void changeRequestedPopUpView(Views requestedPopUpView) {
		ClientGUI.requestedPopUpView = requestedPopUpView;
		ClientGUI.popUpViewChanged.set(true);
	}

	public View<?> getActiveView() {
		return activeView;
	}

	public View<?> getPopUpView() {
		return popUpView;
	}

	private void setPopUpView(View<?> popUpview) {
		this.popUpView = popUpview;
	}

	/**
	 * Used by other threads to modify to GUI.
	 *
	 * @param runnable the code to run.
	 */
	void runLater(Runnable runnable) {
		if (ClientGUI.exists()) {
			Platform.runLater(runnable);
		}
	}

	/**
	 * Start the GUI.
	 *
	 * @param stage the primary stage for this application, created by JavaFX.
	 */
	@Override
	public void start(Stage stage) {
		ClientGUI.the = this;

		double a = Screen.getPrimary().getDpi() / (Screen.getPrimary().getOutputScaleX()
			* Screen.getPrimary().getBounds().getWidth());
		double b = 107d / (3 * 1280);
		double c = 1.3 * 0.75 * a / b;
		Fonts.SCALE = c;

		/* Music. */
		Sounds.BACKGROUND.play();

		/* Prepare stage. */
		this.stage = stage;
		this.stage.setOnCloseRequest(event -> System.exit(1));
		this.stage.getIcons().add(Images.get(Images.ICON));
		Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		this.stage.setX(bounds.getMinX());
		this.stage.setY(bounds.getMinY());
		this.stage.setWidth(bounds.getWidth());
		this.stage.setHeight(bounds.getHeight());
		this.stage.setMaximized(true);

		/* Show stage and measure scene dimensions. */
		var splashView = new SplashView(this.stage);
		this.setView(splashView);
		splashView.select();
		this.stage.show();
		this.scaleUnits();

		/* Start. */
		this.stage.widthProperty()
			.addListener((observable, oldValue, newValue) -> this.scaleUnits());
		this.stage.heightProperty()
			.addListener((observable, oldValue, newValue) -> this.scaleUnits());
		Schedule.atFixedRate(this::draw, 1000 / FPS, "draw");
	}

	/**
	 * Show a view.
	 *
	 * @param view the view.
	 */
	private void setView(View<?> view) {
		this.activeView = view;
	}

	private void scaleUnits() {
		this.width = this.getActiveView().getScene().getWidth();
		this.height = this.getActiveView().getScene().getHeight();

		double maxGameWidth =
			this.width - this.getActiveView().getSidebarWidth() - this.getActiveView()
				.getChatWidth();
		if (maxGameWidth > this.height) {
			Units.scale(this.height / Game.HEIGHT.u());
		} else {
			Units.scale(maxGameWidth / Game.WIDTH.u());
		}
	}

	/**
	 * Show a chat message.
	 *
	 * @param message the message.
	 */
	public void addMessage(String color, String author, String recipient, String message) {
		if (activeView != null) {
			this.runLater(() -> activeView.addMessage(color, author, recipient, message));
		}
	}

	/**
	 * Update the current view.
	 */
	private Object draw() {
		this.frame = (this.frame + 1) % FPS;

		boolean isZeroFrame = this.frame == 0;

		this.runLater(() -> {
			boolean viewChanged = ClientGUI.viewChanged.getAndSet(false);
			if (viewChanged) {
				this.setView(switch (requestedView) {
					case START -> new StartView(this.stage);
					case LOBBIES -> new LobbiesView(this.stage);
					case TUTORIAL -> new TutorialView(this.stage);
					case LOBBY -> new LobbyView(this.stage);
					case GAME -> new GameView(this.stage);
					default -> null;
				});
			}

			boolean popUpViewChanged = ClientGUI.popUpViewChanged.getAndSet(false);
			if (popUpViewChanged) {
				this.setPopUpView(requestedPopUpView == null ? null : switch (requestedPopUpView) {
					case HIGHSCORES -> new HighscoresView(stage);
					case RECIPES -> new RecipesView(stage);
					default -> null;
				});
			}

			if (popUpViewChanged && popUpView != null) {
				this.popUpView.select();
				this.scaleUnits();
			} else if (popUpView == null && (viewChanged) || popUpViewChanged) {
				this.activeView.select();
				this.scaleUnits();
			}

			if (this.popUpView != null && (popUpViewChanged || isZeroFrame)) {
				this.popUpView.draw();
			} else if (viewChanged || isZeroFrame || this.activeView instanceof KitchenView) {
				this.activeView.draw();
			}
		});

		return null;
	}

	public void showAlert(AlertType type, String title, String message, boolean wait) {
		Alert alert = new Alert(type);

		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.getDialogPane().lookup(".content.label")
			.setStyle("-fx-font-family: 'Arial'; -fx-font-size: 14px; -fx-text-fill: #333333;");

		this.showAlert(alert, wait);
	}

	public void showAlert(Alert alert, boolean wait) {
		assert Thread.currentThread().getName().equals("JavaFX Application Thread");

		DialogPane alertPane = alert.getDialogPane();
		Stage alertStage = (Stage) alertPane.getScene().getWindow();
		alertStage.getIcons().add(Images.get(Images.ICON));
		if (alertPane.getButtonTypes().get(0) == ButtonType.OK) {
			alertStage.close();
		}

		if (wait) {
			alert.showAndWait();
		} else {
			alert.show();
		}
	}

	public void showNicknameDialog() {
		TextInputDialog nicknameDialog = new TextInputDialog();

		nicknameDialog.setTitle("Choose a new nickname");
		nicknameDialog.setHeaderText(null);
		nicknameDialog.setContentText("Please enter your nickname:");

		DialogPane dialogPane = nicknameDialog.getDialogPane();
		dialogPane.setGraphic(new ImageView(Images.get(Images.ICON)));
		Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
		dialogStage.getIcons().add(Images.get(Images.ICON));

		nicknameDialog.showAndWait().ifPresent(result -> {
			if (!result.isEmpty()) {
				Client.the().tryNickname(result);
			} else {
				Alert alert = new Alert(Alert.AlertType.ERROR);
				alert.setTitle("Invalid nickname");
				alert.setHeaderText(null);
				alert.setContentText("Invalid nickname! Please enter a valid nickname!");

				this.showAlert(alert, false);
			}
		});
	}

	public void showWhisperDialog() {
		List<String> nicknames = new ArrayList<>();
		for (var actor : Client.the().getActors()) {
			nicknames.add(actor.record().orElseThrow().getNickname());
		}

		// ChoiceBox mit den Nicknamen
		ChoiceBox<String> nicknameChoiceBox = new ChoiceBox<>();
		nicknameChoiceBox.getItems().addAll(nicknames);

		// Dialog für den Whisper Chat
		Dialog<String> whisperDialog = new Dialog<>();
		whisperDialog.setTitle("Whisper Chat");
		whisperDialog.setHeaderText("Enter a Whisper Message");

		// Icon oben links
		DialogPane dialogPane = whisperDialog.getDialogPane();
		dialogPane.setGraphic(new ImageView(Images.get(Images.ICON)));
		Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
		dialogStage.getIcons().add(Images.get(Images.ICON));

		// Eingabefeld für die Nachricht
		TextArea whisperInputField = new TextArea();
		whisperInputField.setPromptText("Type your message here...");

		// Layout für den Dialog
		VBox dialogContent = new VBox(10);
		dialogContent.setPadding(new Insets(10));
		dialogContent.getChildren()
			.addAll(new Label("Select recipient"), nicknameChoiceBox, whisperInputField);
		whisperDialog.getDialogPane().setContent(dialogContent);

		// Buttons für den Dialog
		ButtonType sendButtonType = new ButtonType("Send", ButtonBar.ButtonData.OK_DONE);
		whisperDialog.getDialogPane().getButtonTypes()
			.addAll(sendButtonType, ButtonType.CANCEL);

		// Validiere die Eingabe
		whisperDialog.setResultConverter(dialogButton -> {
			if (dialogButton == sendButtonType) {
				return nicknameChoiceBox.getValue();
			}
			return null;
		});

		// überprüfen und command senden
		Optional<String> result = whisperDialog.showAndWait();
		result.ifPresent(recipient -> {
			String message = whisperInputField.getText().trim();
			if (!message.isEmpty()) {
				Client.the().tryWhisperViaNickname(recipient, message);
			}
		});
	}

	public void showYellDialog() {
		// Dialog für den globalen Chat
		Dialog<String> globalDialog = new Dialog<>();
		globalDialog.setTitle("Global Chat");
		globalDialog.setHeaderText("Talk with other Kitchens!");

		// Icon oben links
		DialogPane globalPane = globalDialog.getDialogPane();
		globalPane.setGraphic(new ImageView(Images.get(Images.ICON)));
		Stage globalStage = (Stage) globalPane.getScene().getWindow();
		globalStage.getIcons().add(Images.get(Images.ICON));

		// Eingabefeld für die Nachricht
		TextArea globalInputField = new TextArea();
		globalInputField.setPromptText("Type your message here...");

		// Layout für den Dialog
		VBox globalDialogContent = new VBox(10);
		globalDialogContent.setPadding(new Insets(10));
		globalDialogContent.getChildren()
			.addAll(new Label("Enter a Global Message"), globalInputField);
		globalDialog.getDialogPane().setContent(globalDialogContent);

		// Buttons für den Dialog
		ButtonType sendButtonType = new ButtonType("Send", ButtonData.OK_DONE);
		globalDialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

		// Überprüfen und command schicken
		Optional<String> resultOptional = globalDialog.showAndWait();
		if (resultOptional.isPresent()) {
			String message = globalInputField.getText().trim();
			if (!message.isEmpty()) {
				Sounds.CLICK.play();
				Client.the().tryYell(message);
			}
		}
	}

	public void showSettingsDialog() {
		Dialog<String> settingsDialog = new Dialog<>();
		settingsDialog.setTitle("Settings");
		settingsDialog.setHeaderText("Set your Kitchen!");

		// Icon oben links
		DialogPane settingsPane = settingsDialog.getDialogPane();
		settingsPane.setGraphic(new ImageView(Images.get(Images.ICON)));
		Stage settingsStage = (Stage) settingsPane.getScene().getWindow();
		settingsStage.getIcons().add(Images.get(Images.ICON));

		Label volumeLabel = new Label("Volume");
		Slider volumeSlider = new Slider(0, 100,
			Sounds.BACKGROUND.getMediaPlayer().getVolume() * 100);
		volumeSlider.setShowTickMarks(true);
		volumeSlider.setShowTickLabels(true);
		volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
			double volume = newValue.doubleValue()
				/ 100.0; // Slider-Value anpassen -> 0 to 1
			Sounds.BACKGROUND.getMediaPlayer().setVolume(volume);
		});

		CheckBox muteMusicCheckbox = new CheckBox("Mute Background Music");
		muteMusicCheckbox.setSelected(Sounds.BACKGROUND.getMediaPlayer().isMute());
		muteMusicCheckbox.setOnAction(
			e -> Sounds.BACKGROUND.getMediaPlayer().setMute(muteMusicCheckbox.isSelected()));

		CheckBox muteButtonSoundsCheckbox = new CheckBox("Mute Sound Effects");
		muteButtonSoundsCheckbox.setSelected(Sounds.getMuteSoundEffects());
		muteButtonSoundsCheckbox.setOnAction(
			e -> Sounds.setMuteSoundEffects(muteButtonSoundsCheckbox.isSelected()));

		GridPane grid = new GridPane();
		grid.add(volumeLabel, 0, 0);
		grid.add(volumeSlider, 1, 0);
		grid.add(muteMusicCheckbox, 0, 1, 2,
			1);
		grid.add(muteButtonSoundsCheckbox, 0, 2, 2, 1);

		settingsPane.setContent(grid);

		settingsPane.getButtonTypes().addAll(ButtonType.CLOSE);

		settingsDialog.showAndWait();
	}

	public void showLobbyDialog() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("New lobby");
		dialog.setHeaderText(null);
		dialog.setContentText("Please enter a name for your lobby:");

		DialogPane dialogPane = dialog.getDialogPane();
		Stage dialogStage = (Stage) dialogPane.getScene().getWindow();
		dialogPane.setGraphic(new ImageView(Images.get(Images.ICON)));
		dialogStage.getIcons().add(Images.get(Images.ICON));

		dialog.showAndWait().ifPresent(result -> {
			if (!result.isEmpty()) {
				if (Client.the().getOwnActor().member().isEmpty()) {
					Sounds.CLICK.play();
					Client.the().tryOpenLobby(result);
				}
			}
		});
	}
}
