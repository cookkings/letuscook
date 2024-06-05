package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import javafx.scene.image.Image;

/**
 * A grill.
 */
public class GrillWorkbench extends TransformerWorkbench {

	/**
	 * Create a grill.
	 */
	public GrillWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * @return the recipes.
	 */
	@Override
	public final Recipe[] getRecipes() {
		return new Recipe[]{
			new Recipe(Stack.of(Item.RAW_PATTY), 3, Stack.of(Item.GRILLED_PATTY), 10,
				Stack.of(Item.BURNT_PATTY)),
			new Recipe(Stack.of(Item.RAW_CHICKEN), 5, Stack.of(Item.GRILLED_CHICKEN), 10,
				Stack.of(Item.BURNT_CHICKEN)),
		};
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
		this.surfaceImages.computeIfAbsent(null,
			key -> new Image(Resource.get("workbench/grill_active.png"), SIZE.px(), SIZE.px(), true,
				false));
		this.surfaceImages.computeIfAbsent(State.IDLE,
			key -> new Image(Resource.get("workbench/grill_idle.png"), SIZE.px(), SIZE.px(), true,
				false));
	}
}
