package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;

/**
 * Represents a workbench for processing a single item.
 */

public class ItemWorkbench extends Workbench {

	/**
	 * The item placed on the workbench.
	 */
	private final Item content;

	/**
	 * Constructs a new ItemWorkbench with the specified identifier, coordinates, and item content.
	 *
	 * @param identifier The identifier of the item workbench.
	 * @param coords     The coordinates of the item workbench.
	 * @param content    The item placed on the workbench.
	 */
	public ItemWorkbench(Identifier identifier, Coords coords, Item content) {
		super(identifier, coords);

		this.content = content;
	}

	/**
	 * Retrieves the item currently placed on the workbench.
	 *
	 * @return The item currently placed on the workbench.
	 */
	@Override
	public Stack peekContents() {
		return Stack.of(this.content);
	}

	/**
	 * Sets the contents of the item workbench according to the provided stack.
	 *
	 * @param contents The stack representing the contents to be set on the item workbench.
	 */
	@Override
	public void forceSetContentsAccordingToState(Stack contents) {
		assert contents.equals(Stack.of(this.content));
	}

	/**
	 * Trades items with the item workbench. If the offer is not empty, it is returned unchanged. If
	 * the offer is empty, the item currently on the workbench is returned.
	 *
	 * @param offer The stack of items being offered.
	 * @return The stack of items received in return.
	 */
	@Override
	public Stack trade(Stack offer) {
		if (!offer.isEmpty()) {
			return offer;
		}

		return this.peekContents();
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
	}
}
