package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Identifier;

/**
 * Represents a workbench for plating items.
 */
public class PlateWorkbench extends Workbench {

	/**
	 * The contents currently on the plate.
	 */
	private Stack contents = new Stack();

	/**
	 * Constructs a new PlateWorkbench with the specified identifier and coordinates.
	 *
	 * @param identifier The identifier of the plate workbench.
	 * @param coords     The coordinates of the plate workbench.
	 */
	public PlateWorkbench(Identifier identifier, Coords coords) {
		super(identifier, coords);
	}

	/**
	 * Retrieves the contents currently on the plate.
	 *
	 * @return The contents currently on the plate.
	 */
	@Override
	public Stack peekContents() {
		return this.contents;
	}

	/**
	 * Sets the contents of the plate workbench according to the provided stack.
	 *
	 * @param contents The stack representing the contents to be set on the plate workbench.
	 */
	@Override
	public void forceSetContentsAccordingToState(Stack contents) {
		this.contents = contents;
	}

	/**
	 * Trades items with the plate workbench. If the offer is empty, the contents on the plate
	 * workbench are returned. Otherwise, the offer is added to the plate workbench.
	 *
	 * @param offer The stack of items being offered.
	 * @return The stack of items received in return.
	 */
	@Override
	public Stack trade(Stack offer) {
		if (offer.isEmpty()) {
			Stack content = this.peekContents();
			this.contents = new Stack();
			return content;
		}

		contents.push(offer);
		return new Stack();
	}

	/**
	 * Compute the surface images.
	 */
	@Override
	final void computeSurfaceImages() {
	}
}
