package geometry;

import main.RayTracer.Ray;

public abstract class Shape {
	int materialIndex;
	
	/**
	 * The function checks if a ray intersects the shape. 
	 * Every derived shape implements the function differently.
	 * @param ray - The ray to check with 
	 * @return The intersection location, or 0.0 if no intersection 
	 */
	public abstract double hit(Ray ray);
}
