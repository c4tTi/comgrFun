package ch.fhnw.comgr.tron.models;

import java.util.Random;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Flag;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class ExplosionParticle {
	
	public static final int MIN_LIFESPAN = 80;
	public static final int MAX_LIFESPAN = 120;
	private static final float EXPLOSION_SIZE = 3f;
	private static final float PARTICLE_SIZE = 0.4f;
	private static final float MAX_SPEED = 0.1f;
	private static final float MAX_ROTATION = 1;
	private IController controller;
	private IMesh particle;
	private int lifespan;
	private int currentFrame;
	private Vec3 position;
	private float xAngle, yAngle, zAngle;
	private float xRot, yRot, zRot;
	private Vec3 direction;
	
	public ExplosionParticle(IController controller, Vec3 bikePosition, RGBA rgba) {
		this.controller = controller;
		
		//Make it random
		Random r = new Random();
		float x = r.nextFloat()*EXPLOSION_SIZE - EXPLOSION_SIZE/2;
		float y = r.nextFloat()*EXPLOSION_SIZE - EXPLOSION_SIZE/2;
		position = bikePosition.add(new Vec3(x, y, 0));
		float xDir = r.nextFloat()*MAX_SPEED - MAX_SPEED/2;
		float yDir = r.nextFloat()*MAX_SPEED - MAX_SPEED/2;
		float zDir = r.nextFloat()*MAX_SPEED/2;
		direction = new Vec3(xDir, yDir, zDir);
		xAngle = r.nextFloat()*360;
		yAngle = r.nextFloat()*360;
		zAngle = r.nextFloat()*360;
		xRot = r.nextFloat()*MAX_ROTATION;
		yRot = r.nextFloat()*MAX_ROTATION;
		zRot = r.nextFloat()*MAX_ROTATION;
		lifespan = (int) ((r.nextFloat()*(MAX_LIFESPAN-MIN_LIFESPAN))+MIN_LIFESPAN);
		
		//Particle
		float[] vertices = {
				0f, 0f, 0f,
				0f, PARTICLE_SIZE, 0f,
				PARTICLE_SIZE, PARTICLE_SIZE, 0f
		};
		float[] colors = new float[3*4];
		for(int i = 0; i < colors.length; i++) {
			colors[i] = 1f;
		}
		float[] texCoords = new float[3*2];
		for(int i = 0; i < texCoords.length; i++) {
			texCoords[i] = 1f;
		}
		particle = new DefaultMesh(IMesh.Primitive.TRIANGLES, new ColorMaterial(rgba), DefaultGeometry.createVCM(vertices, colors, texCoords), IMesh.Queue.DEPTH, Flag.DONT_CULL_FACE);
		controller.getScene().add3DObject(particle);
	}
	
	public void update() {
		if(currentFrame < lifespan) {
			currentFrame++;
			position = position.add(direction);
			particle.setPosition(position);
			xAngle += xRot;
			yAngle += yRot;
			zAngle += zRot;
			particle.setTransform(Mat4.multiply(Mat4.rotate(xAngle, Vec3.X), Mat4.rotate(yAngle, Vec3.Y), Mat4.rotate(zAngle, Vec3.Z)));
		}
		else {
			particle.setPosition(new Vec3(999,999,999));
		}
		
		
	}
}
