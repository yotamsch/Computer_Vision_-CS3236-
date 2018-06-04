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
	 * The function checks if a ray intersects the shape. 
	 * Every derived shape implements the function differently.
	 * @param ray - The ray to check with 
	 * @return The intersection location, or 0.0 if no intersection 
	 */
	public abstract double hit(Ray ray);

	/**
	 * Gets a normal for the shape surface at a given point.
	 * Assuming the point in on the surface of the shape.
	 * @param point - The point for calculation
	 * @return A vector perpendicular to the surface at the point
	 */
	public abstract Vector getNormalAt(Vector point);
	
}
