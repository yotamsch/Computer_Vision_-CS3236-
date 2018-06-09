package main;

import java.awt.Transparency;
import java.awt.color.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
	 * Runs the ray tracer. Takes scene file, output image file and image size as
	 * input.
	 */
	public static void main(String[] args) {

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

			sceneFileName = args[0];
			outputFileName = args[1];

			if (args.length > 3) {
				tracer.imageWidth = Integer.parseInt(args[2]);
				tracer.imageHeight = Integer.parseInt(args[3]);
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

	/**
	 * Parses the scene file and creates the scene.
	 * 
	 * @throws RayTracerException
	 */
	public void parseScene(String sceneFileName) throws IOException, RayTracerException {
		FileReader fr = new FileReader(sceneFileName);

		BufferedReader r = new BufferedReader(fr);
		String line = null;
		int lineNum = 0;
		this.scene = new World();
		System.out.println("Started parsing scene file " + sceneFileName);

		try {
			while ((line = r.readLine()) != null) {
				line = line.trim();
				++lineNum;

				if (line.isEmpty() || (line.charAt(0) == '#')) { // This line in the scene file is a comment
					continue;
				} else {
					String code = line.substring(0, 3).toLowerCase();
					// Split according to white space characters:
					String[] params = line.substring(3).trim().toLowerCase().split("\\s+");

					if (code.equals("cam")) {
						// Camera
						this.scene.camera = new Camera(params);
						this.scene.camera.setAspectRatio(this.imageHeight / this.imageWidth);
						this.scene.camera.setupCamera();
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
						this.scene.materials.add(new Material(params));
						System.out.println(String.format("Parsed material (line %d)", lineNum));
					} else if (code.equals("sph")) {
						// Sphere
						this.scene.shapes.add(new Sphere(params));
						System.out.println(String.format("Parsed sphere (line %d)", lineNum));
					} else if (code.equals("pln")) {
						// Plane
						this.scene.shapes.add(new Plane(params));
						System.out.println(String.format("Parsed plane (line %d)", lineNum));
					} else if (code.equals("trg")) {
						// Triangle
						this.scene.shapes.add(new Triangle(params));
						System.out.println(String.format("Parsed triangle (line %d)", lineNum));
					} else if (code.equals("lgt")) {
						// Light
						this.scene.lights.add(new Light(params));
						System.out.println(String.format("Parsed light (line %d)", lineNum));
					} else {
						System.out
								.println(String.format("ERROR: Did not recognize object: %s (line %d)", code, lineNum));
					}
				}
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new RayTracerException(String.format("Invalid format in line: %d.", lineNum));
		} finally {
			r.close();
		}

		if (this.scene.camera == null) {
			throw new RayTracerException("Camera settings are missing.");
		}
		if (this.backgroundColor == null) {
			throw new RayTracerException("General settings are missing.");
		}
		for (Shape s : this.scene.shapes) {
			int mat = s.getMaterialIndex();
			if (mat < 0 || mat >= this.scene.materials.size()) {
				throw new RayTracerException(
						String.format("Invalid material for %s.", s.getClass().getName().split("geometry.")[1]));
			}
		}

		System.out.println("Finished parsing scene file " + sceneFileName);

	}

	/**
	 * Renders the loaded scene and saves it to the specified file location.
	 */
	public void renderScene(String outputFileName) {
		long startTime = System.currentTimeMillis();

		// Create a byte array to hold the pixel data:
		byte[] rgbData = new byte[this.imageWidth * this.imageHeight * 3];

		// Base looping over the pixels
		System.out.print("Progress:\t|--------------------|\n");
		System.out.print("\t\t ");

		for (int y = 0; y < this.imageHeight; y++) {
			for (int x = 0; x < this.imageWidth; x++) {
				// Initialize to a black color
				Color clr = new Color(0.0F, 0.0F, 0.0F);

				// Support for anti-aliasing
				for (int i = 0; i < this.superSamplingLevel; i++) {
					for (int j = 0; j < this.superSamplingLevel; j++) {
						Ray ray;
						double trace_x, trace_y;
						float rand1, rand2;

						// Adding random noise for anti-aliasing not including edges
						rand1 = rand.nextFloat();
						rand2 = rand.nextFloat();

						trace_x = (x + (j + rand1) / this.superSamplingLevel) / this.imageWidth;
						trace_y = (y + (i + rand2) / this.superSamplingLevel) / this.imageHeight;
						ray = this.scene.camera.getRayPerspective(trace_x, trace_y);

						// add the color (average later)
						clr.add(this.traceColor(ray, 0));
					}
				}

				clr.div((float) Math.pow(this.superSamplingLevel, 2));

				// Set the color of the output image
				byte[] bClr = clr.getRGB();
				rgbData[(y * this.imageWidth + x) * 3] = bClr[0];
				rgbData[(y * this.imageWidth + x) * 3 + 1] = bClr[1];
				rgbData[(y * this.imageWidth + x) * 3 + 2] = bClr[2];
			}

			double precent = 100 * ((double) y / this.imageHeight);
			if (precent % 5 == 0) {
				System.out.print("=");
			}
		}

		System.out.print("\nDone.\n");

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
		Color outColor = new Color(0, 0, 0);

		if (currRecursionLevel >= this.recursionsMaxLevel) {
			return outColor;
		}
		currRecursionLevel += 1;

		Intersection firstIntersected = getFirstIntersection(ray);

		if (firstIntersected != null) {

			Material mat = this.scene.materials.get(firstIntersected.shape.getMaterialIndex());

			Color baseColor = new Color(0, 0, 0);
			Color refracColor = new Color(0, 0, 0);
			Color reflecColor = new Color(0, 0, 0);

			// get refraction
			if (mat.isRefractive()) {
				Ray refractedRay = firstIntersected.shape.getRefractedRay(ray.getDirection(), firstIntersected.point,
						1);
				refracColor = traceColor(refractedRay, currRecursionLevel);
			}

			// reflection
			if (mat.isReflective()) {
				Ray reflectedRay = firstIntersected.shape.getReflectedRay(ray.getDirection(), firstIntersected.point);
				reflecColor = traceColor(reflectedRay, currRecursionLevel);
				reflecColor.mul(mat.getReflection());
			}

			// get diffuse and specular
			for (Light light : this.scene.lights) {
				Color currColor = getBaseColor(light, ray, firstIntersected);

				// soft shadows
				if (light.getShadowIntensity() != 0 && !currColor.isBlack()) {
					float lightPass = getLightPassPrecent(firstIntersected, light);
					currColor.mul(1 - light.getShadowIntensity() + light.getShadowIntensity() * lightPass);
				}
				baseColor.add(currColor);
			}
			baseColor.clamp();

			outColor.add(baseColor.mul(1 - mat.getTranparency()));
			outColor.add(refracColor.mul(mat.getTranparency()));
			outColor.add(reflecColor);
			outColor.clamp();

			return outColor;
		} else {
			return outColor.add(this.backgroundColor);
		}

	}

	private Color getBaseColor(Light light, Ray ray, Intersection firstIntersected) {
		Material mat = this.scene.materials.get(firstIntersected.shape.getMaterialIndex());
		Color baseColor = mat.getDiffuse();
		Color specularColor = mat.getSpecular();

		Vector lightDir = new Vector(light.getPosition()).sub(firstIntersected.point).normalize();
		Vector pointNormal = firstIntersected.shape.getNormalAt(firstIntersected.point);

		if (Vector.dot(ray.getDirection(), pointNormal) > 0) {
			pointNormal.mul(-1);
		}

		// add diffuse color
		float cosAngle = Vector.cos(lightDir, pointNormal);
		if (cosAngle <= 0) {
			baseColor.mul(0);
			return baseColor;
		}
		baseColor.mul(cosAngle);

		if (!specularColor.isBlack()) {
			Vector lightReflect = new Vector(pointNormal).mul(2 * Vector.dot(lightDir, pointNormal)).sub(lightDir);

			// add specular color
			cosAngle = Vector.cos(ray.getDirection(), lightReflect);
			if (cosAngle < 0) {
				cosAngle = (float) Math.pow(cosAngle, mat.getPhong());
				specularColor.mul(cosAngle * light.getSpecularIntensity());
				baseColor.add(specularColor);
			}
		}

		baseColor.mul(light.getColor());

		return baseColor;
	}

	private float getLightPassPrecent(Intersection firstIntersected, Light light) {

		Vector up = new Vector(this.scene.camera.getUpDirection());
		Vector N = new Vector(light.getPosition()).sub(firstIntersected.point);
		Vector U = Vector.cross(N, up).normalize();
		Vector V = Vector.cross(N, U).normalize();

		Vector lightPos = new Vector(light.getPosition());

		// <print>
		// System.out.println(up);
		// System.out.println(N);
		// System.out.println(U);
		// System.out.println(V);
		// System.out.println(lightPos);

		double lightLevel = 0;

		for (int i = 0; i < this.shadowRaysNum; i++) {
			for (int j = 0; j < this.shadowRaysNum; j++) {
				double rand1 = this.shadowRaysNum > 1 ? rand.nextDouble() : 0;
				double rand2 = this.shadowRaysNum > 1 ? rand.nextDouble() : 0;

				Vector lightPoint = new Vector(lightPos);
				lightPoint.add(new Vector(V)
						.mul((i + rand1 - (this.shadowRaysNum) / 2) * (light.getRadius() / this.shadowRaysNum)));
				lightPoint.add(new Vector(U)
						.mul((j + rand2 - (this.shadowRaysNum) / 2) * (light.getRadius() / this.shadowRaysNum)));

				Vector LightDir = new Vector(firstIntersected.point).sub(lightPoint);
				double T = LightDir.norm();

				Ray lightRay = new Ray(lightPoint, LightDir);
				lightLevel += getLightLevel(lightRay, T);
			}
		}

		return (float) (lightLevel / Math.pow(this.shadowRaysNum, 2));
	}

	private double getLightLevel(Ray lightRay, double T) {
		double lightLevel = 1;

		for (Shape s : this.scene.shapes) {
			double t = s.hit(lightRay);

			if (t > epsilon && t < T - epsilon) {
				double trans = this.scene.materials.get(s.getMaterialIndex()).getTranparency();
				// check if the hit object is blocking all light
				if (trans == 0) {
					return 0;
				}
				lightLevel *= trans;
			}
		}
		return lightLevel;
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
				result.tValue = nearestHitValue;
			}
		}
		if (result.shape != null) {
			result.point = new Vector(ray.getOrigin()).add(new Vector(ray.getDirection()).mul(result.tValue));
			return result;
		}
		return null;
	}

}
