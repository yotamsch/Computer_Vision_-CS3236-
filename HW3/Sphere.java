
public class Sphere extends Shape {
	Vector center;
	double radius;

	public Sphere(String[] params) {
		// TODO: might need -1
		super(Integer.parseInt(params[4]));
		
		this.center = new Vector(params[0], params[1], params[2]);
		this.radius = Double.parseDouble(params[3]);
	}

	// TODO is this even correct?
	public Vector getNormal(Vector intersectionPoint) {
		Vector result = new Vector(intersectionPoint);
		result.sub(this.center);
		result.normalize();
		return result;
	}
}