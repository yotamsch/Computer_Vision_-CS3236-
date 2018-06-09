package main;

import geometry.Shape;
import utility.Vector;

public class Intersection {
	public Shape shape;
	public Vector point;
	public double tValue;

	public Intersection() {
		this.shape = null;
		this.point = null;
		this.tValue = 0;
	}
}

