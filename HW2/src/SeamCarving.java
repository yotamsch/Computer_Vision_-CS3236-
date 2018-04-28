import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;

public class SeamCarving {
	static BufferedImage imgOriginal;
	static BufferedImage img;
	static double alpha = 0.5; // the weight of the energy function when combining with entropy
	static int height;
	static int width;
	static double[][] gradientMap; // The gradient map of the image
	static double[][] energyMap; // The energy map of the image

	// TODO: Remove before submission, only for debug
	static String outputGradientFileName = "sm_gradient.jpg";
	static String outputEnergyFileName = "sm_energy.jpg";
	static String outputSeamsFileName = "sm_seams.jpg";

	static class SeamSum {
		public int idx;
		public double sum;

		public SeamSum(int idx, double sum) {
			this.idx = idx;
			this.sum = sum;
		}

		public double compareTo(SeamSum o) {
			return (sum - o.sum);
		}
	} // ! SeamSum

	static class Pixel {
		public int red;
		public int green;
		public int blue;
		public int greyscale;
		public int gradient;

		// Constructor
		public Pixel(int x, int y) {
			try {
				Color color = new Color(img.getRGB(x, y));
				red = color.getRed();
				green = color.getGreen();
				blue = color.getBlue();
				greyscale = (red + blue + green) / 3;
			} catch (Exception e) {
				System.out.printf("Index: %d %d\n", x, y);
				e.printStackTrace();
			}
		}

		// Calculates the pixel (x,y)'s gradient:
		// red, green and blue differences from its 8 neighbors
		// returns their sum
		public int calcGradient(int x, int y) {
			int red_dif = 0;
			int green_dif = 0;
			int blue_dif = 0;
			int numOfNeighbours = -1; // start from -1 so it wouldn't count the pixel itself
			int x_start = Math.max(x - 1, 0);
			int x_finish = Math.min(x + 1, width - 1);
			int y_start = Math.max(y - 1, 0);
			int y_finish = Math.min(y + 1, height - 1);

			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					Pixel neighbour = new Pixel(i, j);
					numOfNeighbours += 1;
					// FIXME: might actually be without Math.abs, should verify
					red_dif += Math.abs(this.red - neighbour.red);
					green_dif += Math.abs(this.green - neighbour.green);
					blue_dif += Math.abs(this.blue - neighbour.blue);
				}
			}
			return (red_dif + green_dif + blue_dif) / numOfNeighbours;
		}

		/*
		 * Returns Pm,n - the probability of the pixel's colours
		 */
		public float prob(int x, int y, int x_start, int x_finish, int y_start, int y_finish) {
			int fmn = new Pixel(x, y).greyscale;
			float denominator = 0;
			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					Pixel neighbour = new Pixel(i, j);
					denominator += neighbour.greyscale;
				}
			}
			// System.out.println(denominator);
			return fmn / denominator;
		}

		/**
		 * Returns the entropy of pixel (x, y) calls the method prob(...)
		 */
		public double entropy(int x, int y) {
			double res = 0;
			int x_start = x - 4;
			int x_finish = x + 4;
			int y_start = y - 4;
			int y_finish = y + 4;
			if (x <= 4) {
				x_start = 0;
			}
			if (y <= 4) {
				y_start = 0;
			}
			if (x >= width - 5) {
				x_finish = width - 1;
			}
			if (y >= height - 5) {
				y_finish = height - 1;
			}

			float prob = 0;
			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					prob = prob(i, j, x_start, x_finish, y_start, y_finish);
					if (prob == 0) {
						// System.out.println("Prob is 0: " + i + " " + j + "\n");
					}
					res -= prob * Math.log(prob);
				}
			}
			return res;
		}
	} // ! Pixel

	public static void calcGradientMap(boolean isEntropyActive) {
		gradientMap = new double[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				Pixel curPixel = new Pixel(x, y);
				gradientMap[y][x] = isEntropyActive
						? alpha * curPixel.calcGradient(x, y) + (1 - alpha) * curPixel.entropy(x, y)
						: curPixel.calcGradient(x, y);
			}
		}
	}

	public static void calcEnergyMap(boolean isSimple) {
		double southWest;
		double south;
		double southEast;
		energyMap = new double[height][width];
		// initialize the first row of the energy map
		for (int i = 0; i < width; i++)
			energyMap[0][i] = gradientMap[0][i];
		// use dynamic programming to calculate the energy map
		for (int y = 1; y < height; y++) {
			for (int x = 0; x < width; x++) {
				south = energyMap[y - 1][x];
				southWest = x > 0 && !isSimple ? energyMap[y - 1][x - 1] : Double.MAX_VALUE;
				southEast = x < width - 1 && !isSimple ? energyMap[y - 1][x + 1] : Double.MAX_VALUE;
				energyMap[y][x] = gradientMap[y][x] + Math.min(Math.min(southEast, southWest), south);
			}
		}
	}

	/**
	 * Finds the row positions indices of the seam with the lowest energy.
	 * 
	 * @param isSimple
	 *            Is a simple vertical seam needs to be detected
	 * @return An array of integers representing the position of the seam along the
	 *         height.
	 */
	static int[] findVerticalSeam(boolean isSimple) {
		int col = 0;
		int[] seam;
		double minValue = Double.MAX_VALUE;
		// Go over the bottom row to check for the minimum
		for (int x = 0; x < width; x++) {
			if (energyMap[height - 1][x] < minValue) {
				minValue = energyMap[height - 1][x];
				col = x;
			}
		}
		seam = backtrackVerticalSeam(isSimple, col);
		return seam;
	}

	/**
	 * Backtracks the vertical seam and finds the correct path of the seam.
	 * 
	 * @param isSimple
	 *            Is the seam a simple or complex.
	 * @param col
	 *            The end column of the seam.
	 * @return An array of integers representing the seam path.
	 */
	private static int[] backtrackVerticalSeam(boolean isSimple, int col) {
		int[] seam;
		double minValue;
		// Get the entire row indices of the seam (Backtracking)
		// It prefers the direct row
		seam = new int[height];
		seam[height - 1] = col;
		for (int y = height - 1; y > 0; y--) {
			minValue = energyMap[y - 1][seam[y]];
			seam[y - 1] = seam[y];
			if (!isSimple) {
				if (seam[y] > 0 && minValue > energyMap[y - 1][seam[y] - 1]) {
					seam[y - 1] = seam[y] - 1;
				}
				if (seam[y] < width - 1 && minValue > energyMap[y - 1][seam[y] + 1]) {
					seam[y - 1] = seam[y] + 1;
				}
			}
		}
		return seam;
	}

	/**
	 * Removes a vertical seam, based on two methods: the simple and the complex
	 * one. Where the simple looks at the naive vertical seam and the complex looks
	 * at diagonals as well.
	 * 
	 * @param isSimple
	 *            Represents is it a simple seam removal
	 */
	private static int[] removeVerticalSeam(int widthDiff, boolean isSimple) {
		int[] seamToRemove = findVerticalSeam(isSimple);
		for (int y = 0; y < height; y++) {
			// Mark a red line on the seam in the original image
			if (imgOriginal != null)
				imgOriginal.setRGB(seamToRemove[y] + widthDiff, y, Color.RED.getRGB());

			for (int x = seamToRemove[y]; x < width - 1; x++) {
				int rgb = img.getRGB(x + 1, y);
				img.setRGB(x, y, rgb);
			}
		}
		img = img.getSubimage(0, 0, width - 1, height);
		width = width - 1;
		return seamToRemove;
	}

	/**
	 * Removes K vertical seams, based on two methods: the simple and the complex
	 * one. Where the simple looks at the naive vertical seam and the complex looks
	 * at diagonals as well.
	 * 
	 * @param k
	 *            The number of seams to remove.
	 * @param isSimple
	 *            Represents is it a simple seam removal
	 */
	public static void removeKVerticalSeams(int k, boolean isSimple, boolean isEntropyActive) {
		int[] seamIndex;
		// Energy map initialization
		calcGradientMap(isEntropyActive);
		// Calculate the energy of the Image
		calcEnergyMap(isSimple);

		// TODO: Remove. Print the gradient as image
//		saveImageFromDoubleArray(gradientMap, outputGradientFileName, BufferedImage.TYPE_INT_RGB, false);
		// TODO: Remove. Print the energy as image
//		saveImageFromDoubleArray(energyMap, outputEnergyFileName, BufferedImage.TYPE_INT_RGB, false);
		
		for (int seam = 0; seam < k; seam++) {
			seamIndex = removeVerticalSeam(seam, isSimple);
			reAdjustEnergyMap(seamIndex, isSimple, isEntropyActive);
		}
	}

	/**
	 * Readjusts the energy map after a seam has been removed.
	 * 
	 * @param isSimple
	 *            Is the removed seam of simple or complex type
	 * @param isEntropyActive
	 *            Is using the normal gradient or the entropy version
	 */
	private static void reAdjustEnergyMap(int[] seam, boolean isSimple, boolean isEntropyActive) {
		// TODO: Maybe have a better more efficient implementation
		calcGradientMap(isEntropyActive);
		calcEnergyMap(isSimple);
	}

	/**
	 * Transposes the image, rotating it 90 degrees. A second activation rotates the
	 * image back.
	 * 
	 * @param src
	 *            The source image to transpose.
	 * @return A new transposed image.
	 */
	static BufferedImage transposeImage(BufferedImage src) {
		int w = src.getWidth();
		int h = src.getHeight();
		BufferedImage dest = new BufferedImage(h, w, src.getType());
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				dest.setRGB(y, x, src.getRGB(x, y));
		return dest;
	}

	// TODO: remove before submitting
	/**
	 * Thresholds a value, returns 0 or 255 based on threshold.
	 * 
	 * @param value
	 *            The value.
	 * @param threshhold
	 *            The threshold.
	 */
	private static int thresholdValue(double value, double threshold) {
		return value >= threshold ? 255 : 0;
	}

	// TODO: remove before submitting
	/**
	 * Saves a double array as gray scale image.
	 * 
	 * @param array
	 *            The double 2D-array.
	 * @param outPath
	 *            The output path to save to.
	 */
	private static void saveImageFromDoubleArray(double[][] array, String outPath, int colorType, boolean threshold) {
		try {
			int width = array[0].length;
			int height = array.length;
			Color val;
			double min = Double.MAX_VALUE;
			double max = 0;
			// get the maximum and minimum values
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					max = Math.max(max, array[i][j]);
					min = Math.min(min, array[i][j]);
				}
			}
			BufferedImage image = new BufferedImage(width, height, colorType);
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					int th = thresholdValue(array[i][j] / max, 0.5);
					// gets the hue value between blue and red
					float hue = (float) (Color.RGBtoHSB(0, 0, 255, null)[0]
							+ (Color.RGBtoHSB(255, 0, 0, null)[0] - Color.RGBtoHSB(0, 0, 255, null)[0])
									* (array[i][j] - min) / (max - min));
					val = threshold ? new Color(th, th, th) : Color.getHSBColor(hue, 1, 1);
					image.setRGB(j, i, val.getRGB());
				}
			}

			ImageIO.write(image, "jpg", new File(outPath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String inputFileName;
		String outputFileName;
		int outputWidth;
		int outputHeight;
		int energyType;
		int alterCols;
		int alterRows;

		// TODO: make accessible when submitting
		if (false) {
			outputWidth = Integer.valueOf(args[2]);
			outputHeight = Integer.valueOf(args[3]);
			energyType = Integer.valueOf(args[4]);
		}

		inputFileName = "sm.jpg";
		outputFileName = "sm_output.jpg";

		boolean isSimple = false; // is simple (direct) or complex (diagonal)
		boolean isEntropyActive = false; // is using the entropy gradient version

		try {
			// Load the image
			img = ImageIO.read(new File(inputFileName));
			imgOriginal = ImageIO.read(new File(inputFileName));

			// NOTE:
			// Image rotation, if needs to do horizontal,
			// is possible with the transposeImage(source) function

			// Get the final width and height
			height = img.getHeight();
			width = img.getWidth();

			// TODO: Remove. To set the cropping dimensions.
			outputWidth = height - 10;
			outputHeight = width - 10;
			
			System.out.printf("Old dimensions: (%d, %d)\nNewdimensions: (%d, %d)\n", width, height, outputWidth, outputHeight);

			alterCols = width - outputWidth;
			alterRows = height - outputHeight;

			if (alterCols != 0) {
				if (alterCols > 0) {
					System.out.println("Removing vertical seams");
					// Remove K vertical seams
					removeKVerticalSeams(Math.abs(alterCols), isSimple, isEntropyActive);
				}
			}
			if (alterRows != 0) {
				if (alterRows > 0) {
					System.out.println("Removing horizontal seams");
					// Remove K horizontal seams
					img = transposeImage(img);
					imgOriginal = transposeImage(imgOriginal);
					height = img.getHeight();
					width = img.getWidth();
					removeKVerticalSeams(Math.abs(alterRows), isSimple, isEntropyActive);
					// Revert back to normal rotation
					img = transposeImage(img);
					imgOriginal = transposeImage(imgOriginal);
				}
			}

			// Save the result
			ImageIO.write(img, "jpg", new File(outputFileName));
			ImageIO.write(imgOriginal, "jpg", new File(outputSeamsFileName));
			System.out.println("DONE\n");
		} catch (IOException e) {
			System.out.println("Could not open image file.\n");
		}
	}

}
