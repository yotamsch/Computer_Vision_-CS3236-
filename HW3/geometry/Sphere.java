package geometry;

import main.RayTracer.Ray;
import utility.Vector;

public class Sphere extends Shape {
	// NOTE:
	// The sphere has a center point (p) and a radius (R).
	// A point (r) is on a sphere if it agrees with the function:
	// (r - p) * (r - p) = R ^ 2
	private Vector center;
	private double radius;

	public Sphere(Vector center, double radius, int materialIndex) {
		super(materialIndex);
		this.center = center;
		this.radius = radius;
	}

	@Override
	public double hit(Ray ray) {
		// Based on the method found in:
		// https://www.cs.unc.edu/~rademach/xroads-RT/RTarticle.html

		Vector EO = new Vector(this.center).sub(ray.getOrigin());
		double v = Vector.dot(EO, ray.getDirection());
		double disc = Math.pow(this.radius, 2) - (Vector.dot(EO, EO) - Math.pow(v, 2));
		if (disc < 0) {
			return 0;
		} else {
			return v - Math.sqrt(disc);
		}
	}

	@Override
	public Vector getNormalAt(Vector point) {
		return new Vector(point).sub(this.center).normalize();
	}
}