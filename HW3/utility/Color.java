package utility;

public class Color {
	float r, g, b;

	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color(Color other) {
		this(other.r, other.g, other.b);
	}
	
	public void add(Color other) {
		this.r += other.r;
		this.g += other.g;
		this.b += other.b;
	}
	
	public void mul(Color other) {
		this.r *= other.r;
		this.g *= other.g;
		this.b *= other.b;
	}
	
	public void mul(float scalar) {
		this.r *= scalar;
		this.g *= scalar;
		this.b *= scalar;
	}
	
	public void div(float scalar) {
		mul(1.0F / scalar);
	}
	
	public void clamp() {
		float min = 0.0F;
		float max = 1.0F;
		
		this.r = this.r < min ? min : this.r;
		this.r = this.r > max ? max : this.r;
		this.g = this.g < min ? min : this.g;
		this.g = this.g > max ? max : this.g;
		this.b = this.b < min ? min : this.b;
		this.b = this.b > max ? max : this.b;
	}

	public byte[] getRGB() {
		byte[] result = new byte[3];
		result[0] = (byte) (Math.round(this.r * 255));
		result[1] = (byte) (Math.round(this.g * 255));
		result[2] = (byte) (Math.round(this.b * 255));
		return result;
	}

	public String toString() {
		byte[] RGB = getRGB();
		return String.format("<Color: [r-%d,g-%d,b-%d]>", RGB[0], RGB[1], RGB[2]);
	}
}
