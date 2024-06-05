package ch.unibas.dmi.dbis.cs108.letuscook.util;

import ch.unibas.dmi.dbis.cs108.letuscook.gui.Units;

/**
 * A vector.
 */
public class Vector {

	/**
	 * The horizontal component.
	 */
	private Units x;

	/**
	 * The vertical component.
	 */
	private Units y;

	/**
	 * Create a vector with the given initial components.
	 *
	 * @param x the horizontal component.
	 * @param y the vertical component.
	 */
	public Vector(final Units x, final Units y) {
		assert x != null && y != null;

		this.x = x;
		this.y = y;
	}

	/**
	 * Parse coordinates from a string.
	 *
	 * @param string the string.
	 * @throws MalformedException if the coordinates are malformed.
	 */
	public Vector(final String string) throws MalformedException {
		try {
			String[] xy = string.split(",", 2);

			this.x = new Units(Double.parseDouble(xy[0]));
			this.y = new Units(Double.parseDouble(xy[1]));
		} catch (NullPointerException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new MalformedException("malformed component(s)");
		}
	}

	/**
	 * Get the displacement vector between two sets of coordinates.
	 *
	 * @param a the origin.
	 * @param b the target.
	 * @return the displacement vector.
	 */
	public static Vector between(final Coords a, final Coords b) {
		return new Vector(new Units(b.getX().u() - a.getX().u()),
			new Units(b.getY().u() - a.getY().u()));
	}

	/**
	 * @return the horizontal component.
	 */
	public final Units getX() {
		return this.x;
	}

	/**
	 * Set the horizontal component.
	 *
	 * @param x the horizontal component.
	 */
	public void setX(final Units x) {
		this.x = x;
	}

	/**
	 * @return the vertical component.
	 */
	public final Units getY() {
		return this.y;
	}

	/**
	 * Set the vertical component.
	 *
	 * @param y the vertical component.
	 */
	public void setY(final Units y) {
		this.y = y;
	}

	/**
	 * @return the magnitude of this vector.
	 */
	public final Units magnitude() {
		return new Units(Math.sqrt(Math.pow(this.getX().u(), 2) + Math.pow(this.getY().u(), 2)));
	}

	/**
	 * Create a copy of this vector with the given magnitude.
	 *
	 * @param magnitude the new magnitude.
	 * @return the new vector.
	 */
	public final Vector withMagnitude(final Units magnitude) {
		if (this.magnitude().u() == 0) {
			return new Vector(new Units(0), new Units(0));
		}

		double factor = magnitude.u() / this.magnitude().u();

		return new Vector(new Units(this.getX().u() * factor), new Units(this.getY().u() * factor));
	}

	/**
	 * @return a textual representation of these coordinates.
	 */
	@Override
	public String toString() {
		return this.x + "," + this.y;
	}

	/**
	 * Compare two vectors for equality.
	 *
	 * @param object the vector to compare to.
	 * @return whether the two vectors are equal.
	 */
	@Override
	public boolean equals(final Object object) {
		if (!(object instanceof final Vector that)) {
			return false;
		}

		return this.getX().u() == that.getX().u() && this.getY().u() == that.getY().u();
	}
}
