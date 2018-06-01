package geometry;

import main.RayTracer.Ray;
import utility.Vector;

public class Triangle extends Shape {
	Vector v1, v2, v3;

	public Triangle(Vector v1, Vector v2, Vector v3, int materialIndex) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		this.materialIndex = materialIndex;
	}

	@Override
	public double hit(Ray ray) {
		// TODO Auto-generated method stub
		return 0;
	}
}
