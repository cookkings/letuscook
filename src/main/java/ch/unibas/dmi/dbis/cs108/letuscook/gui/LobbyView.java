package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.server.Actor;
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
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The lobby view of the Let Us Cook application, displaying lobby information and player list.
 */
class LobbyView extends View<BorderPane> {

	private final HBox memberList = new HBox(25);

	private final Map<Actor, VBox> actorNodeMap = new HashMap<>();

	private final Map<Actor, String> actorNicknameMap = new HashMap<>();

	private final Map<Actor, Boolean> actorReadyMap = new HashMap<>();

	private final HBox readyButtonContainer = new HBox();

	private Button readyButton = null;

	private ReadyState readyState = ReadyState.NOT_READY;

	private Label latestScore = null;

	/**
	 * Constructs the LobbyView with the specified stage.
	 *
	 * @param stage The stage for the view.
	 */
	LobbyView(Stage stage) {
		super(
			stage,
			Views.LOBBY,
			new BorderPane(),
			800d, 600d,
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			Images.LEAVE,
			() -> Client.the().tryLeaveLobby(),
			"Leave Lobby"
		);

		var name = new Label(
			Client.the().getOwnActor().member().orElseThrow().getLobby().getName());
		name.setFont(Fonts.get(Fonts.UI_TITLE));
		name.setTextFill(Color.WHITE);
		name.setPadding(new Insets(0, 0, 50, 0));

		this.latestScore = new Label();
		latestScore.setWrapText(true);
		latestScore.setFont(Fonts.get(Fonts.UI_PRIMARY));
		latestScore.setStyle("-fx-text-fill: white;");
		latestScore.setPadding(new Insets(0, 0, 50, 0));

		var overview = new VBox(50, name, this.latestScore, memberList, readyButtonContainer);
		overview.setAlignment(Pos.CENTER);
		this.pane.setCenter(overview);
	}

	@Override
	void draw() {
		/* Ensure the client is currently in a lobby. */
		var memberOrEmpty = Client.the().getOwnActor().member();
		if (memberOrEmpty.isEmpty()) {
			return;
		}

		/*
		 * Update the score.
		 */
		var latestScoreOrEmpty = memberOrEmpty.get().getLobby().getClientLatestScore();
		latestScoreOrEmpty.ifPresent(
			score -> this.latestScore.setText("Game over! You scored " + score + " points!"));

		/*
		 * Update the member list.
		 */
		var actors = memberOrEmpty.orElseThrow().getLobby().getActors();

		/* Drop actors that left the lobby. */
		actorNodeMap.keySet().retainAll(List.of(actors));
		actorReadyMap.keySet().retainAll(List.of(actors));
		actorNicknameMap.keySet().retainAll(List.of(actors));

		for (Actor actor : actors) {
			var member = actor.member();
			var record = actor.record();
			if (member.isEmpty() || record.isEmpty()) {
				continue;
			}
			var existingNode = actorNodeMap.get(actor);

			int nodeIndex = -1; /* If this actor is new, we'll add a new node for it instead of replacing the old one. */
			if (existingNode != null) {
				var readyStateHasChanged = member.get().isReady() != actorReadyMap.get(actor);

				var nameHasChanged = !record.get().getNickname()
					.equals(actorNicknameMap.get(actor));

				if (readyStateHasChanged || nameHasChanged) {
					/* If we've already recorded this actor, but its state has changed, grab the node index to replace it. */
					nodeIndex = this.memberList.getChildren().indexOf(existingNode);
				} else {
					/* If we've already recorded this actor and its state is unchanged, skip it. */
					continue;
				}
			}

			/* Create the presence. */
			boolean ready = member.get().isReady();
			actorReadyMap.put(actor, ready);
			ImageView model = new ImageView(
				Images.get(ready ? Images.MEMBER_READY : Images.MEMBER_ASLEEP));

			String nickname = record.get().getNickname();
			actorNicknameMap.put(actor, nickname);
			Label nameTag = new Label(nickname);
			nameTag.setFont(Fonts.get(Fonts.UI_PRIMARY));
			nameTag.setTextFill(Color.WHITE);

			var presence = new VBox(25);
			presence.setAlignment(Pos.CENTER);
			presence.getChildren().addAll(model, nameTag);
			actorNodeMap.put(actor, presence);

			/* Add or replace the node. */
			if (nodeIndex == -1) {
				this.memberList.getChildren().add(presence);
			} else {
				this.memberList.getChildren().set(nodeIndex, presence);
			}
		}
		this.memberList.getChildren().retainAll(this.actorNodeMap.values());
		this.memberList.setAlignment(Pos.CENTER);

		/*
		 * 'Ready' button.
		 */
		var newReadyState =
			memberOrEmpty.get().getLobby().isEveryoneReady() ? ReadyState.EVERYONE_READY
				: memberOrEmpty.get().isReady() ? ReadyState.READY : ReadyState.NOT_READY;
		if (this.readyButton == null || newReadyState != this.readyState) {
			this.readyState = newReadyState;
			this.readyButton = Controls.primaryButton(
				readyState != ReadyState.EVERYONE_READY ? "Ready" : "Start",
				readyState == ReadyState.READY ? null : () -> {
					if (Client.the().getOwnActor().member().isPresent()) {
						Sounds.CLICK.play();
						Client.the().sendReady();
						Client.the().tryStartGame();
					}
				});
			this.readyButtonContainer.getChildren().clear();
			this.readyButtonContainer.getChildren().add(readyButton);
			this.readyButtonContainer.setAlignment(Pos.CENTER);
		}
	}

	private enum ReadyState {
		NOT_READY,
		READY,
		EVERYONE_READY,
		;
	}
}
