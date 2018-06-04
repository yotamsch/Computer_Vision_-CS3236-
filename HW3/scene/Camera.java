package scene;

import main.RayTracer.Ray;
import utility.Vector;

public class Camera {	
	private Vector position, lookAt, up;
	private double screenDist, screenWidth, screenHeight;
	private Vector u, v, w, L;

	public Camera(Vector position, Vector lookAt, Vector up, double screenDist, double screenWidth, double aspectRatio) {
		this.position = position;
		this.lookAt = lookAt;
		this.up = up;
		this.screenDist = screenDist;
		this.screenWidth = screenWidth;
		this.screenHeight = screenWidth * aspectRatio;

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
	
	public Vector getPosition() {
		return this.position;
	}
	
	public Ray getRayPerspective(double x_pos, double y_pos) {
		Vector pixelLocation = new Vector(L).add(new Vector(u).mul(x_pos * screenWidth)).add(new Vector(v).mul(y_pos * screenHeight));
		Vector rayDirection = new Vector(pixelLocation).sub(this.position);
		
		return new Ray(new Vector(position), rayDirection);
	}
	
	public Ray getRayOrthographic(double x_pox, double y_pos) {
		double z_pos = Vector.dot(this.position, new Vector(0,0,1));
		double zoom_factor = screenDist;
		return new Ray(new Vector(zoom_factor * x_pox ,zoom_factor * y_pos, z_pos), new Vector(lookAt));
	}
}
