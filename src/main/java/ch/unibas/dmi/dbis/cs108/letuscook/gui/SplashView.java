package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * The SplashView class represents the graphical user interface (GUI) for the splash screen in the
 * Let Us Cook game.
 */
public class SplashView extends View<BorderPane> {

	/**
	 * Constructs a new SplashView object with the specified stage.
	 *
	 * @param stage The stage for the view.
	 */
	public SplashView(Stage stage) {
		super(
			stage,
			Views.SPLASH,
			new BorderPane(),
			800d, 600d,
			"Let Us Cook!",
			null,
			null, null, null
		);

		this.pane.setCenter(new ImageView(Images.get(Images.LOGO)));
	}
}
