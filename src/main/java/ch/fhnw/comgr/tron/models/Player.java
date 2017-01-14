package ch.fhnw.comgr.tron.models;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.comgr.tron.ui.BikeTool;
import ch.fhnw.comgr.tron.ui.Grid;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec2;
import ch.fhnw.util.math.Vec3;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Player {
	private static final float BIKE_WIDTH = 3.2f;
	private static final float BIKE_LENGTH = 0.9f;
	private static final int CAMERA_DISTANCE = 20;
	private static final int CAMERA_HEIGHT = 10;
	private static final float MAX_CURVE_LEAN_ANGLE = 30;
    private final IController controller;
    private final BikeTool bikeTool;
    private final IView view;
    private final ICamera cam;
    private final DefaultCameraControl cameraControl;

    private final Team team;

    private List<IMesh> bike;
    private Vec3 position;
    private float rotationAngle;
    private float curveLeanAngle;
    private boolean isTurningLeft, isTurningRight;
    private boolean dead;
    private int leftKey, rightKey;

    private static int i;

    public Player(IController controller, IView view, ICamera cam, Team team, int leftKey, int rightKey, BikeTool bikeTool) {
        this.controller = controller;
        this.view = view;
        this.cam = cam;
        this.team = team;

        this.cameraControl = new DefaultCameraControl(cam);
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.bikeTool = bikeTool;
        position = new Vec3(0,10*i,0);
    	rotationAngle = 0;
    	i++;
    }

    public void enable() throws IOException {
        bike = LoadBikeModel();
        IMesh grid = Grid.makeGrid();

        for (IMesh m : bike) {
            controller.getScene().add3DObject(m);
            controller.getScene().add3DObject(grid);
        }

        controller.animate((time, interval) -> {   
        	if(dead) {
        		System.out.println("lul u ded");
        	}
        	else {
	        	bikeTool.update(this);
	        	float x = (float) (Math.cos(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
	        	float y = (float) (Math.sin(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
	        	cam.setPosition(position.add(new Vec3(-x, -y, CAMERA_HEIGHT)));
	            cam.setTarget(position);
	            //System.out.println("Position: " + (int) position.x + "/" + (int) position.y);
	        	
	            for (IMesh m : bike) {
	                m.setPosition(position);
	                if(isTurningRight) {
	                	if(curveLeanAngle < MAX_CURVE_LEAN_ANGLE) curveLeanAngle++;
	                }
	                else if(isTurningLeft) {
	                	if(curveLeanAngle > -MAX_CURVE_LEAN_ANGLE) curveLeanAngle--;
	                }
	                else {
	                	if(curveLeanAngle > 0) curveLeanAngle--;
	                	else if(curveLeanAngle < 0) curveLeanAngle++;
	                }
	                m.setTransform(Mat4.multiply(Mat4.rotate(rotationAngle, Vec3.Z), Mat4.rotate(curveLeanAngle, Vec3.X)));
	            }
        	}
        });

    }

    private List<IMesh> LoadBikeModel() throws IOException {
        final List<IMesh> meshes = new ArrayList<>();
        new ObjReader(getClass().getResource("/assets/Bike/Tron.obj")).getMeshes().forEach(meshes::add);
        return MeshUtilities.mergeMeshes(meshes);
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
		return (pointInPlayer(position.x - BIKE_WIDTH/2, position.y - BIKE_LENGTH/2, p)
				|| pointInPlayer(position.x - BIKE_WIDTH/2, position.y + BIKE_LENGTH/2, p)
				|| pointInPlayer(position.x + BIKE_WIDTH/2, position.y - BIKE_LENGTH/2, p)
				|| pointInPlayer(position.x + BIKE_WIDTH/2, position.y + BIKE_LENGTH/2, p));
	}
	
	/**
	 * Checks and returns if the point (x,y) is inside the position of player p.
	 */
	private boolean pointInPlayer(float x, float y, Player p) {
		return (x > p.getPosition().x - BIKE_WIDTH/2 
				&& x < p.getPosition().x + BIKE_WIDTH
				&& y > p.getPosition().y - BIKE_LENGTH
				&& y < p.getPosition().y + BIKE_LENGTH);
	}
	
	public void die() {
		dead = true;
	}
    
    
    public void setPosition(Vec3 newPosition) { position = newPosition; }
    public Vec3 getPosition() { return position; }
    
    public void setRotationAngle(float newRotationAngle) { rotationAngle = newRotationAngle; }
    public float getRotationAngle() { return rotationAngle; }
    
    public void setIsTurningLeft(boolean b) { isTurningLeft = b; }
    public boolean isTurningLeft() { return isTurningLeft; }
    
    public void setIsTurningRight(boolean b) { isTurningRight = b; }
    public boolean isTurningRight() { return isTurningRight; }
    
    public int getLeftKey() { return leftKey; }
    public int getRightKey() { return rightKey; }
    
    public Team getTeam() { return team; }
}
