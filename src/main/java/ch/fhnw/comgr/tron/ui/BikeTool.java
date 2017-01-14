package ch.fhnw.comgr.tron.ui;

import java.util.ArrayList;

import ch.fhnw.comgr.tron.models.Player;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.controller.tool.ITool;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Vec3;

public class BikeTool implements ITool{
	
	private static final float TURNING_SPEED = 2f;
	private static final float SPEED = 0.2f;
	ArrayList<Player> players;
	
	public BikeTool() {
		players = new ArrayList<Player>();
	}
	
	public void addPlayer(Player p) {
		players.add(p);
	}
	
	public void update(Player player) {
		for(Player p : players) {
			if(p == player) {
				float x, y;
				x = (float) (Math.cos(Math.toRadians(p.getRotationAngle())) * SPEED);
				y = (float) (Math.sin(Math.toRadians(p.getRotationAngle())) * SPEED);
				p.setPosition(p.getPosition().add(new Vec3(x, y, 0)));
				
				if(p.isTurningLeft()) p.setRotationAngle(p.getRotationAngle() - TURNING_SPEED);
				if(p.isTurningRight()) p.setRotationAngle(p.getRotationAngle() + TURNING_SPEED);
			}
		}
	}
	
	@Override
	public void activate() {}

	@Override
	public void deactivate() {}

	@Override
	public void refresh(IView view) {}
	@Override
	public void keyPressed(IKeyEvent e) {
		for(Player p : players) {
			if(e.getKey() == p.getLeftKey()) {
				p.setIsTurningLeft(true);
				p.setIsTurningRight(false);
			}
			if(e.getKey() == p.getRightKey()) {
				p.setIsTurningRight(true);
				p.setIsTurningLeft(false);
			}
		}
	}

	@Override
	public void keyReleased(IKeyEvent e) {
		for(Player p : players) {
			if(e.getKey() == p.getLeftKey()) {
				p.setIsTurningLeft(false);
			}
			if(e.getKey() == p.getRightKey()) {
				p.setIsTurningRight(false);
			}
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
