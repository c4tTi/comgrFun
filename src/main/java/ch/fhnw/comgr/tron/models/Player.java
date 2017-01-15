package ch.fhnw.comgr.tron.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.comgr.tron.ui.BikeTool;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.light.PointLight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;
import ch.fhnw.util.math.geometry.BoundingBox;

/**
 * Created by Serquet.
 */
public class Player {
	private static final int CAMERA_DISTANCE = 20;
	private static final int CAMERA_HEIGHT = 10;
    private final IController controller;
    private final BikeTool bikeTool;
    private final IView view;
    private final ICamera cam;
    private final DefaultCameraControl cameraControl;
    private final Team team;

    private IMesh bike;
    private PointLight light;
    private Vec3 position;
    private float rotationAngle;
    private float curveLeanAngle;
    private boolean isTurningLeft, isTurningRight;
    private boolean dead, exploding;
    private int explosionFrame;
    ExplosionParticle[] explosionParticles;
    private int leftKey, rightKey;
    private final int playerIndex;


    public Player(IController controller, IView view, ICamera cam, Team team, int leftKey, int rightKey, BikeTool bikeTool, Vec3 pos, int playerIndex) {
        this.controller = controller;
        this.view = view;
        this.cam = cam;
        this.team = team;
        
        

        this.cameraControl = new DefaultCameraControl(cam);
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.bikeTool = bikeTool;
        position = pos;
    	rotationAngle = 0;
    	
    	this.playerIndex = playerIndex;
    	this.rotationAngle = -90 * playerIndex;
    	
    }

    public void enable() throws IOException {
        bike = LoadBikeModel();
    	bike.setPosition(position);
    	light = new PointLight(position, team.getTeamColor().toRGB(), team.getTeamColor().toRGB(), 12);
        controller.getScene().add3DObject(bike);
        controller.getScene().add3DObject(light);

        controller.animate((time, interval) -> {  
        	if(dead) {
        		if(exploding) {
        			if(explosionFrame < ExplosionParticle.MAX_LIFESPAN) {
        				explosionFrame++;
        				for(int i = 0; i < explosionParticles.length; i++) {
        					explosionParticles[i].update();
        				}
        			}
        			else {
        				exploding = false;
        			}
        		}
        	}
        	else {
	        	Mat4 trans = bikeTool.update(this);
                bike.setTransform(Mat4.multiply(Mat4.rotate(rotationAngle, Vec3.Z), Mat4.rotate(curveLeanAngle, Vec3.X)));

	        	float x = (float) (Math.cos(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
	        	float y = (float) (Math.sin(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
	        	cam.setPosition(position.add(new Vec3(-x, -y, CAMERA_HEIGHT)));
	            cam.setTarget(position);
	            //System.out.println("Position: " + (int) position.x + "/" + (int) position.y);
	            
	            float xLight = (float) (Math.cos(Math.toRadians(rotationAngle)) * 7);
	        	float yLight = (float) (Math.sin(Math.toRadians(rotationAngle)) * 7);
	            light.setPosition(position.add(new Vec3(xLight, yLight,2)));
                bike.setPosition(position);
        	}
        });

    }

    private IMesh LoadBikeModel() throws IOException {
        final List<IMesh> meshes = new ArrayList<>();
        new ObjReader(getClass().getResource("/assets/Bike/Tron.obj")).getMeshes().forEach(meshes::add);
        return MeshUtilities.mergeMeshes(meshes).get(0);
    }
    
	/**
	 * Calculates and returns the distance between this and player p.
	 */
	public float calculateDistance(Player p) {		
		return (float) Math.sqrt(((position.x - p.getPosition().x) * (position.x - p.getPosition().x))
				+ ((position.y - p.getPosition().y) * (position.y - p.getPosition().y)));
	}

    public Vec3 getPointBetween(Player p) {
        return Vec3.lerp(position, p.position, 0.5f);
    }
	
	/**
	 * Checks and returns if the player has collided with player p.
	 */
	public boolean collidedWithPlayer(Player p) {
		BoundingBox bb = bike.getBounds();
		return bb.intersects(p.getBoundingBox());
	}
	
	/**
	 * Checks and returns if the point (x,y) is inside the position of player p.
	 */
	public boolean pointInPlayer(float x, float y) {
		BoundingBox bb = bike.getBounds();
		return bb.contains(new Vec3(x, y, 0.5f));
	}
	
	public void die() {
		dead = true;
		controller.getScene().remove3DObject(bike);
		controller.getScene().remove3DObject(light);
		this.team.RemovePlayer(this);
		explode();
	}
	
	private void explode() {
		exploding = true;
		int amountOfParticles = 200;
		
		explosionParticles = new ExplosionParticle[amountOfParticles];
		
		for(int i = 0; i < amountOfParticles; i++) {
			explosionParticles[i] = new ExplosionParticle(controller, position, team.getTeamColor());
		}
	}
    
    public Mat4 getTransforma() { return bike.getTransform(); }
    
    public BoundingBox getBoundingBox() { return bike.getBounds(); }
	
    public void setPosition(Vec3 newPosition) { position = newPosition; }
    public Vec3 getPosition() { return position; }
    
    public void setRotationAngle(float newRotationAngle) { rotationAngle = newRotationAngle; }
    public float getRotationAngle() { return rotationAngle; }
    
    public void setCurveLeanAngle(float curveLeanAngle) { this.curveLeanAngle = curveLeanAngle; }
    public float getCurveLeanAngle() { return curveLeanAngle; }
    
    public void setIsTurningLeft(boolean b) { isTurningLeft = b; }
    public boolean isTurningLeft() { return isTurningLeft; }
    
    public void setIsTurningRight(boolean b) { isTurningRight = b; }
    public boolean isTurningRight() { return isTurningRight; }
    
    public int getLeftKey() { return leftKey; }
    public int getRightKey() { return rightKey; }
    
    public Team getTeam() { return team; }
    
    public boolean isDead() { return dead; }
}
