package main;

import utility.Vector;

/**
 * The ray class. Defined by on origin (p) and a direction (d). A point (r) on
 * the ray with agree with the function: r = p + t * d Where t is a number
 * representing the "distance" or "offset" on the ray. Basically, a point is
 * some offset from the origin with the direction of the ray.
 */
public class Ray {
	private Vector origin, direction;

	public Ray(Vector origin, Vector direction) {
		this.origin = origin;
		this.direction = direction;

		this.direction.normalize();
	}

	public Vector getOrigin() {
		return origin;
	}

	public Vector getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return String.format("<Ray: [%s, %s]>", this.origin, this.direction);
	}
}
