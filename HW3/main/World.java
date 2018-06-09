package main;

import java.util.ArrayList;
import java.util.List;

import geometry.Shape;
import scene.Camera;
import scene.Light;
import scene.Material;

public class World {
	public Camera camera; // the camera
	public List<Light> lights; // the lights
	public List<Shape> shapes; // the shapes
	public List<Material> materials; // the materials

	public World() {
		this.lights = new ArrayList<Light>();
		this.shapes = new ArrayList<Shape>();
		this.materials = new ArrayList<Material>();
	}
}
