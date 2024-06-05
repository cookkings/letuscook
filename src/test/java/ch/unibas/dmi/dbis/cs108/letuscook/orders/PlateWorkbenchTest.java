package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PlateWorkbenchTest {

	private PlateWorkbench plateWorkbench;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		plateWorkbench = new PlateWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	/**
	 * Tests the creation of a PlateWorkbench instance.
	 */
	@Test
	public void testPlateWorkbenchCreation() {
		assertTrue(plateWorkbench.peekContents().isEmpty());
	}


	/**
	 * Tests the trade method with an empty inventory.
	 */
	@Test
	public void testTradeWithEmptyInventory() {
		Stack offer = new Stack();
		Stack result = plateWorkbench.trade(offer);

		assertTrue(result.isEmpty());
		assertTrue(plateWorkbench.peekContents().isEmpty());
	}
}
