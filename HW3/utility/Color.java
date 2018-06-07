package utility;

public class Color {
	private float r, g, b;

	public Color(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public Color(Color other) {
		this(other.r, other.g, other.b);
	}

	public Color add(Color other) {
		this.r += other.r;
		this.g += other.g;
		this.b += other.b;
		return this;
	}

	public Color mul(Color other) {
		this.r *= other.r;
		this.g *= other.g;
		this.b *= other.b;
		return this;
	}

	public Color mul(float scalar) {
		this.r *= scalar;
		this.g *= scalar;
		this.b *= scalar;
		return this;
	}

	public Color div(float scalar) {
		return mul(1.0F / scalar);
	}

	public Color clamp() {
		float min = 0.0F;
		float max = 1.0F;

		this.r = this.r < min ? min : this.r;
		this.r = this.r > max ? max : this.r;
		this.g = this.g < min ? min : this.g;
		this.g = this.g > max ? max : this.g;
		this.b = this.b < min ? min : this.b;
		this.b = this.b > max ? max : this.b;

		return this;
	}

	public byte[] getRGB() {
		byte[] result = new byte[3];
		result[0] = (byte) (Math.round(this.r * 255));
		result[1] = (byte) (Math.round(this.g * 255));
		result[2] = (byte) (Math.round(this.b * 255));
		return result;
	}

	public String toString() {
		char[] RGB = new char[3];
		RGB[0] = (char) (Math.round(this.r * 255));
		RGB[1] = (char) (Math.round(this.g * 255));
		RGB[2] = (char) (Math.round(this.b * 255));
		return String.format("<Color: [%d,%d,%d]>", (int) RGB[0], (int) RGB[1], (int) RGB[2]);
	}

	public boolean equals(Color obj) {
		if (this.r == obj.r)
			if (this.g == obj.g)
				if (this.b == obj.b)
					return true;
		return false;
	}
}
