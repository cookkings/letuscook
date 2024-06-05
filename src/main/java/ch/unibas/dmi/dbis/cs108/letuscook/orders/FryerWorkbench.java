package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Resource;
import javafx.scene.image.Image;

/**
 * A fryer.
 */
public class FryerWorkbench extends TransformerWorkbench {

	/**
	 * Create a fryer.
	 */
	public FryerWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * @return the recipes.
	 */
	@Override
	public final Recipe[] getRecipes() {
		return new Recipe[]{
			new Recipe(Stack.of(Item.RAW_FRIES), 3, Stack.of(Item.FRIES), 10,
				Stack.of(Item.BURNT_FRIES)),
			new Recipe(Stack.of(Item.RAW_CHICKEN_NUGGETS), 5, Stack.of(Item.FRIED_CHICKEN_NUGGETS),
				10,
				Stack.of(Item.BURNT_CHICKEN_NUGGETS)),
		};
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
		this.surfaceImages.computeIfAbsent(null,
			key -> new Image(Resource.get("workbench/fryer_active.png"), SIZE.px(), SIZE.px(), true,
				false));
		this.surfaceImages.computeIfAbsent(State.IDLE,
			key -> new Image(Resource.get("workbench/fryer_idle.png"), SIZE.px(), SIZE.px(), true,
				false));
	}
}
