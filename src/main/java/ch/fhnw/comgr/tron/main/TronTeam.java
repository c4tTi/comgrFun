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

import ch.fhnw.comgr.tron.render.CustomMaterial;
import ch.fhnw.comgr.tron.ui.UI;
import ch.fhnw.comgr.tron.ui.UIBike;
import ch.fhnw.ether.controller.DefaultController;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.platform.Platform;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.view.DefaultView;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.AutoDisposer;

public class TronTeam {
    
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
                } catch (Exception e) {}
            }, "gc forcer");
            t.setDaemon(true);
            t.start();

            new TronTeam(true);
            
            Platform.get().run();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }    
    }

    public TronTeam(boolean fullscreen) throws IOException {
        final UIBike gui = new UIBike();
        final IController controller = new DefaultController() {
            @Override
            public void viewResized(IView view) {
                gui.viewResized(view.getViewport().w, view.getViewport().h);
                super.viewResized(view);
            }
        };

        controller.run(time -> {
            final IView view = new DefaultView(controller, 100, 100, 960, 540, new IView.Config(IView.ViewType.INTERACTIVE_VIEW, 0, new IView.ViewFlag[0]), "Test");
            controller.setScene(new DefaultScene(controller));
            
            try {
                gui.enable(controller);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (fullscreen) {
                Platform.get().runOnMainThread(() -> view.getWindow().setFullscreen(Platform.get().getMonitors()[0]));
            }
        });
    }
}
