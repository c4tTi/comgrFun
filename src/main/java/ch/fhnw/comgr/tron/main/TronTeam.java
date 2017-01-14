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

import java.io.IOException;
import java.util.Random;

import ch.fhnw.comgr.tron.models.Player;
import ch.fhnw.comgr.tron.models.Team;
import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.render.IRenderManager;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.light.PointLight;
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

    private final Team[] teams;
    private final Player[] players;
    
    private final RGBA[] teamColors = new RGBA[]{ RGBA.GREEN, RGBA.BLUE, RGBA.RED, RGBA.CYAN};

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

        controller.run(time -> {
            IScene scene = new DefaultScene(controller);
            controller.setScene(scene);

            // Create player instances
            CreateTeams(controller);
            CreatePlayers(controller);
            CreateLights(scene);
        });
    }

    private void CreateTeams(IController controller) {
        for (int i  = 0; i < NR_OF_TEAMS; i++)
        {
            if (i < teamColors.length)
            {
                teams[i] = new Team(teamColors[i]);
            }
            else
            {
                teams[i] = new Team(createRandomColor());
            }
        }
    }

    private void CreateLights(IScene scene) {
        ILight light = new PointLight(new Vec3(0, -5, 0), RGB.BLACK, RGB.WHITE);
        scene.add3DObject(light);
    }

    private void CreatePlayers(IController controller) {
        IRenderManager renderManager = controller.getRenderManager();

        int full_width = Platform.get().getMonitors()[0].getWidth();
        int full_height = Platform.get().getMonitors()[0].getHeight();

        int window_width = full_width / NR_OF_TEAMS;
        int window_height = full_height / TEAM_SIZES;

        for (int i = 0; i < NR_PLAYERS; i++) {
            try {
                AddPlayer(renderManager, controller, i, window_width, window_height);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    private void AddPlayer(IRenderManager renderManager, IController controller, int playerIndex, int window_width, int window_height)
            throws IOException {
        int teamOffset   = playerIndex / TEAM_SIZES;
        int playerOffset = playerIndex % TEAM_SIZES;

        final IView view = new DefaultView(controller,
                teamOffset*window_width,
                playerOffset*window_height,
                window_width, window_height,
                IView.RENDER_VIEW, "Player " + playerIndex);
        renderManager.getCamera(view);

        final ICamera cam = renderManager.getCamera(view);

        Vec3 test;
        switch (playerIndex)
        {
            case 0:
                test = new Vec3(0.01, 0, 0);
                break;
            case 1:
                test = new Vec3(0, 0.01, 0);
                break;
            case 2:
                test = new Vec3(0.01, 0.01, 0);
                break;
            default:
                test = new Vec3(0.02, 0.02, 0);
                break;
        }

        Player player = new Player(controller, view, cam, teams[teamOffset]);
        player.enable(test);
    }

    public RGBA createRandomColor() {
        Random rand = new Random();
        return new RGBA(rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), 1);
    }
}
