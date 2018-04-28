import java.awt.Color;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.Math;
//import java.util.Arrays;
//import java.util.Collections;

public class SeamCarving {
	static BufferedImage img;
	static double alpha = 0.5; // the weight of the energy function when combining with entropy
	static int height;
	static int width;
	static double[][] energyMap;

	static class SeamSum { 
		public int idx; 
		public double sum; 
		public SeamSum(int idx, double sum) { 
			this.idx = idx; 
			this.sum = sum; 
		}

		public double compareTo(SeamSum o) {
			return(sum - o.sum);
		}
	}

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
				System.out.format("Index: %d %d\n", x, y);
				e.printStackTrace();
			}
		}

		// Calculates the pixel (x,y)'s gradient:
		// red, green and blue differences from its 8 neighbours
		// returns their sum
		public int calcGradient(int x, int y) {
			int red_dif = 0;
			int green_dif = 0;
			int blue_dif = 0;
			int numOfNeighbours = 0;
			int x_start = x-1;
			int x_finish = x+1;
			int y_start = y-1;
			int y_finish = y+1;

			if (x == 0) {
				x_start = x;
			} else if (x == width-1) {
				x_finish = x;
			}

			if (y == 0) {
				y_start = y;
			} else if (y == height-1) {
				y_finish = y;
			}

			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					Pixel neighbour = new Pixel(i, j);
					numOfNeighbours += 1;
					red_dif += Math.abs(red - neighbour.red);
					green_dif += Math.abs(green - neighbour.green);
					blue_dif += Math.abs(blue - neighbour.blue);
				}
			}
			return (red_dif + green_dif + blue_dif) / numOfNeighbours;
		}

		/*
		 * Returns Pm,n - the probability of the pixel's colours
		 */
		public float prob(int x, int y, int x_start, int x_finish, int y_start, int y_finish) {
			int fmn = new Pixel(x,y).greyscale;
			float denominator = 0;
			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					Pixel neighbour = new Pixel(i, j);
					denominator += neighbour.greyscale;
				}
			}
			//System.out.println(denominator);
			return fmn / denominator;
		}

		/**
		 * Returns the entropy of pixel (x, y)
		 * calls the method prob(...)
		 */
		public double entropy(int x, int y) {
			double res = 0;
			int x_start = x-4;
			int x_finish = x+4;
			int y_start = y-4;
			int y_finish = y+4;
			if (x <= 4) { x_start = 0;}
			if (y <= 4) {y_start = 0;}
			if (x >= width - 5) {x_finish = width-1;}
			if (y >= height - 5) {y_finish = height-1;}

			float prob = 0;
			for (int i = x_start; i <= x_finish; i++) {
				for (int j = y_start; j <= y_finish; j++) {
					prob = prob(i, j, x_start, x_finish, y_start, y_finish);
					if (prob == 0) {
						System.out.println("Prob is 0: " + i + " " + j + "\n");
					}
					res -= prob * Math.log(prob);
				}
			}
			return res;
		}
	}

	public static void initializeMap() {
		energyMap = new double[height][width];
		for (int y=0; y<height ;y++) {
			for (int x=0; x<width; x++) {
				Pixel curPixel = new Pixel(x, y);
				//energyMap[y][x] = alpha * curPixel.calcGradient(x, y) + (1-alpha)*curPixel.entropy(x, y);
				energyMap[y][x] = curPixel.calcGradient(x, y);
			}
		}
//		try {
//			writeMap();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public static void calcEnergyMap() {
		for (int y=0; y<height ;y++) {
			for (int x=0; x<width; x++) {
				//Pixel curPixel = new Pixel(x, y);
				if (y != 0) {
					try {
						energyMap[y][x] += Math.min(Math.min(energyMap[y-1][x-1], energyMap[y-1][x]), energyMap[y-1][x+1]);
					} catch (ArrayIndexOutOfBoundsException e) {
						//System.out.println("In the energyMap catch\n");
						if (x==0) energyMap[y][x] += Math.min(energyMap[y-1][x],energyMap[y-1][x+1]);
						else energyMap[y][x] += Math.min(energyMap[y-1][x],energyMap[y-1][x-1]);
					}
				}
				//System.out.println("Energy map val: " + energyMap[y][x]);
			}
		}
		try {
			writeMap();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * seam[] holds the column index of the lowest energy pixel from top to bottom
	 */
	//	public static SeamSum[] sortVerticalSeams(int k) {
	//		double curColSum = 0;
	//		SeamSum[] seamSums = new SeamSum[width];
	//		for (int y = 0; y < height; y++) {
	//			for (int x = 0; x < width; x++) {
	//				curColSum += energyMap[y][x];
	//			}
	//			seamSums[y] = new SeamSum(y, curColSum);
	//		}
	//		Arrays.sort(seamSums);
	//		
	//		return Arrays.copyOfRange(seamSums, 0, k);
	//	}
	
	public static void writeMap () throws IOException {
		  BufferedWriter outputWriter = null;
		  outputWriter = new BufferedWriter(new FileWriter("energyMap.txt"));
		  for (int i = 0; i < width; i++) {
		    for(int j = 0; j < height; j++) {
		    	outputWriter.write(Double.toString(energyMap[j][i]) + " ");
		    	outputWriter.newLine();
		    }
		  }
		  outputWriter.flush();  
		  outputWriter.close();  
		}

	static int[] findVerticalSeam() {
		double curColSum = 0;
		double minValue = Double.MAX_VALUE;
		int col = 0;
		for (int x = 0; x < width; x++) {
			System.out.println("x1: " + x);
			for (int y = 0; y < height; y++) {
				curColSum += energyMap[y][x];
			}
			if (curColSum < minValue) {
				minValue = curColSum;
				System.out.println("x2: " + x);
				col += x;
			}
		}
		int[] seam = new int[height];
		for (int i = 0; i < height; i++) {
			seam[i] = col;
		}
		//System.out.println("Seam idx: " + col);
		return seam;
	}



	public static int[] findGeneralSeam() {
		int[] seam = new int[height];
		double minEnergy = energyMap[height-1][0];
		//find the lowest energy in the last line of energyMap
		for (int i = 0; i < width; i++) {
			if (energyMap[height-1][i] < minEnergy) {
				minEnergy = energyMap[height-1][i]; 
				seam[height-1]=i;
			}
		}

		//TODO: find min parent of each pixel

		return seam;
	}


	// removes k seams
	public static BufferedImage removeVerticalSeams (int k) {
		//SeamSum[] sortedSeams = sortVerticalSeams(k);
		//TODO: sort srtedSeams by col index
		for (int seam = 0; seam < k; seam++) {
			int[] seamToRemove = findVerticalSeam();
			for (int y=0; y < height; y++) {
				for (int x = seamToRemove[y]; x < width-1 ; x++) {
					int rgb = img.getRGB(x+1, y);
					img.setRGB(x, y, rgb);
				}
			}
			calcEnergyMap();
		}
		return img.getSubimage(0,  0, width-k, height);
	}


	// removes the seam
	public static void removeSeam(BufferedImage outImg, int[] seam) {

		for (int i=0; i < height; i++) {
			for (int j=seam[i]; j < width-1; j++) {
				int rgb = outImg.getRGB(i, j+1);
				outImg.setRGB(i, j, rgb);
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//		String inputFileName = args[1];
		//		int outputColumns = Integer.valueOf(args[2]);
		//		int outputRows = Integer.valueOf(args[3]);
		//		int energyType = Integer.valueOf(args[4]);
		//		String outputFileName = args[5];

		String inputFileName = "sm.png";
		String outputFileName = "sm_output.png";
		int removeColsNum = 5;

		try {			
			img = ImageIO.read(new File(inputFileName));
			height = img.getHeight();
			width = img.getWidth();
			//BufferedImage outputImg = new BufferedImage(width-removeColsNum, height, BufferedImage.TYPE_INT_ARGB);
			initializeMap();
//			for (int i = 0; i < height; i++) {
//				for (int j = 0; j < width; j++) {
//					System.out.println(energyMap[i][j]);					
//				}
//				System.out.println("\n");
//			}
			calcEnergyMap();
			BufferedImage outputImg = removeVerticalSeams(removeColsNum);
			ImageIO.write(outputImg, "jpg", new File (outputFileName));
			System.out.println("DONE\n");
		} catch (IOException e) {
			System.out.println("Could not open image file.\n");
		}
		//double[][] findSeam = optimalSeam();
	}

}
