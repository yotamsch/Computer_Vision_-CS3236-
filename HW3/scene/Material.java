package scene;

import utility.Color;

public class Material {
	private Color diffuse, specular, reflection;
	private float phong, tranparency;

	public Material(String[] params) {
		this(new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2])),
				new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]), Float.parseFloat(params[5])),
				new Color(Float.parseFloat(params[6]), Float.parseFloat(params[7]), Float.parseFloat(params[8])),
				Float.parseFloat(params[9]), Float.parseFloat(params[10]));
	}

	public Material(Color diffuse, Color specular, Color reflection, float phong, float transperacy) {
		this.diffuse = diffuse;
		this.specular = specular;
		this.reflection = reflection;
		this.phong = phong;
		this.tranparency = transperacy;
	}
	
	public Color getDiffuse() {
		return new Color(this.diffuse);
	}

	public Color getSpecular() {
		return new Color(this.specular);
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
	
	public boolean isRefractive() {
		return this.tranparency > 0;
	}
	
	public boolean isReflective() {
		return !this.reflection.isBlack();
	}
	
	public boolean isSpecular() {
		return !this.specular.isBlack();
	}
}