package utility;

public class Vector {
	double x, y, z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(Vector other) {
		this(other.x, other.y, other.z);
	}

	public Vector add(Vector other) {
		return new Vector(this.x + other.x, this.y + other.y, this.z + other.z);
	}

	public Vector sub(Vector other) {
		return new Vector(this.x - other.x, this.y - other.y, this.z - other.z);
	}
	
	public Vector mul(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}
	
	public Vector div(double scalar) {
		return mul(1.0 / scalar);
	}

	public void normalize() {
		div(Math.sqrt(this.dot(this)));
	}
	
	public double dot(Vector u) {
		return this.x * u.x + this.y * u.y + this.z * u.z;
	}

	public Vector cross(Vector u) {
		return new Vector(this.y * u.z - this.z * u.y, this.z * u.x - this.x * u.z, this.x * u.y - this.y * u.x);
	}

	// TODO: not sure if needed
	public Vector projection(Vector base) {
		Vector base_N = new Vector(base);
		base_N.normalize();
		double scalar = this.dot(base_N);
		return new Vector(base_N.x * scalar, base_N.y * scalar, base_N.z * scalar);
	}

	// TODO: not sure if needed
	public double distance(Vector from) {
		return Math.sqrt(Math.pow(this.x - from.x, 2) + Math.pow(this.y - from.y, 2) + Math.pow(this.z - from.z, 2));
	}

	public String toString() {
		return String.format("<Vector: [%.4f,%.4f,%.4f]>", x, y, z);
	}

}
