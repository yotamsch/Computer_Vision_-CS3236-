import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry;
import com.sun.prism.paint.Gradient;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class SeamCarving {
	static double alpha = 0.5; // the weight of the energy function when combining with entropy
	static double noise = 0.000001; // TODO: maybe delete is used in (better) entropy
	static boolean isSimple = false; // is simple (direct) or complex (diagonal)

	// TODO: REMOVE
	public static void printDouble2DArrayToFile(double[][] a, String path) {
		try {
			File file = new File(path);
			FileWriter writer = new FileWriter(file);
			BufferedWriter output = new BufferedWriter(writer);
			for (double[] array : a) {
				for (double item : array) {
					output.write(String.format("%.4f", item));
					output.write(" ");
				}
				output.write("\n");
			}
			output.close();
		} catch (IOException e) {

		}
	}

	static class ImageWrapper {
		private BufferedImage image;
		int energyMethod; // 0 = normal, 1 = entropy, 2 = forward
		private Color[][] colorMap; // the Colors map of the image
		private double[][] gradientMap; // The gradient map of the image
		private double[][] pmnMap; // The f(m,n) sum map of the image
		private double[][] entropyMap; // The entropy map of the image
		private double[][] energyMap; // The energy map of the image

		public ImageWrapper(String pathToImage, int energyMethod) throws IOException {
			this.image = ImageIO.read(new File(pathToImage));
			this.energyMethod = energyMethod;

			// TODO: Remove before submission, just for debug
			{
				processImage();
				saveImageFromDoubleArray(gradientMap, "sm_grad_map.jpg", BufferedImage.TYPE_INT_RGB);
				saveImageFromDoubleArray(energyMap, "sm_energy_map.jpg", BufferedImage.TYPE_INT_RGB);
				
				if (this.energyMethod == 0) {
					System.out.println("Using gradient only.");
				}
				if (this.energyMethod == 1) {
					System.out.println("Using gradient and entropy.");
					saveImageFromDoubleArray(pmnMap, "sm_pmn_map.jpg", BufferedImage.TYPE_BYTE_GRAY);
					saveImageFromDoubleArray(entropyMap, "sm_entropy_map.jpg", BufferedImage.TYPE_BYTE_GRAY);
				}
			}
		}

		public int getWidth() {
			return this.image.getWidth();
		}

		public int getHeight() {
			return this.image.getHeight();
		}

		public BufferedImage getImage() {
			return this.image;
		}

		public void processImage() {
			// load the color map if image loaded correct
			initializeColorMap();
			// basically want to calculate the gradient/energy and entropy
			if (this.energyMethod == 1) {
				// calculatePmnMap();
				// calculateEntropyMap();
				calculatePmnMapNAIVE();
				calculateEntropyMapNAIVE();
			}
			calculateGradientMap();
			calculateEnergyMap();
		}

		/**
		 * Removes K vertical seams, based on two methods: the simple and the complex
		 * one. Where the simple looks at the naive vertical seam and the complex looks
		 * at diagonals as well.
		 * 
		 * @param k
		 *            The number of seams to remove.
		 */
		public void removeKVerticalSeams(int k) {
			for (int seam = 0; seam < k; seam++) {
				processImage();
				removeVerticalSeam();
				this.image = resizeImage(getWidth() - 1, getHeight());
			}
		}

		/**
		 * Adds K vertical seams, see removeKVerticalSeams
		 * 
		 * @param outputImg:
		 *            new image object with the required dimensions
		 * @param k:
		 *            The number of seams to remove.
		 * @param isSimple:
		 *            Represents is it a simple seam removal
		 */
		public void addKVerticalSeams(int k) {
			int[][] removedSeams = new int[k][this.image.getHeight()];

			// get a duplicate of the image with the correct dimensions
			BufferedImage outputImg = resizeImage(getWidth() + k, getHeight());

			processImage();
			// REMOVING k seams in order to ADD and duplicate them later
			for (int seam = 0; seam < k; seam++) {
				removedSeams[seam] = removeVerticalSeam();
				// since "removing" K seams needs to correct  
				// the seam position relative to the original image
				for (int i = 0; i < removedSeams[seam].length; i++) {
					removedSeams[seam][i] += seam;
				}
				resizeImage(getWidth() - 1, getHeight());
				processImage();

			}

			for (int seam = 0; seam < k; seam++) {
				addVerticalSeam(outputImg, removedSeams[seam], true);
			}
			// reset the image to be the modified one
			this.image = outputImg;
			processImage(); // just in case
		}

		/**
		 * Transposes the image, rotating it 90 degrees. A second activation rotates the
		 * image back.
		 */
		public void transposeImage() {
			int w = getWidth();
			int h = getHeight();
			BufferedImage transposedImage = new BufferedImage(h, w, this.image.getType());
			for (int y = 0; y < h; y++)
				for (int x = 0; x < w; x++)
					transposedImage.setRGB(y, x, this.image.getRGB(x, y));

			this.image = transposedImage;
		}

		private void calculateEnergyMap() {
			this.energyMap = new double[getHeight()][getWidth()];
			double southWest;
			double south;
			double southEast;
			// initialize the the energy map
			for (int j = 0; j < getHeight(); j++) {
				for (int i = 0; i < getWidth(); i++) {
					// using entropy
					if (this.energyMethod == 1)
						this.energyMap[j][i] = alpha * this.gradientMap[j][i] + (1 - alpha) * this.entropyMap[j][i];
					else
						this.energyMap[j][i] = this.gradientMap[j][i];
				}
			}
			// use dynamic programming to calculate the seam energy map - backwards energy
			if (this.energyMethod == 2) {
				// using forward energy
				double cl = 0;
				double cu = 0;
				double cr = 0;
				for (int y = 1; y < getHeight(); y++) {
					for (int x = 0; x < getWidth(); x++) {
						cl = Math.abs(this.gradientMap[y + 1][x] - this.gradientMap[y - 1][x])
								+ Math.abs(this.gradientMap[y][x - 1] - this.gradientMap[y - 1][x]);

						cu = Math.abs(this.gradientMap[y + 1][x] - this.gradientMap[y - 1][x]);

						cr = Math.abs(this.gradientMap[y + 1][x] - this.gradientMap[y - 1][x])
								+ Math.abs(this.gradientMap[y][x - 1] - this.gradientMap[y + 1][x]);

						this.energyMap[y][x] = this.gradientMap[y][x]
								+ Math.min(Math.min(this.energyMap[y - 1][x - 1] + cl, this.energyMap[y][x - 1] + cu),
										this.energyMap[y + 1][x - 1] + cr);
					}
				}
			} else {
				for (int y = 1; y < getHeight(); y++) {
					for (int x = 0; x < getWidth(); x++) {
						south = energyMap[y - 1][x];
						southWest = x > 0 && !isSimple ? energyMap[y - 1][x - 1] : Double.MAX_VALUE;
						southEast = x < getWidth() - 1 && !isSimple ? energyMap[y - 1][x + 1] : Double.MAX_VALUE;
						energyMap[y][x] = energyMap[y][x] + Math.min(Math.min(southEast, southWest), south);
					}
				}
			}
		}

		private void calculateEntropyMapNAIVE() {
			this.entropyMap = new double[getHeight()][getWidth()];
			int x_start, x_finish, y_start, y_finish;
			for (int l = 0; l < getHeight(); l++) {
				for (int k = 0; k < getWidth(); k++) {
					this.entropyMap[l][k] = 0;
					x_start = Math.max(k - 4, 0);
					x_finish = Math.min(k + 4, getWidth() - 1);
					y_start = Math.max(l - 4, 0);
					y_finish = Math.min(l + 4, getHeight() - 1);
					for (int j = y_start; j <= y_finish; j++) {
						for (int i = x_start; i <= x_finish; i++) {
							this.entropyMap[l][k] -= this.pmnMap[j][i] * Math.log(this.pmnMap[j][i]);
						}
					}
				}
			}
		}

		private void calculateEntropyMap() {
			this.entropyMap = new double[getHeight()][getWidth()];
			Queue<Double> colKeep = new ArrayDeque<Double>(5);

			int x_start, x_finish, y_start, y_finish;
			double entropy, colEntropy;

			for (int j = 1; j < getHeight(); j++) {
				// calculate for the first pixel in the row and insert into queue
				entropy = 0;

				x_start = 0;
				x_finish = 4;
				y_start = Math.max(j - 4, 0);
				y_finish = Math.min(j + 4, getHeight() - 1);
				for (int k = x_start; k <= x_finish; k++) {
					colEntropy = 0;
					for (int l = y_start; l <= y_finish; l++) {
						colEntropy -= pmnMap[k][l] * Math.log(pmnMap[k][l]);
					}

					colKeep.add(colEntropy);
					entropy += colEntropy;
				}
				this.entropyMap[j][0] = entropy;

				for (int i = 1; i < getWidth(); i++) {
					// current = last_pixel + right row - row
					colEntropy = 0;
					if (i < getWidth() - 5) {
						// right_row = ...
						x_finish = Math.min(i + 4, getWidth() - 1);
						y_start = Math.max(j - 4, 0);
						y_finish = Math.min(j + 4, getHeight() - 1);
						for (int l = y_start; l <= y_finish; l++) {
							colEntropy -= pmnMap[l][x_finish] * Math.log(pmnMap[l][x_finish]);
						}

						// queue.enqueue(right_row)
						colKeep.add(colEntropy);
					}

					// current = last_pixel + right_row - queue.dequeue()
					this.entropyMap[j][i] = this.entropyMap[j][i - 1] - colEntropy;
					if (i > 4)
						this.entropyMap[j][i] += colKeep.poll();
				}
			}
		}

		private void calculatePmnMapNAIVE() {
			this.pmnMap = new double[getHeight()][getWidth()];
			int x_start, x_finish, y_start, y_finish;
			for (int l = 0; l < getHeight(); l++) {
				for (int k = 0; k < getWidth(); k++) {
					this.pmnMap[l][k] = 0;
					x_start = Math.max(k - 4, 0);
					x_finish = Math.min(k + 4, getWidth() - 1);
					y_start = Math.max(l - 4, 0);
					y_finish = Math.min(l + 4, getHeight() - 1);
					for (int j = y_start; j <= y_finish; j++) {
						for (int i = x_start; i <= x_finish; i++) {
							this.pmnMap[l][k] += getPixelGrayscale(j, i);
						}
					}
					this.pmnMap[l][k] = getPixelGrayscale(l, k) / this.pmnMap[l][k] + noise;
				}
			}
		}

		private void calculatePmnMap() {
			this.pmnMap = new double[getHeight()][getWidth()];
			Queue<Double> colKeep = new ArrayDeque<Double>(10);

			int x_start, x_finish, y_start, y_finish;
			double colDenominator;
			int i, j, k, l;
			for (j = 0; j < getHeight(); j++) {
				// calculate for the first pixel in the row and insert into queue
				this.pmnMap[j][0] = 0;
				x_start = 0;
				x_finish = 4;
				y_start = Math.max(j - 4, 0);
				y_finish = Math.min(j + 4, getHeight() - 1);
				for (k = x_start; k <= x_finish; k++) {
					colDenominator = 0;
					for (l = y_start; l <= y_finish; l++) {
						colDenominator += getPixelGrayscale(l, k);
					}
					colKeep.add(colDenominator);
					this.pmnMap[j][0] += colDenominator;
				}

				for (i = 1; i < getWidth(); i++) {
					colDenominator = 0;
					if (i < getWidth() - 5) {
						// right_row = ...
						x_finish = i + 4;
						y_start = Math.max(j - 4, 0);
						y_finish = Math.min(j + 4, getHeight() - 1);
						for (l = y_start; l <= y_finish; l++) {
							colDenominator += getPixelGrayscale(l, x_finish);
							// System.out.printf("(%d, %d) - %d: %.5f",j,i,l, getPixelGrayscale(l,
							// x_finish));
							// System.out.println();
						}
						// queue.enqueue(right_row)
						colKeep.add(colDenominator);
					}
					// System.out.printf("(%d, %d): %.5f",j,i, colDenominator);
					// System.out.println();

					// current = last_pixel + right_row - queue.dequeue()
					this.pmnMap[j][i] = this.pmnMap[j][i - 1] + colDenominator;
					if (i > 4) {
						this.pmnMap[j][i] = this.pmnMap[j][i] - colKeep.poll();
					}
				}
			}
			for (j = 0; j < getHeight(); j++) {
				for (i = 0; i < getWidth(); i++) {
					this.pmnMap[j][i] = getPixelGrayscale(j, i) / this.pmnMap[j][i] + noise;
				}
			}
		}

		private double getPixelGrayscale(int l, int k) {
			double res = (colorMap[l][k].getRed() + colorMap[l][k].getGreen() + colorMap[l][k].getBlue()) / 3.0;
			return res;
		}

		private void calculateGradientMap() {
			this.gradientMap = new double[getHeight()][getWidth()];
			for (int i = 0; i < getWidth(); i++) {
				for (int j = 0; j < getHeight(); j++) {
					this.gradientMap[j][i] = calculateGradientForPixel(i, j);
				}
			}
		}

		private double calculateGradientForPixel(int i, int j) {
			double result = 0;
			int numOfNeighbours = -1; // start from -1 so it wouldn't count the pixel itself
			int x_start = Math.max(i - 1, 0);
			int x_finish = Math.min(i + 1, getWidth() - 1);
			int y_start = Math.max(j - 1, 0);
			int y_finish = Math.min(j + 1, getHeight() - 1);

			for (int k = x_start; k <= x_finish; k++) {
				for (int l = y_start; l <= y_finish; l++) {
					numOfNeighbours += 1;
					result += Math.abs(colorMap[j][i].getRed() - colorMap[l][k].getRed());
					result += Math.abs(colorMap[j][i].getGreen() - colorMap[l][k].getGreen());
					result += Math.abs(colorMap[j][i].getBlue() - colorMap[l][k].getBlue());
				}
			}
			return result / numOfNeighbours;
		}

		private void initializeColorMap() {
			this.colorMap = new Color[getHeight()][getWidth()];
			for (int i = 0; i < getWidth(); i++) {
				for (int j = 0; j < getHeight(); j++) {
					this.colorMap[j][i] = new Color(this.image.getRGB(i, j));
				}
			}
		}

		/**
		 * Finds the row positions indices of the seam with the lowest energy.
		 * 
		 * @return An array of integers representing the position of the seam along the
		 *         height.
		 */
		private int[] findVerticalSeam() {
			int col = 0;
			int[] seam;
			double minValue = Double.MAX_VALUE;
			// Go over the bottom row to check for the minimum
			for (int x = 0; x < getWidth(); x++) {
				if (this.energyMap[getHeight() - 1][x] < minValue) {
					minValue = this.energyMap[getHeight() - 1][x];
					col = x;
				}
			}
			seam = backtrackVerticalSeam(col);
			return seam;
		}

		/**
		 * Backtracks the vertical seam and finds the correct path of the seam.
		 * 
		 * @param col
		 *            The end column of the seam.
		 * @return An array of integers representing the seam path.
		 */
		private int[] backtrackVerticalSeam(int col) {
			int[] seam;
			double minValue;
			// Get the entire row indices of the seam (Backtracking)
			// It prefers the direct row
			seam = new int[getHeight()];
			seam[getHeight() - 1] = col;
			for (int y = getHeight() - 1; y > 0; y--) {
				minValue = this.energyMap[y - 1][seam[y]];
				seam[y - 1] = seam[y];
				if (!isSimple) {
					if (seam[y] > 0 && minValue > this.energyMap[y - 1][seam[y] - 1]) {
						seam[y - 1] = seam[y] - 1;
					}
					if (seam[y] < getWidth() - 1 && minValue > this.energyMap[y - 1][seam[y] + 1]) {
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
		private int[] removeVerticalSeam() {
			int[] seamToRemove = findVerticalSeam();
			for (int y = 0; y < getHeight(); y++) {
				for (int x = seamToRemove[y]; x < getWidth() - 1; x++) {
					int rgb = this.image.getRGB(x + 1, y);
					this.image.setRGB(x, y, rgb);
				}
			}
			return seamToRemove;
		}

		/**
		 * Adds a vertical seam, same logic as removeVerticalSeam BUT needs to receive
		 * the seam
		 * 
		 * @param outputImg:
		 *            new image object with the required dimensions
		 * @param seamToAdd
		 * @param shift
		 *            The shift to the original index (depends on how many added)
		 * @param isInterp:
		 *            should the added seams' colour be interpolated. If not - double
		 *            the seams
		 */
		private void addVerticalSeam(BufferedImage outputImg, int[] seamToAdd, boolean isInterp) {
			// shifts all pixels before seam to the right (thus duplicating the seam
			for (int y = 0; y < outputImg.getHeight(); y++) {
				for (int x = outputImg.getWidth() - 1; x > seamToAdd[y]; x--) {
					outputImg.setRGB(x, y, outputImg.getRGB(x - 1, y));
				}
				// adding the seam, averaging its colour with its neighbors 8
				if (isInterp) {
					Color interC = interpolateSeamColorsAtIndex(outputImg, seamToAdd, y);
					outputImg.setRGB(seamToAdd[y], y, interC.getRGB());
				}
			}
		}

		private Color interpolateSeamColorsAtIndex(BufferedImage outputImg, int[] seamToAdd, int y) {
			Color interC;
			// color interpolation
			{
				int r = 0, g = 0, b = 0;
				int delim = 0;
				int x_start = Math.max(seamToAdd[y] - 1, 0);
				int x_finish = Math.min(seamToAdd[y] + 1, outputImg.getWidth() - 1);
				int y_start = Math.max(y - 1, 0);
				int y_finish = Math.min(y + 1, outputImg.getHeight() - 1);
				Color c;
				for (int k = x_start; k <= x_finish; k++) {
					for (int l = y_start; l <= y_finish; l++) {
						delim++;
						c = new Color (outputImg.getRGB(k,l));
						r += c.getRed();
						g += c.getGreen();
						b += c.getBlue();
					}
				}
				r /= delim;
				g /= delim;
				b /= delim;
				
				interC = new Color(r, g, b);
			}
			return interC;
		}

		/**
		 * Resizing a copy of the image, anchored in the (0,0) pixel, meaning top left
		 * corner
		 * 
		 * @param width
		 * @param height
		 */
		private BufferedImage resizeImage(int width, int height) {
			// TODO Auto-generated method stub
			BufferedImage resized = new BufferedImage(width, height, image.getType());
			for (int j = 0; j < Math.min(height, getHeight()); j++) {
				for (int i = 0; i < Math.min(width, getWidth()); i++) {
					resized.setRGB(i, j, image.getRGB(i, j));
				}
			}
			return resized;
		}

	} // ! ImageWrapper

	// TODO: remove before submitting
	/**
	 * Saves a double array as gray scale image.
	 * 
	 * @param array
	 *            The double 2D-array.
	 * @param outPath
	 *            The output path to save to.
	 */
	private static void saveImageFromDoubleArray(double[][] array, String outPath, int colorType) {
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
					// gets the hue value between blue and red
					float hue = (float) (Color.RGBtoHSB(0, 0, 255, null)[0]
							+ (Color.RGBtoHSB(255, 0, 0, null)[0] - Color.RGBtoHSB(0, 0, 255, null)[0])
									* (array[i][j] - min) / (max - min));
					val = Color.getHSBColor(hue, 1, 1);
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

		try {
			ImageWrapper imgWrap = new ImageWrapper(inputFileName, 0);

			// TODO: Remove. To set the cropping dimensions.
			outputWidth = imgWrap.getWidth() - 150;
			outputHeight = imgWrap.getHeight() - 0;

			System.out.printf("Old dimensions: (%d, %d)\nNew dimensions: (%d, %d)\n", imgWrap.getWidth(),
					imgWrap.getHeight(), outputWidth, outputHeight);

			alterCols = imgWrap.getWidth() - outputWidth;
			alterRows = imgWrap.getHeight() - outputHeight;

			if (alterCols != 0) {
				if (alterCols > 0) {
					System.out.println("Removing vertical seams");
					// Remove K vertical seams
					imgWrap.removeKVerticalSeams(Math.abs(alterCols));
				} else {
					System.out.println("Adding vertical seams");
					// Add K vertical seams
					imgWrap.addKVerticalSeams(Math.abs(alterCols));
				}
			}
			if (alterRows != 0) {
				// transpose for horizontal
				imgWrap.transposeImage();
				if (alterRows > 0) {
					System.out.println("Removing horizontal seams");
					// Remove K horizontal seams
					imgWrap.removeKVerticalSeams(Math.abs(alterRows));
				} else {
					System.out.println("Adding horizontal seams");
					// Add K horizontal seams
					imgWrap.addKVerticalSeams(Math.abs(alterRows));
				}
				// Revert back to normal rotation
				imgWrap.transposeImage();
			}

			// Save the result
			ImageIO.write(imgWrap.getImage(), "jpg", new File(outputFileName));
			// ImageIO.write(imgOriginal, "jpg", new File(outputSeamsFileName));
			System.out.println("DONE\n");
		} catch (IOException e) {
			System.out.println("Could not open image file.\n");
		}
	}

}
