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
		return this.position;
	}

	public Vector getUpDirection() {
		return v;
	}

	public Ray getRayPerspective(double x_pos, double y_pos) {
		Vector pixelLocation = new Vector(L).add(new Vector(u).mul(x_pos * screenWidth))
				.add(new Vector(v).mul(y_pos * screenHeight));
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
		this.w = new Vector(this.position).sub(this.lookAt).normalize();
		this.u = Vector.cross(w, this.up).normalize();
		this.v = Vector.cross(w, u).normalize();
		// Center of view-port
		Vector C = new Vector(this.position).sub(new Vector(w).mul(screenDist));
		// Bottom left corner
		this.L = C.sub(new Vector(u).mul(this.screenWidth / 2)).sub(new Vector(v).mul(this.screenHeight / 2));
	}
}
