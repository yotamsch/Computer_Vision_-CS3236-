package scene;

import utility.Color;
import utility.Vector;

public class Light {
	private Vector position;
	private Color color;
	private float specularIntensity, shadowIntensity;
	private double radius;

	public Light(String[] params) {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]), Float.parseFloat(params[5])),
				Float.parseFloat(params[6]), Float.parseFloat(params[7]), Double.parseDouble(params[8]));
	}

	public Light(Vector position, Color color, float specularIntensity, float shadowIntensity, double radius) {
		this.position = position;
		this.color = color;
		this.specularIntensity = specularIntensity;
		this.shadowIntensity = shadowIntensity;
		this.radius = radius;
	}

	public Vector getPosition() {
		return position;
	}

	public void setPosition(Vector position) {
		this.position = position;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public float getSpecularIntensity() {
		return specularIntensity;
	}

	public void setSpecularIntensity(float specularIntensity) {
		this.specularIntensity = specularIntensity;
	}

	public float getShadowIntensity() {
		return shadowIntensity;
	}

	public void setShadowIntensity(float shadowIntensity) {
		this.shadowIntensity = shadowIntensity;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}
}
