package geometry;

import main.Ray;
import utility.Vector;

public class Triangle extends Shape {
	// NOTE:
	// The triangle is defined by the position of the vertices.
	// Meaning that v1, v2, v3 are three points which construct the triangle.
	// Thus a point (r) which is on the triangle satisfies:
	// 1. On the plane defined by (v2 - v1) , (v3 - v1)
	// -> Using the normal (n) of the plane
	// 2. The point is inside the triangle edges, meaning:
	// -> [(v2 - v1) x (r - v1)] * n >= 0
	// -> [(v3 - v2) x (r - v2)] * n >= 0
	// -> [(v1 - v3) x (r - v3)] * n >= 0
	// To find the intersection point (distance) it is the same as that of a plane.
	private Vector v1, v2, v3;

	public Triangle(String[] params) {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]), Double.parseDouble(params[5])),
				new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]), Double.parseDouble(params[8])),
				Integer.parseInt(params[9]));
	}

	public Triangle(Vector v1, Vector v2, Vector v3, int materialIndex) {
		super(materialIndex);
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
	}

	@Override
	public double hit(Ray ray) {
		// OPTION 1
		// Based on the method found in:
		// https://courses.cs.washington.edu/courses/csep557/10au/lectures/triangle_intersection.pdf
		Vector P_n = Vector.cross(new Vector(v2).sub(v1), new Vector(v3).sub(v1)).normalize();

		// Check that the point is on the plane P with normal P_n and point v1
		if (Vector.dot(ray.getDirection(), P_n) == 0) {
			return 0;
		}

		// t = (1/d_r*n) * (p_p * n - p_r * n)
		double t = (Vector.dot(v1, P_n) - Vector.dot(ray.getOrigin(), P_n)) / Vector.dot(ray.getDirection(), P_n);
		Vector r = new Vector(ray.getOrigin()).add(new Vector(ray.getDirection()).mul(t));

		// Check that the point is in the triangle
		// If all conditions apply:
		// [(v2 - v1) x (r - v1)] * n >= 0
		// [(v3 - v2) x (r - v2)] * n >= 0
		// [(v1 - v3) x (r - v3)] * n >= 0
		if ((Vector.dot(Vector.cross(new Vector(v2).sub(v1), new Vector(r).sub(v1)), P_n) >= 0)
				&& (Vector.dot(Vector.cross(new Vector(v3).sub(v2), new Vector(r).sub(v2)), P_n) >= 0)
				&& (Vector.dot(Vector.cross(new Vector(v1).sub(v3), new Vector(r).sub(v3)), P_n) >= 0)) {
			return t;
		}
		return 0;
	}

	@Override
	public Vector getNormalAt(Vector point) {
		return Vector.cross(new Vector(v2).sub(v1), new Vector(v3).sub(v1)).normalize();
	}
}
