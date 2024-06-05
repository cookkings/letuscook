package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import javafx.stage.Stage;

/**
 * Represents the view for the game in the Let Us Cook application.
 */
public class GameView extends KitchenView {

	/**
	 * Creates a new instance of GameView.
	 *
	 * @param stage The primary stage.
	 */
	public GameView(Stage stage) {
		super(
			stage,
			Views.GAME,
			Images.LEAVE,
			() -> Client.the().tryLeaveLobby(),
			"Leave Game"
		);

		var superOnKeyReleased = this.scene.getOnKeyReleased();
		this.scene.setOnKeyReleased(event -> {
			switch (event.getCode()) {
				case E, SPACE -> {
					Client.the().tryInteract();
					Sounds.INTERACT.play();
				}
			}

			superOnKeyReleased.handle(event);
		});
	}
}
