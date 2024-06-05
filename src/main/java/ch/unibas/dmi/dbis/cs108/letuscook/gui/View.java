package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import ch.unibas.dmi.dbis.cs108.letuscook.util.SanitizedLine;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.util.List;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Represents a generic view in the GUI.
 *
 * @param <T> The type of the pane associated with the view.
 */
abstract public class View<T extends Pane> {

	private static VBox messageContainer;
	private static ToggleButton chatButton;
	private static VBox chatBox;
	private static ScrollPane scrollPane;
	private static TextArea chatInput;
	private static VBox rightContainer;
	final Stage stage;
	final Views view;
	final Scene scene;
	final T pane;
	final String title;
	final Runnable returnButtonAction;
	private final Images returnButtonIcon;
	private final String returnButtonLabel;

	/**
	 * Constructs a new View with the specified parameters.
	 *
	 * @param stage               The JavaFX stage associated with the view.
	 * @param view                The type of view.
	 * @param pane                The pane associated with the view.
	 * @param width               The width of the scene.
	 * @param height              The height of the scene.
	 * @param title               The title of the view.
	 * @param backgroundUrlOrNull The background image for the view, or null.
	 */
	View(
		final Stage stage,
		final Views view,
		final T pane,
		final Double width,
		final Double height,
		final String title,
		final Image backgroundUrlOrNull,
		final Images returnButtonIcon,
		final Runnable returnButtonAction,
		final String returnButtonLabel
	) {
		this.stage = stage;
		this.view = view;
		this.pane = pane;
		this.scene = (width != null && height != null) ? new Scene(this.pane, width, height)
			: new Scene(this.pane);
		this.title = title;
		this.returnButtonIcon = returnButtonIcon;
		this.returnButtonAction = returnButtonAction;
		this.returnButtonLabel = returnButtonLabel;

		this.scene.getStylesheets().add(Resource.get("style.css"));

		if (backgroundUrlOrNull != null) {
			this.setBackground(backgroundUrlOrNull);
		}

		this.initializeChat();

		this.initializeSidebar();

		this.scene.setOnKeyReleased(event -> {
			switch (event.getCode()) {
				case T -> {
					if (ClientGUI.the().getActiveView() != null && this.view.showChat) {
						this.focusChat();
					}
				}
				case F1 -> {
					if (ClientGUI.the().getActiveView() != null && this.view.showChat) {
						this.toggleChat();
					}
				}
				case M -> Sounds.BACKGROUND.getMediaPlayer()
					.setMute(!Sounds.BACKGROUND.getMediaPlayer().isMute());
				case N -> Sounds.setMuteSoundEffects(!Sounds.getMuteSoundEffects());
			}

			event.consume();
		});
	}

	/**
	 * Constructs a new View with the specified parameters.
	 *
	 * @param stage               The JavaFX stage associated with the view.
	 * @param view                The type of view.
	 * @param pane                The pane associated with the view.
	 * @param title               The title of the view.
	 * @param backgroundUrlOrNull The URL of the background image for the view, or null.
	 */
	View(
		final Stage stage,
		final Views view,
		final T pane,
		final String title,
		final Image backgroundUrlOrNull,
		final Images returnButtonIcon,
		final Runnable returnButtonAction,
		final String returnButtonLabel
	) {
		this(stage, view, pane, null, null, title, backgroundUrlOrNull, returnButtonIcon,
			returnButtonAction, returnButtonLabel);
	}

	public static void clearChat() {
		messageContainer = null;
		chatButton = null;
		chatBox = null;
		scrollPane = null;
		chatInput = null;
		rightContainer = null;
	}

	public double getSidebarWidth() {
		return 0.1 * ClientGUI.the().width;
	}

	public double getChatWidth() {
		return 0.2 * ClientGUI.the().width;
	}

	public double getCenterWidth() {
		return ClientGUI.the().width - this.getSidebarWidth() - this.getChatWidth();
	}

	public Scene getScene() {
		return this.scene;
	}

	public void initializeChat() {
		if (!this.view.showChat) {
			return;
		}

		if (chatBox == null) {
			createChat();
		}

		((BorderPane) this.pane).setRight(rightContainer);
	}

	public void createChat() {
		rightContainer = new VBox();

		final double sceneHeight = ClientGUI.the().height;
		final double toggleHeight = 0.03 * sceneHeight;
		final double tipHeight = 0.08 * sceneHeight;
		final double inputHeight = 0.1 * sceneHeight;

		chatButton = new ToggleButton(null);
		chatButton.setSelected(false);
		chatButton.setPrefHeight(toggleHeight);
		chatButton.setOnAction(event -> {
			FadeTransition fadeTransition = new FadeTransition(Duration.millis(500), chatBox);
			fadeTransition.setNode(chatBox);
			fadeTransition.setFromValue(0.0);
			fadeTransition.setToValue(1.0);

			if (chatButton.isSelected()) {
				fadeTransition.setRate(-1);
				fadeTransition.play();
				chatButton.setBackground(new Background(new BackgroundImage(
					Images.get(Images.VISIBLE_SYMBOL),
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.CENTER, new BackgroundSize(
					BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));
				chatButton.setText(null);
			} else {
				fadeTransition.setRate(1);
				fadeTransition.play();
				chatButton.setBackground(new Background(new BackgroundImage(
					Images.get(Images.INVISIBLE_SYMBOL),
					BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT,
					BackgroundPosition.CENTER, new BackgroundSize(
					BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false))));
				chatButton.setText(null);
			}
		});
		this.toggleChat(); /* This is necessary. */
		this.toggleChat(); /* I know. */

		chatBox = new VBox();
		chatBox.setStyle("-fx-background-color: #222034");

		// Chat-Nachrichtenfenster
		if (messageContainer == null) {
			messageContainer = new VBox();
			var filler = new Region();
			VBox.setVgrow(filler, Priority.ALWAYS);
			messageContainer.getChildren().add(filler);
			messageContainer.setPrefHeight(1000000);
			messageContainer.setStyle("-fx-background-color: transparent");
			messageContainer.heightProperty()
				.addListener(
					(observable -> Platform.runLater(() -> scrollPane.setVvalue(1.0))));
		}

		scrollPane = new ScrollPane(messageContainer);
		scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent");
		Platform.runLater(() -> {
			try {
				scrollPane.lookup(".scroll-bar").setStyle("-fx-background-color: transparent;");
				scrollPane.lookup(".scroll-bar .thumb")
					.setStyle("-fx-background-color: transparent;");
				scrollPane.lookup(".scroll-bar .increment-button")
					.setStyle("-fx-background-color: transparent;");
				scrollPane.lookup(".scroll-bar .decrement-button")
					.setStyle("-fx-background-color: transparent;");
			} catch (NullPointerException ignored) {
			}
		});
		scrollPane.setFitToWidth(true);

		//Fokus auf den chatcontainer lenken
		ClientGUI.the().runLater(messageContainer::requestFocus);

		// Eingabefeld zum Chatten
		chatInput = Controls.textArea(this.getChatWidth(), inputHeight, "Message...", true,
			code -> {
				switch (code) {
					case ESCAPE -> {
						chatInput.getParent().requestFocus();
					}
					case ENTER -> {
						String message = chatInput.getText().trim();
						if (ClientGUI.the().getActiveView().view.alwaysYell) {
							message += "!";
						}
						if (!message.isEmpty()) {
							if (message.startsWith("@")) { // Whisper Chat Kürzel
								String[] whisperMessage = message.split(" ", 2);
								if (whisperMessage.length >= 2) {
									String recipient = whisperMessage[0].substring(1);
									String content = whisperMessage[1];
									Client.the().tryWhisperViaNickname(recipient, content);
								} else {
									Messenger.user("Malformed whisper message.");
								}
							} else if (message.endsWith("!")) { //Global Chat
								Client.the().tryYell(message.substring(0, message.length() - 1));
							} else {
								Client.the().tryChat(message); // nur in lobbys
							}
							chatInput.clear();
							if (!ClientGUI.the().getActiveView().view.keepChatFocused) {
								chatInput.getParent().requestFocus();
							}
						}
					}
				}
			}, SanitizedLine::canContain, 0);

		Label tip = new Label(
			"Use @name to whisper. If your message concerns everyone on the server, end it with an exclamation mark (!).");
		tip.setStyle("-fx-background-color: #18162a; -fx-text-fill: #524c83;");
		tip.setFont(Fonts.get(Fonts.UI_SECONDARY));
		tip.setPadding(new Insets(5, 10, 5, 10));
		tip.setMinHeight(tipHeight);
		tip.setWrapText(true);
		tip.setPrefWidth(this.getChatWidth());

		chatBox.setPrefWidth(this.getChatWidth());
		chatBox.setAlignment(Pos.CENTER_RIGHT);
		chatBox.getChildren().addAll(scrollPane, tip, chatInput);

		rightContainer.getChildren().addAll(chatButton, chatBox);
		rightContainer.setAlignment(Pos.CENTER_RIGHT);
	}

	public void focusChat() {
		if (chatInput != null) {
			chatInput.requestFocus();
		}
	}

	public void toggleChat() {
		if (chatButton != null) {
			chatButton.fire();
		}
	}

	/**
	 * Adds a message to the chat.
	 *
	 * @param kind      The kind of message.
	 * @param author    The author of the message.
	 * @param recipient The recipient of the message.
	 * @param message   The message content.
	 */
	public void addMessage(String kind, String author, String recipient, String message) {
		if (chatBox == null) {
			return;
		}

		String prefix = "";
		if (author != null) {
			prefix = author;
			if (recipient != null) {
				prefix += " > " + recipient;
			}
		}

		String style = "-fx-border-radius: 10; -fx-background-radius: 10; -fx-background-color: "
			+ switch (kind) {
			case "chat" -> "#5168C5" + (message.toLowerCase().contains("zaza")
				? "; -fx-background-color: linear-gradient(to right, red, orange, yellow, green, blue, indigo, violet)"
				: "");
			case "whisper" -> "#8C93AD";
			case "yell" ->
				"white; -fx-text-fill: black; -fx-border-width: 2px; -fx-border-color: #8a81bd; -fx-background-radius: 15;";
			case "user" -> "#c279e8";
			default -> "black";
		} + ";";

		Label messageLabel = new Label(message);
		messageLabel.setWrapText(true);
		messageLabel.setStyle(style);
		messageLabel.setFont(Fonts.get(Fonts.UI_PRIMARY));
		messageLabel.setPadding(new Insets(5, 10, 5, 10));

		Label authorLabel = new Label(prefix);
		authorLabel.setStyle("-fx-text-fill: white;");
		authorLabel.setFont(Fonts.get(Fonts.UI_SECONDARY));

		VBox messageNode = new VBox(authorLabel, messageLabel);
		messageNode.setAlignment(
			author == null ?
				Pos.CENTER
				: (author.equals(Client.the().getOwnActor().record().orElseThrow().getNickname()) ?
					Pos.CENTER_RIGHT :
					Pos.CENTER_LEFT));
		messageNode.setPadding(new Insets(0, 0, 10, 0));

		messageContainer.getChildren().add(messageNode);
	}

	public void initializeSidebar() {
		if (!this.view.showSidebar) {
			return;
		}

		List<Button> buttons;
		if (this.view.showFullSidebar) {
			buttons = List.of(
				Controls.sidebarButton(this.returnButtonIcon, this.returnButtonLabel,
					this.returnButtonAction),
				Controls.sidebarButton(Images.RECIPES, "Recipes",
					() -> ClientGUI.changeRequestedPopUpView(Views.RECIPES)),
				Controls.sidebarButton(Images.HIGHSCORE, "Highscores",
					() -> ClientGUI.changeRequestedPopUpView(Views.HIGHSCORES)),
				Controls.sidebarButton(Images.CHANGE_NICKNAME, "Nickname...",
					() -> ClientGUI.the().showNicknameDialog()),
//				Controls.sidebarButton(Images.WHISPER, "Whisper...",
//					() -> ClientGUI.the().showWhisperDialog()),
//				Controls.sidebarButton(Images.GLOBAL_CHAT, "Yell...",
//					() -> ClientGUI.the().showYellDialog()),
				Controls.sidebarButton(Images.SETTINGS, "Settings...",
					() -> ClientGUI.the().showSettingsDialog())
			);
		} else {
			buttons = List.of(
				Controls.sidebarButton(this.returnButtonIcon, this.returnButtonLabel,
					this.returnButtonAction)
			);
		}

		var sidebar = new VBox();
		sidebar.setPadding(new Insets(10, 0, 0, 5));
		sidebar.setSpacing(5);
		sidebar.setPrefWidth(this.getSidebarWidth());
		sidebar.getChildren().addAll(buttons);
		sidebar.setAlignment(Pos.TOP_LEFT);

		((BorderPane) this.pane).setLeft(sidebar);
	}

	private void setBackground(final Image backgroundImage) {
		this.pane.setBackground(
			new Background(new BackgroundImage(backgroundImage, null, null, null, null)));
	}

	final void select() {
		this.stage.setScene(this.scene);
		this.stage.setTitle(this.title);
	}

	void draw() {
	}
}
