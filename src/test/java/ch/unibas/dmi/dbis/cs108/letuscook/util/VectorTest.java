package ch.unibas.dmi.dbis.cs108.letuscook.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class VectorTest {

	public static Vector fiveSix;
	public static Vector southEast;
	public static Vector southWest;
	public static Vector northEast;
	public static Vector northWest;

	@BeforeEach
	void setUp() {
		fiveSix = new Vector(new Units(5), new Units(6));
	}

	@Test
	void testParse() throws MalformedException {
		assertEquals(fiveSix, new Vector("5,6"));
	}

	@Test
	void testToString() {
		assertEquals(5d + "," + 6d, fiveSix.toString());
	}

	@Test
	void testBetween() throws MalformedException {
		assertEquals(new Vector("4,5"), Vector.between(new Coords("1,1"), new Coords("5,6")));
	}

	@Test
	void testWithMagnitude() throws MalformedException {
		assertEquals(new Vector("10,12").magnitude(),
			fiveSix.withMagnitude(new Units(fiveSix.magnitude().u() * 2)).magnitude());
	}
}
