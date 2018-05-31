
public class Color extends Vector {

	public Color(double r, double g, double b) {
		super(r, g, b);
	}

	public Color(String r, String g, String b) {
		super(r, g, b);
	}

	public Color(Color other) {
		super(other);
	}

	public byte[] getRGB() {
		byte[] result = new byte[3];
		result[0] = (byte) (Math.round(this.x * 255));
		result[1] = (byte) (Math.round(this.y * 255));
		result[2] = (byte) (Math.round(this.z * 255));
		return result;
	}
	
	public String toString() {
		byte[] RGB = getRGB();
		return String.format("<Color: [r-%.4f,g-%.4f,b-%.4f]>", RGB[0], RGB[1], RGB[2]);
	}
}
