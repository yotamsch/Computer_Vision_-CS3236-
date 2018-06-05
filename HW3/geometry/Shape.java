package geometry;

import main.RayTracer.Ray;
import utility.Vector;

public abstract class Shape {
	private int materialIndex;

	public Shape(int materialIndex) {
		this.materialIndex = materialIndex;
	}

	public int getMaterialIndex() {
		return materialIndex;
	}

	/**
	 * The function checks if a ray intersects the shape. Every derived shape
	 * implements the function differently.
	 * 
	 * @param ray
	 *            - The ray to check with
	 * @return The intersection location, or 0.0 if no intersection
	 */
	public abstract double hit(Ray ray);

	/**
	 * Gets a normal for the shape surface at a given point. Assuming the point in
	 * on the surface of the shape.
	 * 
	 * @param point
	 *            - The point for calculation
	 * @return A vector perpendicular to the surface at the point
	 */
	public abstract Vector getNormalAt(Vector point);

	/**
	 * Calculates the reflected ray direction at a certain point on the surface.
	 * 
	 * @param rayDirection
	 *            The direction of the ray to be reflected
	 * @param point
	 *            The point to reflect the ray for
	 * @return A vector representing the reflected ray direction
	 */
	public Ray getReflectedRay(Vector rayDirection, Vector point) {
		Vector N = this.getNormalAt(point);
		Vector V = new Vector(rayDirection).mul(-1).normalize();

		return new Ray(new Vector(point), new Vector(N).mul(2 * Vector.dot(N, V)).sub(V));
	}

	/**
	 * Gets the refracted ray based on an original ray, a point of intersection on
	 * the shape and the fraction between the refraction of the two shapes.
	 * 
	 * @param rayDirection
	 *            The direction of the original ray
	 * @param point
	 *            The intersection point on the surface of the shape
	 * @param transformFraction
	 *            The fraction between the refraction parameter of the two shapes
	 * @return A new ray representing the refracted ray
	 */
	public Ray getRefractedRay(Vector rayDirection, Vector point, double transformFraction) {
		Vector N = getNormalAt(point);
		double c1 = -1 * Vector.dot(rayDirection, N);
		double c2 = Math.sqrt(1 - Math.pow(transformFraction, 2) * (1 - Math.pow(c1, 2)));

		Vector direction = new Vector(rayDirection).mul(transformFraction)
				.add(new Vector(N).mul(transformFraction * c1 - c2));

		return new Ray(new Vector(point), direction);
	}
}
