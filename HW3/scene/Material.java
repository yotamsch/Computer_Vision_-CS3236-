package scene;
import utility.Color;

public class Material {
	Color diffuse, specular, reflection;
	double phong, tranparency;

	public Material(Color diffuse, Color specular, Color reflection, double phong, double transperacy) {
		this.diffuse = diffuse;
		this.specular = specular;
		this.reflection = reflection;
		this.phong = phong;
		this.tranparency = transperacy;
	}
}