
public class Material {
	Color diffuse;
	Color specular;
	Color reflection;
	double phong;
	double tranparency;

	public Material(String[] params) {
		this.diffuse = new Color(params[0], params[1], params[2]);
		this.specular = new Color(params[3], params[4], params[5]);
		this.reflection = new Color(params[6], params[7], params[8]);
		this.phong = Double.parseDouble(params[9]);
		this.tranparency = Double.parseDouble(params[10]);
	}
}