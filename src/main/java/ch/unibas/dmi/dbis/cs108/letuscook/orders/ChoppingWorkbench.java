package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import javafx.scene.canvas.GraphicsContext;

/**
 * A chopping board.
 */
public class ChoppingWorkbench extends TransformerWorkbench {

	/**
	 * Create a chopping board.
	 */
	public ChoppingWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * @return the recipes.
	 */
	@Override
	public final Recipe[] getRecipes() {
		return new Recipe[]{
			new Recipe(Stack.of(Item.TOMATO), 0, Stack.of(Item.TOMATO_SAUCE)),
			new Recipe(Stack.of(Item.SALAD_HEAD), 0, Stack.of(Item.CHOPPED_SALAD)),
			new Recipe(Stack.of(Item.POTATO), 0, Stack.of(Item.RAW_FRIES)),
			new Recipe(Stack.of(Item.RAW_CHICKEN), 0, Stack.of(Item.RAW_CHICKEN_NUGGETS)),
		};
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
	}

	@Override
	final void drawProgress(GraphicsContext ctx) {
	}
}
