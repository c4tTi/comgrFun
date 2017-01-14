package ch.fhnw.comgr.tron.models;

import ch.fhnw.ether.controller.IController;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Wall {
    private final IController controller;
    private final Team team;

    public Wall(IController controller, Team team) {
        this.controller = controller;
        this.team = team;
    }
}
