package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Actor;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Lobby;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Represents the graphical user interface (GUI) for displaying available lobbies in the Let Us Cook
 * application. Users can join existing lobbies or create new ones through this interface.
 */
class LobbiesView extends View<BorderPane> {

	private final Label welcome;

	private final Map<Actor, Label> actorLabelMap = new HashMap<>();

	private final VBox actorList;

	/**
	 * The container for holding lobby buttons.
	 */
	private final VBox lobbyList;

	/**
	 * Mapping between lobbies and their corresponding buttons.
	 */
	private final Map<Lobby, Button> lobbyNodeMap = new HashMap<>();

	private final Map<Lobby, Boolean> lobbyGameIsRunningMap = new HashMap<>();

	/**
	 * Constructs a new {@code LobbiesView} with the specified main stage.
	 *
	 * @param stage the main stage of the application
	 */
	LobbiesView(Stage stage) {
		super(
			stage,
			Views.LOBBIES,
			new BorderPane(),
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			Images.LEAVE,
			() -> Client.the().tryDisconnect(),
			"Disconnect"
		);

		/*
		 * Left-hand side.
		 */

		var leftHandSide = new VBox();
		leftHandSide.setPrefWidth(this.getCenterWidth() / 2);
		leftHandSide.setPadding(new Insets(100));

		this.welcome = new Label();
		welcome.setWrapText(true);
		welcome.prefWidthProperty().bind(leftHandSide.widthProperty());
		welcome.setFont(Fonts.get(Fonts.UI_TITLE));
		welcome.setStyle("-fx-text-fill: white;");
		welcome.setPadding(new Insets(0, 0, 50, 0));

		Label introduction = new Label(
			"Welcome to Let Us Cook! Use the sidebar to your left to change your nickname, navigate through menus, and access additional information. To start playing, create a new lobby or join an existing one.\n\nIf this is your first time here, check out the tutorial to learn the basics:");
		introduction.setWrapText(true);
		introduction.prefWidthProperty().bind(leftHandSide.widthProperty());
		introduction.setFont(Fonts.get(Fonts.UI_PRIMARY));
		introduction.setStyle("-fx-text-fill: white;");
		introduction.setPadding(new Insets(0, 0, 50, 0));

		var tutorial = Controls.primaryButton("Play Tutorial",
			() -> ClientGUI.changeRequestedView(Views.TUTORIAL));
		var tutorialBox = new HBox(tutorial);
		tutorialBox.setAlignment(Pos.CENTER);

		Label online = new Label("Online:");
		online.setFont(Fonts.get(Fonts.UI_PRIMARY));
		online.setStyle("-fx-text-fill: white;");
		online.setPadding(new Insets(50, 0, 25, 0));

		this.actorList = new VBox(10);

		leftHandSide.getChildren()
			.addAll(this.welcome, introduction, tutorialBox, online, this.actorList);
		leftHandSide.setAlignment(Pos.TOP_LEFT);

		/*
		 * Right-hand side.
		 */

		var rightHandSide = new VBox();
		rightHandSide.setPrefWidth(this.getCenterWidth() / 2);
		rightHandSide.setPadding(new Insets(100));

		Label lobbies = new Label("Get to cooking!");
		lobbies.setWrapText(true);
		lobbies.prefWidthProperty().bind(leftHandSide.widthProperty());
		lobbies.setFont(Fonts.get(Fonts.UI_TITLE));
		lobbies.setStyle("-fx-text-fill: white;");
		lobbies.setPadding(new Insets(0, 0, 50, 0));

		var open = Controls.primaryButton("New Lobby...", () -> ClientGUI.the().showLobbyDialog());
		open.setPadding(new Insets(0, 0, 50, 0));

		Label openLobbies = new Label("Open lobbies:");
		openLobbies.setWrapText(true);
		openLobbies.prefWidthProperty().bind(leftHandSide.widthProperty());
		openLobbies.setFont(Fonts.get(Fonts.UI_PRIMARY));
		openLobbies.setStyle("-fx-text-fill: white;");
		openLobbies.setPadding(new Insets(0, 0, 25, 0));

		this.lobbyList = new VBox(5);

		rightHandSide.getChildren().addAll(lobbies, open, openLobbies, this.lobbyList);
		rightHandSide.setAlignment(Pos.TOP_CENTER);

		/*
		 * Center.
		 */

		this.pane.setCenter(new HBox(leftHandSide, rightHandSide));

		var superOnKeyReleased = this.scene.getOnKeyReleased();
		this.scene.setOnKeyReleased(event -> {
			if (event.getCode() == KeyCode.O) {
				ClientGUI.the().showLobbyDialog();
			}
			superOnKeyReleased.handle(event);
		});
	}

	/**
	 * Updates the display of lobbies.
	 */
	@Override
	void draw() {
		if (Client.the().hasOwnActor() && Client.the().getOwnActor().record().isPresent()) {
			this.welcome.setText(
				"Welcome, " + Client.the().getOwnActor().record().orElseThrow().getNickname()
					+ "!");
		}

		/*
		 * Update the actors.
		 */
		var actors = Client.the().getActors();

		/* Drop actors that no longer exist. */
		this.actorLabelMap.keySet().retainAll(List.of(actors));

		for (var actor : actors) {
			var recordOrEmpty = actor.record();
			if (recordOrEmpty.isEmpty()) {
				continue;
			}

			Label name = this.actorLabelMap.get(actor);

			/* Create a new label. */
			if (name == null) {
				name = new Label();
				name.setFont(Fonts.get(Fonts.UI_PRIMARY));
				name.setTextFill(Color.WHITE);

				this.actorLabelMap.put(actor, name);
				this.actorList.getChildren().add(name);
			}

			/* Update the label. */
			if (!name.getText().equals(recordOrEmpty.get().getNickname())) {
				name.setText(recordOrEmpty.get().getNickname());
			}
		}
		this.actorList.getChildren().retainAll(this.actorLabelMap.values());
		this.actorList.setAlignment(Pos.CENTER);

		/*
		 * Update the lobby list.
		 */
		var lobbies = Client.the().getLobbies();

		/* Drop lobbies that no longer exist. */
		this.lobbyNodeMap.keySet().retainAll(List.of(lobbies));
		this.lobbyGameIsRunningMap.keySet().retainAll(List.of(lobbies));

		for (var lobby : lobbies) {
			var existingNode = lobbyNodeMap.get(lobby);

			int nodeIndex = -1; /* If this lobby is new, we'll add a new node for it instead of replacing the old one. */
			if (existingNode != null) {
				var gameStateHasChanged = lobby.gameIsRunning() != lobbyGameIsRunningMap.get(lobby);

				if (gameStateHasChanged) {
					/* If we've already recorded this lobby, but its state has changed, grab the node index to replace it. */
					nodeIndex = this.lobbyList.getChildren().indexOf(existingNode);
				} else {
					/* If we've already recorded this lobby and its state is unchanged, skip it. */
					continue;
				}
			}

			/* Create the join button. */
			boolean gameIsRunning = lobby.gameIsRunning();
			lobbyGameIsRunningMap.put(lobby, gameIsRunning);
			var join = Controls.bigButton(
				gameIsRunning ? Images.LOBBY_BUTTON_ACTIVE : Images.LOBBY_BUTTON_IDLE,
				-10,
				lobby.getName(), () -> {
					Sounds.CLICK.play();
					Client.the().tryJoinLobby(lobby.getName());
				});
			lobbyNodeMap.put(lobby, join);

			/* Add or replace the node. */
			if (nodeIndex == -1) {
				this.lobbyList.getChildren().add(join);
			} else {
				this.lobbyList.getChildren().set(nodeIndex, join);
			}
		}
		this.lobbyList.getChildren().retainAll(this.lobbyNodeMap.values());
		this.lobbyList.setAlignment(Pos.CENTER);
	}
}
