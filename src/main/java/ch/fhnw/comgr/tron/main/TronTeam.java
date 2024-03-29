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
import java.util.List;

import ch.fhnw.comgr.tron.models.BorderWall;
import ch.fhnw.comgr.tron.models.Player;
import ch.fhnw.comgr.tron.models.Team;
import ch.fhnw.comgr.tron.ui.BikeTool;
import ch.fhnw.comgr.tron.ui.Grid;
import ch.fhnw.comgr.tron.ui.Skybox;
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
    private final IController controller;
    private IMesh gameOverMesh;
    private boolean gameOver = false;


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
        controller = new DefaultController();

        teams = new Team[NR_OF_TEAMS];
        players = new Player[NR_PLAYERS];

        final int full_width = Platform.get().getMonitors()[0].getWidth();
        final int full_height = Platform.get().getMonitors()[0].getHeight();
        final int window_width = full_width / NR_OF_TEAMS;
        final int window_height = full_height / TEAM_SIZES;
        final int window_center_x = window_width / 2;
        final int window_center_y = window_height / 2;

        final BikeTool bikeTool = new BikeTool(MAP_SIZE, players, teams);

        IMesh[] cd = new IMesh[4];
        for (int i = 0; i < cd.length; i++)
        {
            try {
                IGPUImage cdT = IGPUImage.read(TronTeam.class.getResource("/assets/gui/cd_" + i + ".png"));
                cd[i] = MeshUtilities.createScreenRectangle(
                        window_center_x-128, window_center_y-128,
                        window_center_x+128, window_center_y+128,
                        RGBA.CYAN, cdT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        // game over
        try {
            IGPUImage gameOver = IGPUImage.read(TronTeam.class.getResource("/assets/gui/game_over.png"));
            gameOverMesh = MeshUtilities.createScreenRectangle(
                                window_center_x-256, window_center_y-256,
                                window_center_x+256, window_center_y+256,
                                RGBA.CYAN, gameOver);
        } catch (IOException e) {
            e.printStackTrace();
        }

        controller.run(time -> {
            IScene scene = new DefaultScene(controller);
            controller.setScene(scene);
            controller.setTool(bikeTool);

            IMesh grid = Grid.createSquareMapStandardMat(MAP_SIZE, (int) (MAP_SIZE / 2));
            controller.getScene().add3DObject(grid);

            List<IMesh> skybox = Skybox.createSkybox(MAP_SIZE * 100, -MAP_SIZE *45);
            controller.getScene().add3DObjects(skybox);

            CreateGUI(controller, window_width, window_height);
            CreateBorderWalls(controller);
            CreatePlayers(controller, bikeTool, window_width, window_height);
            CreateLights(scene);
        });

        startCoolDown(controller, cd);
    }

    public void onGameOver()
    {
        if (!gameOver) {
            controller.getScene().add3DObject(gameOverMesh);
            gameOver = true;

            for (Player p : players) {
                p.stop();
            }
        }
    }

    private void startCoolDown(IController controller, IMesh[] cd) {

        final int k = cd.length - 1;
        for (int i = -1; i < cd.length; i++) {
            final double delay = 0.9 * (cd.length - i);
            final int cdIndex = i;
            controller.run(delay, time -> {
                if (cdIndex < k) {
                    final IMesh toRemove = cd[cdIndex + 1];
                    cd[cdIndex + 1] = null;
                    controller.getScene().remove3DObject(toRemove);
                }
                if (cdIndex >= 0) {
                    controller.getScene().add3DObject(cd[cdIndex]);
                }
                if (cdIndex == 0) {
                    for (Player p : players) {
                        p.start();
                    }
                }
            });
        }
    }

    private void CreateGUI(IController controller, int window_width, int window_height) {
        try {
            int s = 32;

            IGPUImage tl = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_top_left.png"));
            IGPUImage t  = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_top.png"));
            IGPUImage tr = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_top_right.png"));
            IGPUImage r  = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_right.png"));
            IGPUImage bl = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_bottom_left.png"));
            IGPUImage b  = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_bottom.png"));
            IGPUImage br = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_bottom_right.png"));
            IGPUImage l  = IGPUImage.read(TronTeam.class.getResource("/assets/gui/border_left.png"));

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

    private void CreatePlayers(IController controller, BikeTool bikeTool, int window_width, int window_height) {
        IRenderManager renderManager = controller.getRenderManager();

        for (int i  = 0; i < NR_OF_TEAMS; i++)
        {
            teams[i] = new Team(controller, teamColors[i], this);
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
