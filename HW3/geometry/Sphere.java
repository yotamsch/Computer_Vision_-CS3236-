package geometry;

import java.io.IOException;

import main.Ray;
import utility.Color;
import utility.Vector;

public class Sphere extends Shape {
	// NOTE:
	// The sphere has a center point (p) and a radius (R).
	// A point (r) is on a sphere if it agrees with the function:
	// (r - p) * (r - p) = R ^ 2
	private Vector center;
	private double radius;
	private double rotateY = 0;

	public Sphere(String[] params) throws IOException {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				Double.parseDouble(params[3]), Integer.parseInt(params[4]));
		if (params.length > 5) {
			this.initializeTexture(params[5]);
		}
		if (params.length > 6) {
			this.rotateY = Double.parseDouble(params[6]);
		}
	}

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

	@Override
	public Color getTextureAt(Vector point) {
		// calc u,v positions
		Vector vN = new Vector(0,-1,0);
		Vector vE = new Vector(1,0,0);
		double u = 0, v = 0;
		Vector vP = new Vector(point).sub(this.center).normalize();
		double phi = Math.acos(-1 * (Vector.dot(vN, vP)));
		v = phi / Math.PI;
		double xi = (Vector.dot(vP,vE)) / Math.sin(phi);
		double t = Math.acos(xi) / (2 * Math.PI); 
		if (Vector.dot(Vector.cross(vN, vE), vP) > 0) {
			u = t;
		} else {
			u = 1-t;
		}

		return this.getRGBAt(u, v, this.rotateY);
	}
}