package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.client.Client;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * Represents the view for displaying highscores in the Let Us Cook application.
 */
public class HighscoresView extends View<BorderPane> {

	/**
	 * Creates a new instance of HighscoresView.
	 *
	 * @param stage The primary stage.
	 */
	HighscoresView(Stage stage) {
		super(stage,
			Views.HIGHSCORES,
			new BorderPane(),
			800d, 600d,
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			Images.RETURN,
			() -> ClientGUI.changeRequestedPopUpView(null),
			"Back"
		);

		var title = new Label("Highscores");
		title.setFont(Fonts.get(Fonts.UI_TITLE));
		title.setTextFill(Color.WHITE);

		var list = new VBox(25);

		var highscores = Client.the().getHighscores().getHighscores();

		for (int i = 0; i < highscores.size(); ++i) {
			if (highscores.get(i).getScore() == 0) {
				continue;
			}

			var place = new Label(String.valueOf(i + 1) + ".");
			place.setFont(Fonts.get(Fonts.UI_TITLE));
			place.setTextFill(switch (i) {
				case 0 -> Color.GOLD;
				case 1 -> Color.SILVER;
				case 2 -> Color.color(120d / 255, 70d / 255, 40d / 255);
				default -> Color.WHITE;
			});
			place.setPrefWidth(this.getCenterWidth() * 1 / 20);

			var score = new Label(String.valueOf(highscores.get(i).getScore()));
			score.setFont(Fonts.get(Fonts.UI_SCORE));
			score.setTextFill(Color.WHITE);
			score.setPrefWidth(this.getCenterWidth() * 1 / 15);

			var players = new Label(String.join(", ", highscores.get(i).getNames()));
			players.setFont(Fonts.get(Fonts.UI_PRIMARY));
			players.setTextFill(Color.WHITE);

			var span = new HBox(25, place, score, players);
			span.setAlignment(Pos.CENTER_LEFT);

			list.getChildren().add(span);
		}

		list.setAlignment(Pos.CENTER);

		var box = new HBox(list);
		var aligner = new HBox(box);
		aligner.setAlignment(Pos.CENTER);

		var container = new VBox(100, title, aligner);
		container.setAlignment(Pos.CENTER);
		container.prefHeightProperty().bind(this.getScene().heightProperty());

		this.pane.setCenter(container);
	}
}
