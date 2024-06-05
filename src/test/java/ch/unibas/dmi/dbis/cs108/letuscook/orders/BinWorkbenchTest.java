package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BinWorkbenchTest {

	private BinWorkbench bin;

	/**
	 * Sets up all required prerequisites for the test units.
	 */
	@BeforeEach
	public void setUp() {
		bin = new BinWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	/**
	 * This test checks if the BinWorkbench was created correctly. It asserts that the contents of
	 * the bin are empty after creation.
	 */
	@Test
	public void testBinWorkbenchCreation() {
		assertTrue(bin.peekContents().isEmpty());
	}

	/**
	 * This test checks if the state of the workbench is set correctly. It forces the state of the
	 * bin to ACTIVE and then sets the contents according to the state.
	 */
	@Test
	public void testForceSetContentsAccordingToState() {
		bin.forceSetState(State.ACTIVE);
		bin.forceSetContentsAccordingToState(new Stack());
		assertTrue(bin.peekContents().isEmpty());
	}

	/**
	 * This test checks if the bin accepts the offer and gives nothing back. It creates a new Stack
	 * as an offer, trades it with the bin, and checks the result.
	 */
	@Test
	public void testTrade() {
		Stack offer = new Stack();
		Stack result = bin.trade(offer);

		assertTrue(
			result.isEmpty());
		assertTrue(bin.peekContents().isEmpty());
	}


}
