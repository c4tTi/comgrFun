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
import ch.fhnw.util.math.Vec3;

/**
 * Created by Serquet on 14.01.2017.
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

    private List<IMesh> bike;
    private Vec3 position;
    private float rotationAngle;
    private boolean isTurningLeft, isTurningRight;
    private int leftKey, rightKey;

    public Player(IController controller, IView view, ICamera cam, Team team, int leftKey, int rightKey, BikeTool bikeTool) {
        this.controller = controller;
        this.view = view;
        this.cam = cam;
        this.team = team;

        this.cameraControl = new DefaultCameraControl(cam);
        this.leftKey = leftKey;
        this.rightKey = rightKey;
        this.bikeTool = bikeTool;
        position = new Vec3(0,0,0);
    	rotationAngle = 0;
    }

    public void enable() throws IOException {
        this.team.AddPlayer(this);
        bike = LoadBikeModel();
        IMesh grid = Grid.makeGrid();

        for (IMesh m : bike) {
            controller.getScene().add3DObject(m);
            controller.getScene().add3DObject(grid);
        }

        controller.animate((time, interval) -> {   
        	bikeTool.update(this);
        	float x = (float) (Math.cos(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
        	float y = (float) (Math.sin(Math.toRadians(rotationAngle)) * CAMERA_DISTANCE);
        	cam.setPosition(position.add(new Vec3(-x, -y, CAMERA_HEIGHT)));
            cam.setTarget(position);
            //System.out.println("Position: " + (int) position.x + "/" + (int) position.y);
        	
            for (IMesh m : bike) {
                m.setPosition(position);
                m.setTransform(Mat4.rotate(rotationAngle, Vec3.Z));
            }
        	
        });

    }

    private List<IMesh> LoadBikeModel() throws IOException {
        final List<IMesh> meshes = new ArrayList<>();
        new ObjReader(getClass().getResource("/assets/Bike/Tron.obj")).getMeshes().forEach(meshes::add);
        return MeshUtilities.mergeMeshes(meshes);
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
}
