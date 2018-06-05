package main;

import java.awt.Transparency;
import java.awt.color.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import geometry.Plane;
import geometry.Shape;
import geometry.Sphere;
import geometry.Triangle;
import scene.Camera;
import scene.Light;
import scene.Material;
import utility.Color;
import utility.Vector;

/**
 * Main class for ray tracing exercise.
 */
public class RayTracer {
	// Static parameters
	public static final Random rand = new Random();
	public static final boolean isPerspective = true;
	public static final float epsilon = 1E-5F;

	// Output image parameters
	int imageWidth;
	int imageHeight;

	// Ray trace parameters
	World scene;
	Color backgroundColor;
	int shadowRaysNum;
	int recursionsMaxLevel;
	int superSamplingLevel;

	/**
	 * Custom exception for Ray Tracing errors.
	 */
	@SuppressWarnings({ "serial" })
	public static class RayTracerException extends Exception {
		public RayTracerException(String msg) {
			super(msg);
		}
	}

	/**
	 * The ray class. Defined by on origin (p) and a direction (d). A point (r) on
	 * the ray with agree with the function: r = p + t * d Where t is a number
	 * representing the "distance" or "offset" on the ray. Basically, a point is
	 * some offset from the origin with the direction of the ray.
	 */
	public static class Ray {
		private Vector origin, direction;

		public Ray(Vector origin, Vector direction) {
			this.origin = origin;
			this.direction = direction;

			this.direction.normalize();
		}

		public Vector getOrigin() {
			return origin;
		}

		public Vector getDirection() {
			return direction;
		}

		@Override
		public String toString() {
			return String.format("<Ray: [%s, %s]>", this.origin, this.direction);
		}
	}

	public static class World {
		public Camera camera; // the camera
		public List<Light> lights; // the lights
		public List<Shape> shapes; // the shapes
		public List<Material> materials; // the materials

		public World() {
			this.lights = new ArrayList<Light>();
			this.shapes = new ArrayList<Shape>();
			this.materials = new ArrayList<Material>();
		}
	}

	public static class Intersection {
		public Shape shape;
		public Vector point;

		public Intersection() {
			this.shape = null;
			this.point = null;
		}
	}

	/**
	 * Runs the ray tracer. Takes scene file, output image file and image size as
	 * input.
	 */
	public static void main(String[] args) {

		// String[] files = { "Test", "Test2", "Pool", "Triangle", "Transparency",
		// "Room1" };
		String[] files = { "Test2" };
		for (String file : files) {
			try {

				RayTracer tracer = new RayTracer();

				// Default values:
				tracer.imageWidth = 500;
				tracer.imageHeight = 500;

				if (args.length < 2)
					throw new RayTracerException(
							"Not enough arguments provided. Please specify an input scene file and an output image file for rendering.");

				String sceneFileName;
				String outputFileName;

				if (files.length > 0) {
					sceneFileName = file.concat(".txt");
					outputFileName = file.concat(".png");
				} else {
					sceneFileName = args[0];
					outputFileName = args[1];

					if (args.length > 3) {
						tracer.imageWidth = Integer.parseInt(args[2]);
						tracer.imageHeight = Integer.parseInt(args[3]);
					}
				}

				// Parse scene file:
				tracer.parseScene(sceneFileName);

				// Render scene:
				tracer.renderScene(outputFileName);

			} catch (RayTracerException e) {
				System.out.println(e.getMessage());
			} catch (IOException e) {
				System.out.println(e.getMessage());
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
	}

	/**
	 * Parses the scene file and creates the scene.
	 */
	public void parseScene(String sceneFileName) throws IOException {
		FileReader fr = new FileReader(sceneFileName);

		BufferedReader r = new BufferedReader(fr);
		String line = null;
		int lineNum = 0;
		this.scene = new World();
		System.out.println("Started parsing scene file " + sceneFileName);

		while ((line = r.readLine()) != null) {
			line = line.trim();
			++lineNum;

			if (line.isEmpty() || (line.charAt(0) == '#')) { // This line in the scene file is a comment
				continue;
			} else {
				String code = line.substring(0, 3).toLowerCase();
				// Split according to white space characters:
				String[] params = line.substring(3).trim().toLowerCase().split("\\s+");

				// TODO: should we verify that the input is correct?
				if (code.equals("cam")) {
					// Camera
					this.scene.camera = new Camera(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]),
									Double.parseDouble(params[5])),
							new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]),
									Double.parseDouble(params[8])),
							Double.parseDouble(params[9]), Double.parseDouble(params[10]),
							this.imageHeight / this.imageWidth);
					System.out.println(String.format("Parsed camera parameters (line %d)", lineNum));
				} else if (code.equals("set")) {
					// Scene parameters
					this.backgroundColor = new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]),
							Float.parseFloat(params[2]));
					this.shadowRaysNum = Integer.parseInt(params[3]);
					this.recursionsMaxLevel = Integer.parseInt(params[4]);
					this.superSamplingLevel = Integer.parseInt(params[5]);
					System.out.println(String.format("Parsed general settings (line %d)", lineNum));
				} else if (code.equals("mtl")) {
					// Material
					this.scene.materials.add(new Material(
							new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]),
									Float.parseFloat(params[2])),
							new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]),
									Float.parseFloat(params[5])),
							new Color(Float.parseFloat(params[6]), Float.parseFloat(params[7]),
									Float.parseFloat(params[8])),
							Float.parseFloat(params[9]), Float.parseFloat(params[10])));
					System.out.println(String.format("Parsed material (line %d)", lineNum));
				} else if (code.equals("sph")) {
					// Sphere
					this.scene.shapes.add(new Sphere(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							Double.parseDouble(params[3]), Integer.parseInt(params[4])));
					System.out.println(String.format("Parsed sphere (line %d)", lineNum));
				} else if (code.equals("pln")) {
					// Plane
					this.scene.shapes.add(new Plane(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							Double.parseDouble(params[3]), Integer.parseInt(params[4])));
					System.out.println(String.format("Parsed plane (line %d)", lineNum));
				} else if (code.equals("trg")) {
					// Triangle
					this.scene.shapes.add(new Triangle(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]),
									Double.parseDouble(params[5])),
							new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]),
									Double.parseDouble(params[8])),
							Integer.parseInt(params[9])));
					System.out.println(String.format("Parsed triangle (line %d)", lineNum));
				} else if (code.equals("lgt")) {
					// Light
					this.scene.lights.add(new Light(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]),
									Float.parseFloat(params[5])),
							Float.parseFloat(params[6]), Float.parseFloat(params[7]), Double.parseDouble(params[8])));
					System.out.println(String.format("Parsed light (line %d)", lineNum));
				} else {
					System.out.println(String.format("ERROR: Did not recognize object: %s (line %d)", code, lineNum));
				}
			}
		}

		// TODO: check that everything is defined (camera, materials, etc...)

		System.out.println("Finished parsing scene file " + sceneFileName);

		r.close();
	}

	/**
	 * Renders the loaded scene and saves it to the specified file location.
	 */
	public void renderScene(String outputFileName) {
		long startTime = System.currentTimeMillis();

		// Create a byte array to hold the pixel data:
		byte[] rgbData = new byte[this.imageWidth * this.imageHeight * 3];

		// Base looping over the pixels
		for (int y = 0; y < this.imageHeight; y++) {
			for (int x = 0; x < this.imageWidth; x++) {
				// Initialize to a black color
				Color clr = new Color(0.0F, 0.0F, 0.0F);

				// Support for anti-aliasing
				for (int i = 0; i < this.superSamplingLevel; i++) {
					for (int j = 0; j < this.superSamplingLevel; j++) {
						Ray ray;
						double trace_x, trace_y;

						// Adding random noise for anti-aliasing not including edges
						float randJitter = rand.nextFloat();
						randJitter = randJitter == 1.0F || randJitter == 0.0F ? 0.5F : randJitter;

						if (isPerspective) {
							// Perspective Ray
							trace_x = (x + (j + randJitter) / this.superSamplingLevel) / this.imageWidth;
							trace_y = (y + (i + randJitter) / this.superSamplingLevel) / this.imageHeight;
							ray = this.scene.camera.getRayPerspective(trace_x, trace_y);
						} else {
							// Orthographic Ray
							trace_x = (x - this.imageWidth / 2 + (j + randJitter) / this.superSamplingLevel)
									/ (this.imageWidth / 2);
							trace_y = (y - this.imageHeight / 2 + (i + randJitter) / this.superSamplingLevel)
									/ (this.imageHeight / 2);
							ray = this.scene.camera.getRayOrthographic(trace_x, -1 * trace_y);
						}

						Color tempClr = this.traceColor(ray, 0);

						clr.add(tempClr);

					}
				}

				clr.div((float) Math.pow(this.superSamplingLevel, 2));

				// Set the color of the output image
				byte[] bClr = clr.getRGB();
				rgbData[(y * this.imageWidth + x) * 3] = bClr[0];
				rgbData[(y * this.imageWidth + x) * 3 + 1] = bClr[1];
				rgbData[(y * this.imageWidth + x) * 3 + 2] = bClr[2];
			}
		}

		long endTime = System.currentTimeMillis();
		Long renderTime = endTime - startTime;

		// The time is measured for your own convenience.
		// Rendering speed will not affect your score,
		// unless it is exceptionally slow (more than a couple of minutes)
		System.out.println("Finished rendering scene in " + renderTime.toString() + " milliseconds.");

		// This is already implemented, and should work without adding any code.
		saveImage(this.imageWidth, rgbData, outputFileName);

		System.out.println("Saved file " + outputFileName);

	}

	////////////// FUNCTIONS TO SAVE IMAGES IN PNG FORMAT /////////////
	///////////////////////////////////////////////////////////////////

	/*
	 * Saves RGB data as an image in png format to the specified location.
	 */
	public static void saveImage(int width, byte[] rgbData, String fileName) {
		try {

			BufferedImage image = bytes2RGB(width, rgbData);
			ImageIO.write(image, "png", new File(fileName));

		} catch (IOException e) {
			System.out.println("ERROR SAVING FILE: " + e.getMessage());
		}

	}

	/*
	 * Producing a BufferedImage that can be saved as PNG from a byte array of RGB
	 * values.
	 */
	public static BufferedImage bytes2RGB(int width, byte[] buffer) {
		int height = buffer.length / width / 3;
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		SampleModel sm = cm.createCompatibleSampleModel(width, height);
		DataBufferByte db = new DataBufferByte(buffer, width * height);
		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		BufferedImage result = new BufferedImage(cm, raster, false, null);

		return result;
	}

	////////////////////////// RAY TRACING /////////////////////////
	////////////////////////////////////////////////////////////////

	/**
	 * This function uses recursion to recursively calculate the accumulated color
	 * of a position viewed from Ray.
	 * 
	 * @param ray
	 *            - The ray to search for
	 * @param currRecursionLevel
	 *            - The current recursion level (starts from 0)
	 * @return The calculated color
	 */
	public Color traceColor(Ray ray, int currRecursionLevel) {
		Color outColor = new Color(this.backgroundColor);

		if (currRecursionLevel >= this.recursionsMaxLevel) {
			return outColor;
		}
		currRecursionLevel += 1;

		Intersection firstIntersected = getFirstIntersection(ray);

		if (firstIntersected != null) {
			Material objMat = this.scene.materials.get(firstIntersected.shape.getMaterialIndex() - 1);
			Vector pointNormal = firstIntersected.shape.getNormalAt(firstIntersected.point);

			// get refraction
			Ray refractedRay = firstIntersected.shape.getRefractedRay(ray.direction, firstIntersected.point, 1);
			outColor = traceColor(refractedRay, currRecursionLevel).mul(objMat.getTranparency());

			for (Light light : this.scene.lights) {
				Vector lightRayDirection = new Vector(firstIntersected.point).sub(light.getPosition()).normalize();

				// shadows
				Vector pW = new Vector(lightRayDirection);
				Vector pU = Vector.cross(pW, new Vector(0, 1, 0)).normalize();
				Vector pV = Vector.cross(pW, pU).normalize();
				int lightHitCount = 0;
				for (int i = 0; i < this.shadowRaysNum; i++) {
					for (int j = 0; j < this.shadowRaysNum; j++) {
						double randJitter = rand.nextDouble();
						Vector lightPoint = new Vector(light.getPosition())
								.add(new Vector(pV).mul(((i - this.shadowRaysNum / 2 + randJitter) / this.shadowRaysNum)
										* light.getRadius()))
								.add(new Vector(pU).mul(((j - this.shadowRaysNum / 2 + randJitter) / this.shadowRaysNum)
										* light.getRadius()));
						Ray lightRay = new Ray(lightPoint, new Vector(firstIntersected.point).sub(lightPoint));
						Intersection lightIntersection = getFirstIntersection(lightRay);
						if (Vector.distance(lightIntersection.point, firstIntersected.point) < epsilon) {
							lightHitCount++;
						}
					}
				}

				Color lightColor = new Color(light.getColor())
						.mul((float) (1
								- (1 - lightHitCount / Math.pow(this.shadowRaysNum, 2)) * light.getShadowIntensity()))
						.mul(1 - objMat.getTranparency());
				Color diffuse = new Color(objMat.getDiffuse()).mul(lightColor);
				Color speculr = new Color(objMat.getSpecular()).mul(lightColor).mul(light.getSpecularIntensity());

				// diffuse
				diffuse.mul(Vector.cos(new Vector(lightRayDirection).mul(-1), pointNormal)).clamp();

				// specular
				speculr.mul((float) Math.pow(
						Vector.cos(
								firstIntersected.shape.getReflectedRay(lightRayDirection, firstIntersected.point)
										.getDirection(),
								new Vector(this.scene.camera.getPosition()).sub(firstIntersected.point).normalize()),
						objMat.getPhong())).clamp();

				diffuse.clamp();
				speculr.clamp();

				// adding it all together
				outColor.add(diffuse).add(speculr);
			}
			// reflection
			Ray reflectedRay = firstIntersected.shape.getReflectedRay(ray.getDirection(), firstIntersected.point);
			Color reflectedColor = traceColor(reflectedRay, currRecursionLevel).mul(objMat.getReflection()).clamp();

			outColor.add(reflectedColor);
		}

		return outColor.clamp();
	}

	/**
	 * The function returns the first object intersected with the ray. In other
	 * words, the closest object to the origin of the ray.
	 * 
	 * @param ray
	 *            The ray to check for
	 * @return The intersection found, null if nothing intersected
	 */
	public Intersection getFirstIntersection(Ray ray) {
		return getIntersection(ray, true);
	}

	/**
	 * Gets the first found intersection (not always closest).
	 * 
	 * @param ray
	 *            The ray to check for
	 * @return The intersection found, null if nothing intersected
	 */
	public Intersection getAnyIntersection(Ray ray) {
		return getIntersection(ray, false);
	}

	/**
	 * Gets an intersection if any occurred. Based on the isClosestSearch parameter,
	 * either searches for closest or stops after the first.
	 * 
	 * @param ray
	 *            The ray to search with
	 * @param isClosestSearch
	 *            Should search for closest or first found
	 * @return The intersection found, null if nothing intersected
	 */
	public Intersection getIntersection(Ray ray, boolean isClosestSearch) {
		double nearestHitValue = Double.MAX_VALUE;
		Intersection result = new Intersection();

		for (Shape shape : this.scene.shapes) {
			double tempHitValue = shape.hit(ray);
			// When the hit value is lower than epsilon (positive number)
			// it means that the object is in an "unseen" position, thus ignored
			// (either behind the camera or too close to it)
			if (tempHitValue >= epsilon && tempHitValue < nearestHitValue) {
				nearestHitValue = tempHitValue;
				result.shape = shape;
				result.point = new Vector(ray.origin).add(new Vector(ray.direction).mul(nearestHitValue));
				if (!isClosestSearch) {
					return result;
				}
			}
		}
		if (result.shape != null)
			return result;
		return null;
	}

}
