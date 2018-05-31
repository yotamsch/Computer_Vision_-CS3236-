
public class Light {
	Vector position;
	Color color;
	double specularIntensity;
	double shadowIntensity;
	double radius;

	public Light(String[] params) {
		this.position = new Vector(params[0], params[1], params[2]);
		this.color = new Color(params[3], params[4], params[5]);
		this.specularIntensity = Double.parseDouble(params[6]);
		this.shadowIntensity = Double.parseDouble(params[7]);
		this.radius = Double.parseDouble(params[8]);
	}
}
