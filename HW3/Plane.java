
public class Plane extends Shape {
	Vector normal;
	double offset;

	public Plane(String[] params) {
		// TODO: might need -1
		super(Integer.parseInt(params[4]));
		
		this.normal = new Vector(params[0], params[1], params[2]);
		this.offset = Double.parseDouble(params[3]);
	}
}