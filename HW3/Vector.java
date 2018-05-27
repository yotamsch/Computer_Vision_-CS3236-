

import java.util.Arrays;


public class Vector {
	double x;
	double y;
	double z;


	//Constructors
	public Vector(double x, double y, double z){
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector(String x, String y, String z){
		this(Double.parseDouble(x), Double.parseDouble(y), Double.parseDouble(z));
	}	

	// Addition
	public static Vector sum(Vector v, Vector u){
		return new Vector(v.x + u.x, v.y + u.y, v.z + u.z);
	}
	public Vector add(Vector v){
		return new Vector(this.x + v.x, this.y + v.y, this.z + v.z);
	}

	public Vector Nadd(Vector v){
		this.x+= v.x;
		this.y+= v.y;
		this.z+= v.z;
		return this;
	}

	//Subtraction

	public static Vector sub(Vector v, Vector u){
		return new Vector(v.x - u.x, v.y - u.y, v.z - u.z);
	}

	public Vector Nsub(Vector v){
		this.x-= v.x;
		this.y-= v.y;
		this.z-= v.z;
		return this;
	}

	//Norm	

	public double norm(){
		return Math.sqrt(dotProduct(this, this));
	}

	public Vector normalize(){
		return this.multiplyBy(1.0/this.norm());
	}

	public Vector Nnormalize(){
		this.NmultiplyBy(1.0/this.norm());
		return this;
	}
	//Multiply
	public Vector multiplyBy(double scalar){
		return new Vector(this.x*scalar, this.y*scalar, this.z*scalar);
	}

	public Vector NmultiplyBy(double scalar){
		this.x *= scalar;
		this.y *= scalar;
		this.z *= scalar;
		return this;
	}

	public Vector multiplyColor(Vector v){
		return new Vector(v.x*this.x, v.y*this.y, v.z*this.z);
	}

	public Vector NmultiplyColor(Vector v){
		this.x *= v.x;
		this.y *= v.y;
		this.z *= v.z;
		return this;
	}

	//miscellaneous

	public static double dotProduct(Vector v, Vector u){
		return (v.x*u.x + v.y*u.y + v.z*u.z);
	}

	public static Vector crossProduct(Vector v, Vector u){
		return new Vector(v.y*u.z - v.z*u.y, v.z*u.x - v.x*u.z, v.x*u.y- v.y*u.x);		
	}

	public static Vector fromPoints(Vector v, Vector u){
		return sub(u,v);
	}
	public Vector projection(Vector base){
		base = base.normalize();
		return base.multiplyBy(dotProduct(base, this));
	}


	public byte[] getRGB(){

		Vector v = this.multiplyBy(255);
		byte[] result = new byte[3];
		result[0] = (byte) (Math.round(v.x));
		result[1] = (byte) (Math.round(v.y));
		result[2] = (byte) (Math.round(v.z));
		return result;
	}

	public String str(){
		return String.format("<Vector: [%.4f,%.4f,%.4f]>", x, y, z);
	}

	public double distanceFrom(Vector v){
		//return fromPoints(this, v).norm();
		return Math.sqrt(Math.pow(this.x-v.x,2) + Math.pow(this.y-v.y,2) + Math.pow(this.z-v.z,2)); 
	}
	public void fixHighs(){
		if(this.x > 1) this.x =1;
		if(this.y > 1) this.y =1;
		if(this.z >1)  this.z =1;
	}

	public boolean isZero(){
		return (this.x==0 && this.y==0 && this.z==0);
	}
}


