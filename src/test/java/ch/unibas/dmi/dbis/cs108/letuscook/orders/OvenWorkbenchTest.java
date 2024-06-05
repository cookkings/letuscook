package ch.unibas.dmi.dbis.cs108.letuscook.orders;

import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import ch.unibas.dmi.dbis.cs108.letuscook.util.Coords;
import ch.unibas.dmi.dbis.cs108.letuscook.util.IdentifierFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OvenWorkbenchTest {

	private OvenWorkbench oven;

	@BeforeEach
	public void setUp() {
		oven = new OvenWorkbench(new IdentifierFactory().next(),
			new Coords(new Units(0), new Units(0)));
	}

	@Test
	public void testOvenWorkbenchCreation() {
		assertTrue(oven.peekContents().isEmpty());
	}
	
}
