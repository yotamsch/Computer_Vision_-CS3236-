
public class Vector {
	protected double x;
	protected double y;
	protected double z;

	// Constructors
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector(String x, String y, String z) {
		this(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
	}

	public Vector(Vector other) {
		this(other.x, other.y, other.z);
	}

	// Add
	public void add(Vector other) {
		this.x += other.x;
		this.y += other.y;
		this.z += other.z;
	}

	public void add(double num) {
		this.x += num;
		this.y += num;
		this.z += num;
	}

	// Subtract
	public void sub(Vector other) {
		this.x -= other.x;
		this.y -= other.y;
		this.z -= other.z;
	}

	public void sub(double num) {
		this.x -= num;
		this.y -= num;
		this.z -= num;
	}

	// Multiply
	public void mul(double scalar) {
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
	}

	public void mul(Vector other) {
		this.x *= other.x;
		this.y *= other.y;
		this.z *= other.z;
	}

	// Norm
	public double norm() {
		return Math.sqrt(dotProduct(this, this));
	}

	// Normalize
	public void normalize() {
		this.mul(1.0 / this.norm());
	}

	// Distance
	public double distance(Vector from) {
		return Math.sqrt(Math.pow(this.x - from.x, 2) + Math.pow(this.y - from.y, 2) + Math.pow(this.z - from.z, 2));
	}

	// Trimming
	public void trim() {
		trim(0, 1);
	}

	public void trim(double min, double max) {
		// lower bound
		if (this.x < min)
			this.x = min;
		if (this.y < min)
			this.y = min;
		if (this.z < min)
			this.z = min;
		// upper bound
		if (this.x > max)
			this.x = max;
		if (this.y > max)
			this.y = max;
		if (this.z > max)
			this.z = max;
	}

	// Other
	public boolean isZero() {
		if (this.x == 0 && this.y == 0 && this.z == 0)
			return true;
		return false;
	}

	public String toString() {
		return String.format("<Vector: [%.4f,%.4f,%.4f]>", x, y, z);
	}

	// Static
	public static double dotProduct(Vector v, Vector u) {
		return v.x * u.x + v.y * u.y + v.z * u.z;
	}

	public static Vector crossProduct(Vector v, Vector u) {
		return new Vector(v.y * u.z - v.z * u.y, v.z * u.x - v.x * u.z, v.x * u.y - v.y * u.x);
	}

	public static Vector projection(Vector base, Vector other) {
		Vector result = new Vector(base);
		result.normalize();
		result.mul(dotProduct(result, other));
		return result;
	}
}
