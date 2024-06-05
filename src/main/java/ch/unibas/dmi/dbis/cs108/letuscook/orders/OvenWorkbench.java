package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import javafx.scene.image.Image;

/**
 * An oven.
 */
public class OvenWorkbench extends TransformerWorkbench {

	/**
	 * Create an oven.
	 */
	public OvenWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * @return the recipes.
	 */
	@Override
	public final Recipe[] getRecipes() {
		return new Recipe[]{
			new Recipe(Stack.of(Item.BREAD, Item.TOMATO_SAUCE, Item.CHEESE), 5,
				Stack.of(Item.PIZZA_MARGHERITA), 10, Stack.of(Item.BURNT_PIZZA_MARGHERITA)),
		};
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
		this.surfaceImages.computeIfAbsent(null,
			key -> new Image(Resource.get("workbench/oven_active.png"), SIZE.px(), SIZE.px(), true,
				false));
		this.surfaceImages.computeIfAbsent(State.IDLE,
			key -> new Image(Resource.get("workbench/oven_idle.png"), SIZE.px(), SIZE.px(), true,
				false));
	}
}
