package geometry;

import main.RayTracer.Ray;
import utility.Vector;

public class Sphere extends Shape {
	Vector center;
	double radius;

	public Sphere(Vector center, double radius, int materialIndex) {
		this.center = center;
		this.radius = radius;
		this.materialIndex = materialIndex;
	}

	@Override
	public double hit(Ray ray) {
		// TODO Auto-generated method stub
		return 0;
	}
}