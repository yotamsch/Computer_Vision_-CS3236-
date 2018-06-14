package utility;

public class Vector {
	private double x, y, z;

	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(Vector other) {
		this(other.x, other.y, other.z);
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public Vector add(Vector other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
		return this;
	}

	public Vector sub(Vector other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
		return this;
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

	public double norm() {
		return Math.sqrt(Vector.dot(this, this));
	}

	public Vector normalize() {
		return div(this.norm());
	}

	public static double dot(Vector v, Vector u) {
		return v.x * u.x + v.y * u.y + v.z * u.z;
	}

	public static Vector cross(Vector v, Vector u) {
		double cross_x = v.y * u.z - v.z * u.y;
		double cross_y = v.z * u.x - v.x * u.z;
		double cross_z = v.x * u.y - v.y * u.x;
		return new Vector(cross_x, cross_y, cross_z);
	}

	public static float cos(Vector v, Vector u) {
		float res = (float) dot(v, u);
		res /= v.norm() * u.norm();
		return res;
	}

	public static float sin(Vector v, Vector u) {
		float cos = cos(v, u);
		return (float) Math.sqrt(1 - Math.pow(cos, 2));
	}

	@Override
	public String toString() {
		return String.format("<Vector: [%.4f,%.4f,%.4f]>", x, y, z);
	}
}
