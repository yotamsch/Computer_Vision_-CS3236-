package scene;

import main.Ray;
import utility.Vector;

public class Camera {
	private Vector position, lookAt, up;
	private double screenDist, screenWidth, screenHeight;
	private Vector u, v, w, L;

	public Camera(String[] params) {
		this(new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]), Double.parseDouble(params[2])),
				new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]), Double.parseDouble(params[5])),
				new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]), Double.parseDouble(params[8])),
				Double.parseDouble(params[9]), Double.parseDouble(params[10]));
	}

	public Camera(Vector position, Vector lookAt, Vector up, double screenDist, double screenWidth) {
		this.position = position;
		this.lookAt = lookAt;
		this.up = up;
		this.screenDist = screenDist;
		this.screenWidth = screenWidth;
	}

	public Vector getPosition() {
		return position;
	}

	public Vector getUpDirection() {
		return v;
	}
	
	public Vector getRightDirection() {
		return u;
	}
	
	public Vector getTowardsDirection() {
		return w;
	}

	public Ray getRayPerspective(double x_pos, double y_pos) {
		Vector pixelLocation = new Vector(L)
				.add(new Vector(u).mul(x_pos * screenWidth))
				.sub(new Vector(v).mul(y_pos * screenHeight));
		Vector rayDirection = new Vector(pixelLocation).sub(this.position);
		
		return new Ray(new Vector(position), rayDirection);
	}

	public void setAspectRatio(double ratio) {
		this.screenHeight = this.screenWidth * ratio;
	}

	public void setupCamera() {
		// Based on a method in:
		// http://web.cse.ohio-state.edu/~shen.94/681/Site/Slides_files/basic_algo.pdf

		// Calculate eye coordinate system
		this.w = new Vector(this.lookAt).sub(this.position).normalize();
		this.u = Vector.cross(this.up, w).normalize();
		this.v = Vector.cross(w, u).normalize();
		// Top left corner
		this.L = new Vector(this.position)
				.add(new Vector(w).mul(this.screenDist))
				.sub(new Vector(u).mul(this.screenWidth / 2))
				.add(new Vector(v).mul(this.screenHeight / 2));
	}
}
