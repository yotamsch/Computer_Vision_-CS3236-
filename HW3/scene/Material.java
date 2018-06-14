package scene;

import utility.Color;

public class Material {
	private Color diffuse, specular, reflection;
	private float phong, tranparency;
	private float refractionRatio = 1;
	private float specularNoise = 0;

	public Material(String[] params) {
		this(new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]), Float.parseFloat(params[2])),
				new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]), Float.parseFloat(params[5])),
				new Color(Float.parseFloat(params[6]), Float.parseFloat(params[7]), Float.parseFloat(params[8])),
				Float.parseFloat(params[9]), Float.parseFloat(params[10]));
		if (params.length > 11) {
			this.refractionRatio = Float.parseFloat(params[11]);
		}
		if (params.length > 12) {
			this.specularNoise = Float.parseFloat(params[12]);
		}
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
		float noise = this.specularNoise == 0 ? 0F : (float)main.RayTracer.rand.nextFloat() * this.specularNoise;
		return new Color(this.specular).add(new Color(noise,noise,noise));
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

	public float getRefractionRatio() { return refractionRatio; }
	
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