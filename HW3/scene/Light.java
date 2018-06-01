package scene;
import utility.Color;
import utility.Vector;

public class Light {
	Vector position;
	Color color;
	float specularIntensity, shadowIntensity;
	double radius;

	public Light(Vector position, Color color, float specularIntensity, float shadowIntensity, double radius) {
		this.position = position;
		this.color = color;
		this.specularIntensity = specularIntensity;
		this.shadowIntensity = shadowIntensity;
		this.radius = radius;
	}
}
