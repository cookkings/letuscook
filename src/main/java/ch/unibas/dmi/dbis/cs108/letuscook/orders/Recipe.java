package ch.unibas.dmi.dbis.cs108.letuscook.orders;

/**
 * Represents a recipe for cooking an item.
 */
public record Recipe(Stack items, int preparationTimeSeconds, Stack result,
                     int expirationTimeSeconds, Stack ruined) {

	/**
	 * Constructs a new Recipe with the specified items, preparation time, and result.
	 *
	 * @param items                  The items required for the recipe.
	 * @param preparationTimeSeconds The time required to prepare the recipe, in seconds.
	 * @param result                 The result produced by the recipe.
	 */
	public Recipe(Stack items, int preparationTimeSeconds, Stack result) {
		this(items, preparationTimeSeconds, result, 0, null);
	}

	/**
	 * Returns a copy of the items required for the recipe.
	 *
	 * @return A copy of the items required for the recipe.
	 */

	public Stack items() {
		return this.items.copy();
	}

	/**
	 * Returns a copy of the result produced by the recipe.
	 *
	 * @return A copy of the result produced by the recipe.
	 */
	public Stack result() {
		return this.result.copy();
	}

	/**
	 * Returns a copy of the items that are ruined if the recipe fails.
	 *
	 * @return A copy of the items that are ruined if the recipe fails.
	 */
	public Stack ruined() {
		return this.ruined.copy();
	}
}
