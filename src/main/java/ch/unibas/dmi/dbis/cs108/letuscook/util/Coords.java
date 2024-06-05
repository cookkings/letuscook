package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;

/**
 * A set of coordinates.
 */
public class Coords extends Vector {

	/**
	 * Create coordinates with the given initial components.
	 *
	 * @param x the horizontal component.
	 * @param y the vertical component.
	 */
	public Coords(final Units x, final Units y) {
		super(x, y);

		assert x.u() >= 0 && y.u() >= 0 : "negative component(s)";
	}

	/**
	 * Parse coordinates from a string.
	 *
	 * @param string the string.
	 * @throws MalformedException if the string is malformed.
	 */
	public Coords(final String string) throws MalformedException {
		super(string);
	}

	/**
	 * Calculate the distance between two coordinates.
	 *
	 * @param a the first coordinates.
	 * @param b the second coordinates.
	 * @return the distance.
	 */
	public static Units distance(Coords a, Coords b) {
		return Vector.between(a, b).magnitude();
	}

	/**
	 * Set the horizontal component.
	 *
	 * @param x the horizontal component.
	 */
	@Override
	public void setX(final Units x) {
		assert x.u() >= 0;

		super.setX(x);
	}

	/**
	 * Set the vertical component.
	 *
	 * @param y the vertical component.
	 */
	@Override
	public void setY(final Units y) {
		assert y.u() >= 0;

		super.setY(y);
	}

	/**
	 * Apply a displacement vector to these coordinates.
	 *
	 * @param vector the displacement vector.
	 */
	public void displace(final Vector vector) {
		this.setX(new Units(Math.max(0, this.getX().u() + vector.getX().u())));
		this.setY(new Units(Math.max(0, this.getY().u() + vector.getY().u())));
	}

	/**
	 * Create a displaced copy of these coordinates.
	 *
	 * @param vector the displacement vector.
	 */
	public Coords displaced(final Vector vector) {
		Coords coords = new Coords(this.getX(), this.getY());

		coords.displace(vector);

		return coords;
	}
}
