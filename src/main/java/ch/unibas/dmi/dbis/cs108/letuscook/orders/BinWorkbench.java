package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;

/**
 * Represents a workbench used as a bin for discarding items.
 */
public class BinWorkbench extends Workbench {

	/**
	 * Constructs a new BinWorkbench with the specified identifier and coordinates.
	 *
	 * @param identifier The identifier of the bin workbench.
	 * @param coords     The coordinates of the bin workbench.
	 */
	public BinWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * Retrieves the contents of the bin workbench.
	 *
	 * @return An empty stack, as the bin has no contents.
	 */
	@Override
	public Stack peekContents() {
		return new Stack();
	}

	/**
	 * Sets the contents of the bin workbench according to the provided stack. Since the bin is used
	 * for discarding items, the provided stack must be empty.
	 *
	 * @param contents The stack representing the contents to be set on the bin workbench.
	 */
	@Override
	public void forceSetContentsAccordingToState(Stack contents) {
		assert contents.isEmpty();
	}

	/**
	 * Trades items with the bin workbench. All items offered to the bin are discarded, and an empty
	 * stack is returned.
	 *
	 * @param offer The stack of items being offered.
	 * @return An empty stack, as all items are discarded.
	 */
	@Override
	public Stack trade(Stack offer) {
		return this.peekContents();
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
	}
}
