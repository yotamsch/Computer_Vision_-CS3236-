package scene;
import main.RayTracer.Ray;
import utility.Vector;

public class Camera {
	Vector position, lookAt, up;
	double screenDist, screenWidth;

	public Camera(Vector position, Vector lookAt, Vector up, double screenDist, double screenWidth) {
		this.position = position;
		this.lookAt = lookAt;
		this.up = up;
		this.screenDist = screenDist;
		this.screenWidth = screenWidth;
	}
	
	public Ray getRay(double x, double y) {
		Vector w = position.sub(lookAt);
		w.normalize();
		Vector u = up.cross(w);
		u.normalize();
		Vector v = w.cross(u);
		v.normalize();
		
		return new Ray(new Vector(position), u.mul(x).add(v.mul(y).sub(w.mul(screenDist))));
	}
}
