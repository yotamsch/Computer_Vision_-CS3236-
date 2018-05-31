
public class Triangle extends Shape {
	Vector v1;
	Vector v2;
	Vector v3;

	public Triangle(String[] params) {
		// TODO: might need -1
		super(Integer.parseInt(params[9]));
		
		this.v1 = new Vector(params[0], params[1], params[2]);
		this.v2 = new Vector(params[3], params[4], params[5]);
		this.v3 = new Vector(params[6], params[7], params[8]);
	}
}
