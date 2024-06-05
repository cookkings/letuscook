package ch.unibas.dmi.dbis.cs108.letuscook.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoordsTest {

	public static Coords fiveSix;

	@BeforeEach
	void setUp() {
		fiveSix = new Coords(new Units(5), new Units(6));
	}

	@Test
	void testSetGet() {
		fiveSix.setX(new Units(6));
		assertEquals(6, fiveSix.getX().u());
	}

	@Test
	void testNoNegative() {
		assertThrows(AssertionError.class, () -> new Coords(new Units(-1), new Units(0)));
		assertThrows(AssertionError.class, () -> fiveSix.setX(new Units(-1)));
	}

	@Test
	void testDisplace() throws MalformedException {
		fiveSix.displace(new Vector("-2,2"));
		assertEquals(new Coords("3,8"), fiveSix);
	}

	@Test
	void testDisplaced() throws MalformedException {
		assertEquals(new Coords("3,8"), fiveSix.displaced(new Vector("-2,2")));
	}
}
