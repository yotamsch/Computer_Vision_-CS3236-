

import java.awt.Transparency;
import java.awt.color.*;
import java.awt.image.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

/**
 *  Main class for ray tracing exercise.
 */
public class RayTracer {

	// Set parameters:
	Vector backgroundColor;
	double rootShadowRays;
	int recursions;
	int superSampling;

	Camera camera;
	List<Light> lights; 
	
	public int imageWidth;
	public int imageHeight;


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

	public class Material {
		Vector diffuse;
		Vector specular;
		Vector reflection;
		double phong;
		double tranparency;

		public Material(String[] params) {
			this.diffuse = new Vector(params[0], params[1], params[2]);
			this.specular = new Vector(params[3], params[4], params[5]);
			this.reflection =  new Vector(params[6], params[7], params[8]);
			this.phong = Double.parseDouble(params[9]);
			this.tranparency = Double.parseDouble(params[10]);
		}
	}

	public class Light {
		Vector position;
		Vector color;
		double specularIntensity;
		double shadowIntensity;
		double width;

		public Light(String[] params) {
			this.position = new Vector(params[0], params[1], params[2]);
			this.color = new Vector(params[3], params[4], params[5]);
			this.specularIntensity = Double.parseDouble(params[6]);
			this.shadowIntensity = Double.parseDouble(params[7]);
			this.width = Double.parseDouble(params[8]);
		}
	}

	public class Sphere {
		Vector center;
		double radius;
		int materialIndex;

		public Sphere(String[] params) {
			this.center = new Vector(params[0], params[1], params[2]);
			this.radius = Double.parseDouble(params[3]);
			this.materialIndex = Integer.parseInt(params[4]); //TODO: might need -1
		}
		
		public Vector getNormal(Vector intersectionPoint) {
			return Vector.sub(intersectionPoint, this.center).normalize();
		}
	}

	public class Plane {
		Vector normal;
		double offset;
		int materialIndex;

		public Plane(String[] params) {
			this.normal = new Vector(params[0], params[1], params[2]);
			this.offset = Double.parseDouble(params[3]);
			this.materialIndex = Integer.parseInt(params[4]); //TODO: might need -1
		}
	}

	public class Triangle {
		Vector v1;
		Vector v2;
		Vector v3;
		int materialIndex;

		public Triangle(String[] params) {
			this.v1 = new Vector(params[0], params[1], params[2]);
			this.v2 = new Vector(params[3], params[4], params[5]);
			this.v3 = new Vector(params[6], params[7], params[8]);
			this.materialIndex = Integer.parseInt(params[9]); //TODO: might need -1
		}
	}
	
	/*
	 * params: camera, pixel coordinates
	 * return a Vector - ray from camera to pixel on screen
	 */
	public Vector constructRay(Camera camera, int x, int y) {
		
	}
	
	// gets [i][j] and calculates the color of that pixel of the image
	public byte[] rayCast(int x, int y) {
		Vector pixelRGB = new Vector (0, 0, 0);
		Vector ray = constructRay(camera, x, y);
	}

	/**
	 * Runs the ray tracer. Takes scene file, output image file and image size as input.
	 */
	public static void main(String[] args) {

		try {

			RayTracer tracer = new RayTracer();

			// Default values:
			tracer.imageWidth = 500;
			tracer.imageHeight = 500;

			if (args.length < 2)
				throw new RayTracerException("Not enough arguments provided. Please specify an input scene file and an output image file for rendering.");

			String sceneFileName = args[0];
			String outputFileName = args[1];

			if (args.length > 3)
			{
				tracer.imageWidth = Integer.parseInt(args[2]);
				tracer.imageHeight = Integer.parseInt(args[3]);
			}


			// Parse scene file:
			tracer.parseScene(sceneFileName);

			// Render scene:
			tracer.renderScene(outputFileName);

			//		} catch (IOException e) {
			//			System.out.println(e.getMessage());
		} catch (RayTracerException e) {
			System.out.println(e.getMessage());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}


	}

	/**
	 * Parses the scene file and creates the scene. Change this function so it generates the required objects.
	 */
	public void parseScene(String sceneFileName) throws IOException, RayTracerException
	{
		FileReader fr = new FileReader(sceneFileName);

		BufferedReader r = new BufferedReader(fr);
		String line = null;
		int lineNum = 0;
		System.out.println("Started parsing scene file " + sceneFileName);


		while ((line = r.readLine()) != null)
		{
			line = line.trim();
			++lineNum;

			if (line.isEmpty() || (line.charAt(0) == '#'))
			{  // This line in the scene file is a comment
				continue;
			}
			else
			{
				String code = line.substring(0, 3).toLowerCase();
				// Split according to white space characters:
				String[] params = line.substring(3).trim().toLowerCase().split("\\s+");

				if (code.equals("cam"))
				{
					// ADDED code here to parse camera parameters
					this.camera = new Camera(params);
					System.out.println(String.format("Parsed camera parameters (line %d)", lineNum));
				}
				else if (code.equals("set"))
				{
					// Add code here to parse general settings parameters
					this.backgroundColor = new Vector(params[0], params[1], params[2]);
					this.rootShadowRays = Integer.parseInt(params[3]);
					this.recursions = Integer.parseInt(params[4]);
					this.superSampling = Integer.parseInt(params[5]);
					System.out.println(String.format("Parsed general settings (line %d)", lineNum));
				}
				else if (code.equals("mtl"))
				{
					// ADDED code here to parse material parameters
					Material material = new Material(params);
					System.out.println(String.format("Parsed material (line %d)", lineNum));
				}
				else if (code.equals("sph"))
				{
					// ADDED code here to parse sphere parameters
					Sphere sphere = new Sphere(params);
					System.out.println(String.format("Parsed sphere (line %d)", lineNum));
				}
				else if (code.equals("pln"))
				{
					// ADDED code here to parse plane parameters
					Plane plane = new Plane(params);
					System.out.println(String.format("Parsed plane (line %d)", lineNum));
				}
				else if (code.equals("lgt"))
				{
					// ADDED code here to parse plane parameters
					this.lights.add(new Light(params));
					System.out.println(String.format("Parsed light (line %d)", lineNum));
				}
				else
				{
					System.out.println(String.format("ERROR: Did not recognize object: %s (line %d)", code, lineNum));
				}
			}
		}

		// It is recommended that you check here that the scene is valid,
		// for example camera settings and all necessary materials were defined.

		System.out.println("Finished parsing scene file " + sceneFileName);

	}

	/**
	 * Renders the loaded scene and saves it to the specified file location.
	 */
	public void renderScene(String outputFileName)
	{
		long startTime = System.currentTimeMillis();

		// Create a byte array to hold the pixel data:
		byte[] rgbData = new byte[this.imageWidth * this.imageHeight * 3];


		// Put your ray tracing code here!
		//
		// Write pixel color values in RGB format to rgbData:
		// Pixel [x, y] red component is in rgbData[(y * this.imageWidth + x) * 3]
		//            green component is in rgbData[(y * this.imageWidth + x) * 3 + 1]
		//             blue component is in rgbData[(y * this.imageWidth + x) * 3 + 2]
		//
		// Each of the red, green and blue components should be a byte, i.e. 0-255


		long endTime = System.currentTimeMillis();
		Long renderTime = endTime - startTime;

		// The time is measured for your own conveniece, rendering speed will not affect your score
		// unless it is exceptionally slow (more than a couple of minutes)
		System.out.println("Finished rendering scene in " + renderTime.toString() + " milliseconds.");

		// This is already implemented, and should work without adding any code.
		saveImage(this.imageWidth, rgbData, outputFileName);

		System.out.println("Saved file " + outputFileName);

	}




	//////////////////////// FUNCTIONS TO SAVE IMAGES IN PNG FORMAT //////////////////////////////////////////

	/*
	 * Saves RGB data as an image in png format to the specified location.
	 */
	public static void saveImage(int width, byte[] rgbData, String fileName)
	{
		try {

			BufferedImage image = bytes2RGB(width, rgbData);
			ImageIO.write(image, "png", new File(fileName));

		} catch (IOException e) {
			System.out.println("ERROR SAVING FILE: " + e.getMessage());
		}

	}

	/*
	 * Producing a BufferedImage that can be saved as png from a byte array of RGB values.
	 */
	public static BufferedImage bytes2RGB(int width, byte[] buffer) {
		int height = buffer.length / width / 3;
		ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		ColorModel cm = new ComponentColorModel(cs, false, false,
				Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		SampleModel sm = cm.createCompatibleSampleModel(width, height);
		DataBufferByte db = new DataBufferByte(buffer, width * height);
		WritableRaster raster = Raster.createWritableRaster(sm, db, null);
		BufferedImage result = new BufferedImage(cm, raster, false, null);

		return result;
	}

	public static class RayTracerException extends Exception {
		public RayTracerException(String msg) {  super(msg); }
	}


}
