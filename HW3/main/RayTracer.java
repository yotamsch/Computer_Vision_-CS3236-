package main;

import java.awt.Transparency;
import java.awt.color.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

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
	int imageWidth; // output image width
	int imageHeight; // output image height

	// Scene parameters
	Color backgroundColor; // the color when no shape detected
	double rootShadowRays; // the number of shadow rays
	int recursions; // the maximum recursion level
	int superSampling; // the super sampling level

	// Scene objects
	Camera camera; // the camera
	List<Light> lights; // the lights
	List<Shape> shapes; // the shapes
	List<Material> materials; // the materials

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
	 * The ray class. Defined by on origin and a direction.
	 */
	public static class Ray {
		Vector origin, direction;

		public Ray(Vector origin, Vector direction) {
			this.origin = origin;
			this.direction = direction;

			this.direction.normalize();
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

			String sceneFileName = args[0];
			String outputFileName = args[1];

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
	 */
	public void parseScene(String sceneFileName) throws IOException {
		FileReader fr = new FileReader(sceneFileName);

		BufferedReader r = new BufferedReader(fr);
		String line = null;
		int lineNum = 0;
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
					this.camera = new Camera(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]),
									Double.parseDouble(params[5])),
							new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]),
									Double.parseDouble(params[8])),
							Double.parseDouble(params[9]), Double.parseDouble(params[10]));
					System.out.println(String.format("Parsed camera parameters (line %d)", lineNum));
				} else if (code.equals("set")) {
					// Scene parameters
					this.backgroundColor = new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]),
							Float.parseFloat(params[2]));
					this.rootShadowRays = Integer.parseInt(params[3]);
					this.recursions = Integer.parseInt(params[4]);
					this.superSampling = Integer.parseInt(params[5]);
					System.out.println(String.format("Parsed general settings (line %d)", lineNum));
				} else if (code.equals("mtl")) {
					// Material
					this.materials.add(new Material(
							new Color(Float.parseFloat(params[0]), Float.parseFloat(params[1]),
									Float.parseFloat(params[2])),
							new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]),
									Float.parseFloat(params[5])),
							new Color(Float.parseFloat(params[6]), Float.parseFloat(params[7]),
									Float.parseFloat(params[8])),
							Double.parseDouble(params[9]), Double.parseDouble(params[10])));
					System.out.println(String.format("Parsed material (line %d)", lineNum));
				} else if (code.equals("sph")) {
					// Sphere
					this.shapes.add(new Sphere(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							Double.parseDouble(params[3]), Integer.parseInt(params[4])));
					System.out.println(String.format("Parsed sphere (line %d)", lineNum));
				} else if (code.equals("pln")) {
					// Plane
					this.shapes.add(new Plane(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							Double.parseDouble(params[3]), Integer.parseInt(params[4])));
					System.out.println(String.format("Parsed plane (line %d)", lineNum));
				} else if (code.equals("trg")) {
					// Triangle
					this.shapes.add(new Triangle(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Vector(Double.parseDouble(params[3]), Double.parseDouble(params[4]),
									Double.parseDouble(params[5])),
							new Vector(Double.parseDouble(params[6]), Double.parseDouble(params[7]),
									Double.parseDouble(params[8])),
							Integer.parseInt(params[4])));
					System.out.println(String.format("Parsed triangle (line %d)", lineNum));
				} else if (code.equals("lgt")) {
					// Light
					this.lights.add(new Light(
							new Vector(Double.parseDouble(params[0]), Double.parseDouble(params[1]),
									Double.parseDouble(params[2])),
							new Color(Float.parseFloat(params[3]), Float.parseFloat(params[4]),
									Float.parseFloat(params[5])),
							Double.parseDouble(params[6]), Double.parseDouble(params[7]),
							Double.parseDouble(params[8])));
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
		for (int x = 0; x < this.imageWidth; x++) {
			for (int y = 0; y < this.imageHeight; y++) {
				// Support for anti-aliasing
				for (int i = 0; i < this.superSampling; i++) {
					for (int j = 0; j < this.superSampling; j++) {
						// Determine the pixel-position to trace
						double trace_x = x - this.imageWidth / 2 + (j + 0.5) / this.superSampling;
						double trace_y = y - this.imageHeight / 2 + (i + 0.5) / this.superSampling;

						// The constructed ray (origin, direction)
						Ray ray = this.camera.getRay(trace_x, trace_y);

						// TODO: Put your ray tracing code here!
						//
						// Write pixel color values in RGB format to rgbData:
						// Pixel [x, y] red component is in rgbData[(y * this.imageWidth + x) * 3]
						// green component is in rgbData[(y * this.imageWidth + x) * 3 + 1]
						// blue component is in rgbData[(y * this.imageWidth + x) * 3 + 2]
						//
						// Each of the red, green and blue components should be a byte, i.e. 0-255
					}
				}
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

	//////////////////////// FUNCTIONS TO SAVE IMAGES IN PNG FORMAT
	//////////////////////// //////////////////////////////////////////

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
	 * Producing a BufferedImage that can be saved as 
	 * PNG from a byte array of RGB values.
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
	
}
