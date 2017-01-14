package ch.fhnw.comgr.tron.models;

import ch.fhnw.util.color.RGBA;

import java.util.ArrayList;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Team {
    private final RGBA teamColor;
    private final ArrayList<Player> players = new ArrayList<>();

    public Team(RGBA teamColor) {
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
}
