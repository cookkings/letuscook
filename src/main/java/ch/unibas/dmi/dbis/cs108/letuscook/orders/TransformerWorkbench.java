package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.server.Game;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Messenger;
import java.util.Optional;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class TransformerWorkbench extends Workbench {

	private Recipe recipe = null;

	/**
	 * Create a workbench.
	 */
	public TransformerWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * @return the current recipe.
	 */
	public final Recipe getRecipe() {
		return this.recipe;
	}

	/**
	 * @return the recipes.
	 */
	public abstract Recipe[] getRecipes();

	/**
	 * Find a recipe via a stack of items, if it exists.
	 *
	 * @param items the items.
	 * @return the recipe, if it exists.
	 */
	public final Optional<Recipe> findRecipeByItems(Stack items) {
		for (Recipe recipe : this.getRecipes()) {
			if (recipe.items().equals(items)) {
				return Optional.of(recipe);
			}
		}

		return Optional.empty();
	}

	/**
	 * Find a recipe via its result, if it exists.
	 *
	 * @param result the result.
	 * @return the recipe, if it exists.
	 */
	public final Optional<Recipe> findRecipeByResult(Stack result) {
		for (Recipe recipe : this.getRecipes()) {
			if (recipe.result().equals(result)) {
				return Optional.of(recipe);
			}
		}

		return Optional.empty();
	}

	@Override
	public final Stack peekContents() {
		if (this.recipe == null && this.getState() != State.IDLE) {
			Messenger.warn("Contents and state disagree");
			return new Stack();
		}
		return switch (this.getState()) {
			case IDLE -> new Stack();
			case ACTIVE -> this.recipe.items();
			case FINISHED -> this.recipe.result();
			case EXPIRED -> this.recipe.ruined();
		};
	}

	@Override
	public final void forceSetContentsAccordingToState(Stack contents) {
		switch (this.getState()) {
			case IDLE -> {
				assert contents.isEmpty();
				this.recipe = null;
			}
			case ACTIVE -> {
				this.recipe = this.findRecipeByItems(contents).orElseThrow();
			}
			case FINISHED -> {
				this.recipe = this.findRecipeByResult(contents).orElseThrow();
			}
		}
	}

	@Override
	public final Stack trade(Stack offer) {
		switch (this.getState()) {
			case IDLE -> {
				var recipeOrEmpty = this.findRecipeByItems(offer);
				if (recipeOrEmpty.isPresent()) {
					this.forceSetState(State.ACTIVE);
					this.forceSetContentsAccordingToState(offer);
					this.ticksUntilStateChange.set(
						recipeOrEmpty.orElseThrow().preparationTimeSeconds() * Game.TPS);
					return new Stack();
				}
			}
			case ACTIVE, FINISHED, EXPIRED -> {
				if (offer.isEmpty()) {
					Stack contents = this.peekContents();
					this.forceSetState(State.IDLE);
					this.forceSetContentsAccordingToState(new Stack());
					return contents;
				}
			}
		}
		return offer;
	}

	/**
	 * Get the fraction of time remaining until the next state change.
	 *
	 * @return the time fraction.
	 */
	@Override
	public final double getTimeFraction() {
		Recipe recipe = this.getRecipe();
		if (recipe == null) {
			return 0;
		}

		double total = Game.TPS * switch (this.getState()) {
			case IDLE, EXPIRED -> 0;
			case ACTIVE -> recipe.preparationTimeSeconds();
			case FINISHED -> recipe.expirationTimeSeconds();
		};

		return Math.min(total, Math.max(0.01, 1d * this.ticksUntilStateChange.get() / total));
	}

	@Override
	void drawProgress(GraphicsContext ctx) {
		double timeFraction = this.getTimeFraction();

		ctx.setFill(switch (this.getState()) {
			case ACTIVE -> new Color(1, 1, 1, 0.6);
			case FINISHED -> new Color(1, 0, 0, 0.3);
			default -> Color.TRANSPARENT;
		});

		ctx.fillRect(this.getRect().getLeft().px(), this.getRect().getTop().px(),
			(1 - timeFraction) * SIZE.px(), SIZE.px());
	}

	/**
	 * Draw the contents.
	 *
	 * @param ctx the graphics context.
	 */
	void drawContents(GraphicsContext ctx) {
		this.peekContents().draw(ctx, this.getRect().asCoords());
	}
}
