
public class Camera {
	Vector position;
	Vector lookAt;
	Vector up;
	double screenDist;
	double screenWidth;

	public Camera(String[] params) {
		this.position = new Vector(params[0], params[1], params[2]);
		this.lookAt = new Vector(params[3], params[4], params[5]);
		this.up = new Vector(params[6], params[7], params[8]);
		this.screenDist = Double.parseDouble(params[9]);
		this.screenWidth = Double.parseDouble(params[10]);
	}
}
