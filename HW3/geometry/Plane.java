package geometry;

import java.io.IOException;

import main.Ray;
import utility.Color;
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
	private double scale = 1e-1;

	public Plane(String[] params) throws IOException {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				Double.parseDouble(params[3]), Integer.parseInt(params[4]));
		if (params.length > 5) {
			this.initializeTexture(params[5]);
		}
		if (params.length > 6) {
			this.scale *= Double.parseDouble(params[6]);
		}
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

	@Override
	public Color getTextureAt(Vector point) {
		// calc u,v positions
		Vector vP = new Vector(point.getX(), point.getY(), point.getZ() - (this.normal.getZ() != 0 ? this.offset / this.normal.getZ() : 0));
		Vector vK = new Vector(0,0,1);
		double cosT = Vector.cos(vK, this.normal);
		double sinT = Vector.sin(vK, this.normal);
		Vector vU = Vector.cross(this.normal, vK).div(this.normal.norm());
		double u1 = vU.getX();
		double u2 = vU.getY();
		
		double u = vP.getX() * (cosT + u1*u1 * (1 - cosT)) + vP.getY() * (u1*u2*(1-cosT)) + vP.getZ() * (u2*sinT);
		double v = vP.getX() * (u1*u2*(1-cosT)) + vP.getY() * (cosT + u1*u1 * (1 - cosT)) + vP.getZ() * (-u1*sinT);
		// double w = vP.getX() * (-u2*sinT) + vP.getY() * (u1*sinT) + vP.getZ() * (cosT);


		return this.getRGBAt(Math.abs(u *scale), Math.abs(v*scale));
	}
}