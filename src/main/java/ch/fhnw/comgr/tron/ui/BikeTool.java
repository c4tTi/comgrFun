package ch.fhnw.comgr.tron.ui;

import java.awt.event.KeyEvent;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.controller.tool.ITool;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Vec3;

public class BikeTool implements ITool{
	
	private static final float TURNING_SPEED = 2f;
	private static final float SPEED = 0.2f;
	private final int leftKey, rightKey;
	private boolean turningLeft, turningRight;
	private Vec3 position;
	private float phi;
	
	public BikeTool(int leftKey, int rightKey) {
		this.leftKey = leftKey;
		this.rightKey = rightKey;
	}
	
	public void update(Vec3 position, float rotationAngle) {
		float x, y;
		x = (float) (Math.cos(Math.toRadians(phi)) * SPEED);
		y = (float) (Math.sin(Math.toRadians(phi)) * SPEED);
		this.position = position.add(new Vec3(x, y, 0));
		
		if(turningRight) phi -= TURNING_SPEED;
		if(turningLeft) phi += TURNING_SPEED;
	}
	
	public Vec3 getPosition() {
		return position;
	}
	
	public float getRotationAngle() {
		return phi;
	}
	
	@Override
	public void activate() {}

	@Override
	public void deactivate() {}

	@Override
	public void refresh(IView view) {}
	@Override
	public void keyPressed(IKeyEvent e) {
		if(e.getKey() == leftKey) {
			turningLeft = true;
			turningRight = false;
		}
		else if(e.getKey() == rightKey) {
			turningRight = true;
			turningLeft = false;
		}
	}

	@Override
	public void keyReleased(IKeyEvent e) {
		if(e.getKey() == leftKey) {
			turningLeft = false;
		}
		else if(e.getKey() == rightKey) {
			turningRight = false;
		}
	}

	@Override
	public void pointerPressed(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pointerReleased(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pointerClicked(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pointerMoved(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pointerDragged(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pointerScrolled(IPointerEvent e) {
		// TODO Auto-generated method stub
		
	}

}
