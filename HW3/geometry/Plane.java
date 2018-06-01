package geometry;

import main.RayTracer.Ray;
import utility.Vector;

public class Plane extends Shape {
	Vector normal;
	double offset;

	public Plane(Vector normal, double offset, int materialIndex) {
		this.normal = normal;
		this.offset = offset;
		this.materialIndex = materialIndex;
	}

	@Override
	public double hit(Ray ray) {
		// TODO Auto-generated method stub
		return 0;
	}
}