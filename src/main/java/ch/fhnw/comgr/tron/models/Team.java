package ch.fhnw.comgr.tron.models;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

import java.util.ArrayList;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Team {
    private final IController controller;
    private final RGBA teamColor;
    private final ArrayList<Player> players = new ArrayList<>();
    private final ArrayList<Wall> walls = new ArrayList<>();

    public Team(IController controller, RGBA teamColor) {
        this.controller = controller;
        this.teamColor = teamColor;
    }

    public void AddPlayer(Player p)
    {
        players.add(p);
    }

    public RGBA getTeamColor() {
        return teamColor;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public ArrayList<Wall> getWalls() {
        return walls;
    }

    public void enable() {
        for (int i = 0; i < players.size(); i++) {
            for (int j = i+1; j < players.size(); j++) {
                Wall wall = new Wall(controller, this, players.get(i), players.get(j));
                walls.add(wall);
                wall.enable();
            }
        }
    }
}
