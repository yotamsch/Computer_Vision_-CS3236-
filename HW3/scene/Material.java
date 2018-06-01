package scene;
import utility.Color;

public class Material {
	Color diffuse, specular, reflection;
	float phong, tranparency;

	public Material(Color diffuse, Color specular, Color reflection, float phong, float transperacy) {
		this.diffuse = diffuse;
		this.specular = specular;
		this.reflection = reflection;
		this.phong = phong;
		this.tranparency = transperacy;
	}
	
	public Color getBaseColor(Light light) {
		Color clr = new Color(diffuse);
		clr.mul(light.color);
		return clr;
	}
	
	public Color getSpecularColor(Light light) {
		Color clr = new Color(light.color);
		clr.mul(light.specularIntensity);
		clr.mul(specular);
		return clr;
	}
	
	public Color getReflectionColor(Color other) {
		Color clr = new Color(reflection);
		clr.mul(other);
		return clr;
	}
}