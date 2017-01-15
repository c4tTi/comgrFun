/*
 * Copyright (c) 2015 - 2016 Stefan Muller Arisona, Simon Schubiger
 * Copyright (c) 2015 - 2016 FHNW & ETH Zurich
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *  Neither the name of FHNW / ETH Zurich nor the names of its contributors may
 *   be used to endorse or promote products derived from this software without
 *   specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.fhnw.comgr.tron.main;

import java.awt.event.KeyEvent;
import java.io.IOException;

import ch.fhnw.comgr.tron.models.BorderWall;
import ch.fhnw.comgr.tron.models.Player;
import ch.fhnw.comgr.tron.models.Team;
import ch.fhnw.comgr.tron.ui.BikeTool;
import ch.fhnw.comgr.tron.ui.Grid;
import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.render.IRenderManager;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.light.DirectionalLight;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.AutoDisposer;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class TronTeam {

    private static final int TEAM_SIZES = 2;
    private static final int NR_OF_TEAMS = 2;
    private static final int NR_PLAYERS = NR_OF_TEAMS * TEAM_SIZES;
    private static final float MAP_SIZE = 50f;
    private static final int[] KEYS = {KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_O, KeyEvent.VK_P, KeyEvent.VK_N, KeyEvent.VK_M};
    private static final Vec3[] START_POS = { new Vec3(-MAP_SIZE/2, MAP_SIZE/2, 0), new Vec3(MAP_SIZE/2, MAP_SIZE/2, 0), new Vec3(MAP_SIZE/2, -MAP_SIZE/2, 0), new Vec3(-MAP_SIZE/2, -MAP_SIZE/2, 0)};
    private static final RGBA[] teamColors = new RGBA[]{ RGBA.GREEN, RGBA.BLUE, RGBA.RED, RGBA.CYAN};

    private final Team[] teams;
    private final Player[] players;


    public static void main(String[] args) {
        Platform.get().init();

        try {
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep(10000);
                        //System.out.println("run gc");
                        AutoDisposer.runGC();
                    }
                } catch (Exception e) {
                }
            }, "gc forcer");
            t.setDaemon(true);
            t.start();

            new TronTeam();

            Platform.get().run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public TronTeam() throws IOException {
        final IController controller = new DefaultController();
       

        teams = new Team[NR_OF_TEAMS];
        players = new Player[NR_PLAYERS];
        
        final BikeTool bikeTool = new BikeTool(MAP_SIZE, players, teams);

        controller.run(time -> {
            IScene scene = new DefaultScene(controller);
            controller.setScene(scene);
            controller.setTool(bikeTool);

            IMesh grid = Grid.createSquareMapStandardMat(MAP_SIZE, (int) (MAP_SIZE / 2));
            controller.getScene().add3DObject(grid);

            CreateBorderWalls(controller);
            CreatePlayers(controller, bikeTool);
            CreateLights(scene);
        });
    }

    private void CreateGUI(IController controller, int window_width, int window_height) {
        try {
            int s = 32;

            IGPUImage tl = IGPUImage.read(Grid.class.getResource("/assets/gui/border_top_left.png"));
            IGPUImage t  = IGPUImage.read(Grid.class.getResource("/assets/gui/border_top.png"));
            IGPUImage tr = IGPUImage.read(Grid.class.getResource("/assets/gui/border_top_right.png"));
            IGPUImage r  = IGPUImage.read(Grid.class.getResource("/assets/gui/border_right.png"));
            IGPUImage bl = IGPUImage.read(Grid.class.getResource("/assets/gui/border_bottom_left.png"));
            IGPUImage b  = IGPUImage.read(Grid.class.getResource("/assets/gui/border_bottom.png"));
            IGPUImage br = IGPUImage.read(Grid.class.getResource("/assets/gui/border_bottom_right.png"));
            IGPUImage l  = IGPUImage.read(Grid.class.getResource("/assets/gui/border_left.png"));

            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(0, 0, s, s, RGBA.CYAN, bl));
            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(s, 0, window_width - s, s, RGBA.CYAN, b));
            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(window_width - s,0,window_width,s, RGBA.CYAN, br));

            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(0, window_height - s, s, window_height, RGBA.CYAN, tl));
            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(s, window_height - s, window_width - s, window_height, RGBA.CYAN, t));
            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(window_width - s, window_height - s, window_width, window_height, RGBA.CYAN, tr));

            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(0,s,s,window_height-s, RGBA.CYAN, l));
            controller.getScene().add3DObject(MeshUtilities.createScreenRectangle(window_width-s,s,window_width,window_height-s, RGBA.CYAN, r));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void CreateLights(IScene scene) {
        ILight light = new DirectionalLight(new Vec3(0, -5, 10), RGB.BLACK, RGB.GRAY50);
        scene.add3DObject(light);
    }
    
    private void CreateBorderWalls(IController controller) {
    	new BorderWall(controller, MAP_SIZE);
    }

    private void CreatePlayers(IController controller, BikeTool bikeTool) {
        IRenderManager renderManager = controller.getRenderManager();

        int full_width = Platform.get().getMonitors()[0].getWidth();
        int full_height = Platform.get().getMonitors()[0].getHeight();

        int window_width = full_width / NR_OF_TEAMS;
        int window_height = full_height / TEAM_SIZES;

        CreateGUI(controller, window_width, window_height);

        for (int i  = 0; i < NR_OF_TEAMS; i++)
        {
            teams[i] = new Team(controller, teamColors[i]);
        }

        for (int i = 0; i < NR_PLAYERS; i++) {
            try {
                AddPlayer(renderManager, controller, i, window_width, window_height, bikeTool, KEYS[2*i], KEYS[2*i+1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i  = 0; i < NR_OF_TEAMS; i++)
        {
            teams[i].enable();
        }
    }


    private void AddPlayer(IRenderManager renderManager, IController controller, int playerIndex, int window_width, int window_height, BikeTool bikeTool, int leftKey, int rightKey)
            throws IOException {
        int teamOffset   = playerIndex / TEAM_SIZES;
        int playerOffset = playerIndex % TEAM_SIZES;

        final IView view = new DefaultView(controller,
                teamOffset*window_width,
                playerOffset*window_height,
                window_width, window_height,
                IView.RENDER_VIEW, "Player " + playerIndex);

        final ICamera cam = renderManager.getCamera(view);
        
        Player player = new Player(controller, view, cam, teams[teamOffset], leftKey, rightKey, bikeTool, START_POS[playerIndex], playerIndex);
        players[playerIndex] = player;
        teams[teamOffset].AddPlayer(player);
        player.enable();
    }
}
