package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Sounds;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * Represents a utility class for creating GUI controls in the Let Us Cook application.
 */
public class Controls {

	public static final Units primaryButtonWidth = new Units(1.6);

	public static final Units primaryButtonHeight = new Units(1);

	public static final Units secondaryButtonWidth = new Units(1.5);

	public static final Units secondaryButtonHeight = new Units(0.75);

	public static final double sidebarWidthToHeightRatio = 64d / 20;

	private static double sidebarWidth() {
		return 0.075 * ClientGUI.the().width;
	}

	/**
	 * Returns the height of the sidebar.
	 *
	 * @return The height of the sidebar.
	 */
	private static double sidebarHeight() {
		return sidebarWidth() / sidebarWidthToHeightRatio;
	}

	/**
	 * Creates a sidebar button.
	 *
	 * @param icon     The icon image.
	 * @param text     The text of the button.
	 * @param runnable The action to be performed when the button is clicked.
	 * @return The created button.
	 */
	public static Button sidebarButton(Images icon, String text, Runnable runnable) {
		var image = new ImageView(Images.get(icon));
		image.setPreserveRatio(true);
		int imageInset = 5;
		image.setFitWidth(sidebarHeight() - 2 * imageInset);

		var label = new Label(text);
		label.setTextFill(Color.BLACK);
		label.setPrefWidth(sidebarWidth() - sidebarHeight());
		label.setFont(Fonts.get(Fonts.UI_SECONDARY));

		var span = new HBox(imageInset + 5, image, label);
		span.setPadding(new Insets(0, 0, 0, imageInset));
		span.setPrefWidth(sidebarWidth());
		span.setAlignment(Pos.CENTER_LEFT);

		var background = new ImageView(Images.get(Images.SIDEBAR_BUTTON));
		background.setPreserveRatio(true);
		background.setFitWidth(sidebarWidth());

		var stack = new StackPane();
		stack.getChildren().addAll(background, span);

		var button = new Button(null, stack);
		button.setPrefWidth(sidebarWidth());
		button.setStyle("-fx-background-color: transparent;");

		button.setOnAction(event -> {
			Sounds.CLICK.play();
			runnable.run();
		});

		return button;
	}

	/**
	 * Creates a primary button.
	 *
	 * @param text     The text of the button.
	 * @param runnable The action to be performed when the button is clicked.
	 * @return The created button.
	 */
	public static Button primaryButton(String text, Runnable runnable) {
		return bigButton(Images.PRIMARY_BUTTON, -primaryButtonHeight.px() / 8, text, runnable);
	}

	/**
	 * Creates a secondary button.
	 *
	 * @param text     The text of the button.
	 * @param runnable The action to be performed when the button is clicked.
	 * @return The created button.
	 */
	public static Button secondaryButton(String text, Runnable runnable) {
		return bigButton(Images.SECONDARY_BUTTON, 0, text, runnable);
	}

	/**
	 * Creates a big button.
	 *
	 * @param backgroundImage The background image of the button.
	 * @param translateY      The Y-axis translation of the button.
	 * @param text            The text of the button.
	 * @param runnable        The action to be performed when the button is clicked.
	 * @return The created button.
	 */
	public static Button bigButton(Images backgroundImage, double translateY, String text,
		Runnable runnable) {
		var image = Images.get(backgroundImage);

		var imageView = new ImageView(image);
		imageView.setPreserveRatio(true);
		imageView.setFitWidth(image.getWidth());

		var label = new Label(text);
		label.setTranslateY(-image.getHeight() / 25 + translateY);
		label.setAlignment(Pos.CENTER);
		label.setWrapText(true);
		label.setPrefWidth(image.getWidth());
		label.setFont(Fonts.get(Fonts.UI_PRIMARY));
		label.setTextFill(Color.BLACK);

		var stack = new StackPane();
		stack.getChildren().addAll(imageView, label);

		var button = new Button(null, stack);
		button.setPrefWidth(image.getWidth());
		button.setPrefHeight(image.getHeight());
		button.setStyle("-fx-background-color: transparent;");

		if (runnable == null) {
			button.setOpacity(0.5);
		} else {
			button.setOnAction(event -> {
				Sounds.CLICK.play();
				runnable.run();
			});
		}

		return button;
	}

	/**
	 * Creates a text area.
	 *
	 * @param width        The width of the text area.
	 * @param height       The height of the text area.
	 * @param prompt       The prompt text of the text area.
	 * @param wrap         Whether the text area should wrap text.
	 * @param onKeyPressed The action to be performed when a key is pressed.
	 * @param filter       The filter predicate for allowed characters.
	 * @param limit        The maximum character limit.
	 * @return The created text area.
	 */
	public static TextArea textArea(double width, double height, String prompt, boolean wrap,
		Consumer<KeyCode> onKeyPressed, IntPredicate filter, int limit) {
		final String backgroundColor = "#524c83";
		final String promptColor = "#6d66a0";
		final String textColor = "#B5B8DE";
		final String backgroundColorFocused = "#dedded";
		final String promptColorFocused = "#8a81bd";
		final String textColorFocused = "#222034";
		String style =
			"-fx-control-inner-background: " + backgroundColor + ";"
				+ "-fx-prompt-text-fill: " + promptColor + ";"
				+ "-fx-text-fill: " + textColor + ";"
				+ "-fx-focus-color: transparent;"
				+ "-fx-text-box-border: transparent;"
				+ "-fx-faint-focus-color: transparent;";
		String styleFocused = style
			+ "-fx-control-inner-background: " + backgroundColorFocused + ";"
			+ "-fx-prompt-text-fill: " + promptColorFocused + ";"
			+ "-fx-text-fill: " + textColorFocused + ";";

		var textArea = new TextArea();
		textArea.setWrapText(wrap);
		textArea.setPrefSize(width, height);
		textArea.setMaxSize(width, height);
		textArea.setMinSize(width, height);
		textArea.setPromptText(prompt);

		textArea.setStyle(style);
		textArea.focusedProperty().addListener(
			(observable, oldValue, newValue) -> textArea.setStyle(newValue ? styleFocused : style));
		textArea.setFont(Fonts.get(Fonts.UI_PRIMARY));

		textArea.textProperty().addListener((((observable, oldValue, newValue) -> {
			if ((filter != null && !newValue.chars().allMatch(filter)) || (limit > 0
				&& newValue.length() > limit)) {
				textArea.setText(oldValue);
			}
		})));
		textArea.setOnKeyPressed(event -> {
			if (onKeyPressed != null) {
				onKeyPressed.accept(event.getCode());
			}
			event.consume();
		});
		textArea.setOnKeyReleased(Event::consume);

		return textArea;
	}
}
