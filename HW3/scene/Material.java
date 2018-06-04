package scene;

import utility.Color;

public class Material {
	private Color diffuse, specular, reflection;
	private float phong, tranparency;

	public Material(Color diffuse, Color specular, Color reflection, float phong, float transperacy) {
		this.diffuse = diffuse;
		this.specular = specular;
		this.reflection = reflection;
		this.phong = phong;
		this.tranparency = transperacy;
	}
	
	public Color getDiffuse() {
		return new Color(diffuse).mul(1 - this.tranparency);
	}

	public Color getSpecular() {
		return new Color(this.specular).mul(1 - this.tranparency);
	}

	public Color getReflection() {
		return new Color(this.reflection);
	}

	public float getPhong() {
		return phong;
	}

	public float getTranparency() {
		return tranparency;
	}
}