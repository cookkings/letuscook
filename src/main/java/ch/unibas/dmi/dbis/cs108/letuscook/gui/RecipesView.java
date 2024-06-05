package ch.unibas.dmi.dbis.cs108.letuscook.gui;

import ch.unibas.dmi.dbis.cs108.letuscook.orders.ChoppingWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.FryerWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.GrillWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Order;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.OvenWorkbench;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Recipe;
import ch.unibas.dmi.dbis.cs108.letuscook.orders.Workbench;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Fonts;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Images;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The RecipesView class represents the graphical user interface (GUI) for displaying recipes in the
 * Let Us Cook game.
 */
public class RecipesView extends View<BorderPane> {

	private static final double listSpacing = 10;

	Image grillImage = new Image(Resource.get("workbench/grill_active.png"),
		Order.SIZE.px(), Order.SIZE.px(), true, false);

	Image ovenImage = new Image(Resource.get("workbench/oven_active.png"),
		Order.SIZE.px(), Order.SIZE.px(), true, false);

	Image fryerImage = new Image(Resource.get("workbench/fryer_active.png"),
		Order.SIZE.px(), Order.SIZE.px(), true, false);

	Image choppingImage = new Image(Resource.get("workbench/chopping_active.png"),
		Order.SIZE.px(), Order.SIZE.px(), true, false);

	/**
	 * Constructs a new RecipesView object with the specified stage.
	 *
	 * @param stage The stage for the view.
	 */
	public RecipesView(Stage stage) {
		super(stage,
			Views.RECIPES,
			new BorderPane(),
			800d, 600d,
			"Let Us Cook!",
			Images.get(Images.BACKGROUND),
			Images.RETURN,
			() -> ClientGUI.changeRequestedPopUpView(null),
			"Back"
		);

		var orderList = new HBox(listSpacing);

		for (var order : Order.values()) {
			var parts = new VBox();

			var stack = order.stack().toArray();
			for (int i = 0; i < stack.length; ++i) {
				parts.getChildren().add(this.object(stack[i].getImage()));
				if (i < stack.length - 1) {
					parts.getChildren().add(this.plusSymbol());
				}
			}

			parts.getChildren().addAll(this.actionSymbol(), this.object(order.getImage()));
			parts.setAlignment(Pos.TOP_CENTER);
			parts.setStyle("-fx-background-color: rgba(30, 30, 50, 0.5);");

			orderList.getChildren().add(parts);
		}

		var transformationLists = new HBox(50);

		transformationLists.getChildren().addAll(
			this.transformationList(grillImage, new GrillWorkbench(Identifier.NONE,
				new Coords(new Units(0), new Units(0))).getRecipes()),
			this.transformationList(fryerImage, new FryerWorkbench(Identifier.NONE,
				new Coords(new Units(0), new Units(0))).getRecipes()),
			this.transformationList(choppingImage, new ChoppingWorkbench(Identifier.NONE,
				new Coords(new Units(0), new Units(0))).getRecipes()),
			this.transformationList(ovenImage, new OvenWorkbench(Identifier.NONE,
				new Coords(new Units(0), new Units(0))).getRecipes())
		);

		var panels = new HBox(50, transformationLists, orderList);

		this.pane.setCenter(panels);
	}

	/**
	 * Create an ImageView for the given image.
	 *
	 * @param image The image to be displayed.
	 * @return The created ImageView.
	 */
	private ImageView object(Image image) {
		var imageView = new ImageView(image);
		imageView.setFitWidth(Workbench.SIZE.px() / 2);
		return imageView;
	}

	/**
	 * Create a Label for the plus symbol.
	 *
	 * @return The created Label.
	 */
	private Label plusSymbol() {
		var label = new Label("+");
		label.setAlignment(Pos.CENTER);
		label.setFont(Fonts.get(Fonts.UI_PRIMARY));
		label.setTextFill(Color.WHITE);
		label.setMaxHeight(20);
		label.setMinHeight(20);
		label.setPrefHeight(20);
		return label;
	}

	/**
	 * Create a Label for the action symbol.
	 *
	 * @return The created Label.
	 */
	private Label actionSymbol() {
		var label = new Label("\\/");
		label.setAlignment(Pos.CENTER);
		label.setFont(Fonts.get(Fonts.UI_PRIMARY));
		label.setTextFill(Color.WHITE);
		label.setPadding(new Insets(20, 0, 20, 0));
		return label;
	}

	/**
	 * Create a transformation list for the given workbench and recipes.
	 *
	 * @param transformer The image representing the workbench.
	 * @param recipes     The recipes for the workbench.
	 * @return The HBox containing the transformation list.
	 */
	private HBox transformationList(Image transformer, Recipe[] recipes) {
		var transformationList = new HBox(listSpacing);

		for (var recipe : recipes) {
			var parts = new VBox();

			var stack = recipe.items().toArray();
			for (int i = 0; i < stack.length; ++i) {
				parts.getChildren().add(this.object(stack[i].getImage()));
				if (i < stack.length - 1) {
					parts.getChildren().add(this.plusSymbol());
				}
			}

			parts.getChildren()
				.addAll(this.actionSymbol(), this.object(transformer), this.actionSymbol(),
					this.object(recipe.result().reduceToSingleItem().getImage()));
			parts.setAlignment(Pos.TOP_CENTER);
			parts.setStyle("-fx-background-color: rgba(30, 30, 50, 0.5);");

			transformationList.getChildren().add(parts);
		}

		return transformationList;
	}
}
