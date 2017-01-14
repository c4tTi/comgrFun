package ch.fhnw.comgr.tron.ui;

import java.util.ArrayList;

import ch.fhnw.comgr.tron.models.Player;
import ch.fhnw.comgr.tron.models.Team;
import ch.fhnw.ether.controller.event.IKeyEvent;
import ch.fhnw.ether.controller.event.IPointerEvent;
import ch.fhnw.ether.controller.tool.ITool;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Vec3;

public class BikeTool implements ITool{
	
	private static final float TURNING_SPEED = 2f;
	private static final float SPEED = 0.2f;
	private static final float MAX_BIND_DISTANCE = 20f;
	Player[] players;
	Team[] teams;
	
	public BikeTool(Player[] players, Team[] teams) {
		this.players = players;
		this.teams = teams;
	}
	
	/**
	 * Updates position, rotation and wall bindings of given player.
	 */
	public void update(Player player) {
		float x, y;
		x = (float) (Math.cos(Math.toRadians(player.getRotationAngle())) * SPEED);
		y = (float) (Math.sin(Math.toRadians(player.getRotationAngle())) * SPEED);
		player.setPosition(player.getPosition().add(new Vec3(x, y, 0)));
		
		if(player.isTurningLeft()) player.setRotationAngle(player.getRotationAngle() + TURNING_SPEED);
		if(player.isTurningRight()) player.setRotationAngle(player.getRotationAngle() - TURNING_SPEED);
		
		//Check for distance between teammembers
		for(Player teamMember : player.getTeam().getPlayers()) {
			if(player != teamMember && player.calculateDistance(teamMember) < MAX_BIND_DISTANCE) {
				//TODO: Generate laser thing between players 'player' and 'teamMember'
			}
		}
		
		//Check for collision between players
		for(Player otherPlayer : players) {
			if(player != otherPlayer && player.collidedWithPlayer(otherPlayer)) {
				player.die();
				otherPlayer.die();
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
