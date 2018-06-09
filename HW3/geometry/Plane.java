package geometry;

import main.Ray;
import utility.Vector;

public class Plane extends Shape {
	// NOTE:
	// Every plane has a normal (n) and an offset (f) from the origin "o"
	// (0,0,0) point which the plane is shifted in.
	// A point (p) on the plane will agree with:
	// p = (o + f * n) => p = f * n
	// Any other point (r) will be on the plane if:
	// (r - p) * n = 0
	// Meaning that the vector from point r to point p dotted (dot product)
	// with the normal, should sum to 0, since it should be
	// perpendicular to the normal.
	private Vector normal;
	private double offset;

	public Plane(String[] params) {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				Double.parseDouble(params[3]), Integer.parseInt(params[4]));
	}

	public Plane(Vector normal, double offset, int materialIndex) {
		super(materialIndex);
		this.normal = normal.normalize();
		this.offset = offset;
	}

	@Override
	public double hit(Ray ray) {
		if (Vector.dot(ray.getDirection(), this.normal) == 0) {
			return 0;
		}

		// t = (1/d_r*n) * (f - p_r * n)
		double t = (this.offset - Vector.dot(ray.getOrigin(), this.normal))
				/ Vector.dot(ray.getDirection(), this.normal);

		return t;
	}

	@Override
	public Vector getNormalAt(Vector point) {
		return new Vector(this.normal).normalize();
	}
}