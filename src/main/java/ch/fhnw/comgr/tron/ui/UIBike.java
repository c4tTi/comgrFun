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

package ch.fhnw.comgr.tron.ui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.fhnw.comgr.tron.render.CustomMaterial;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.controller.tool.NavigationTool;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.DefaultScene;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.light.ILight;
import ch.fhnw.ether.scene.light.PointLight;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.util.color.RGB;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class UIBike{
    private IController controller;
    private IMesh mesh;
    private BikeTool bikeTool;
    private Vec3 position;
    private float rotationAngle;
    
    public UIBike(int leftKey, int rightKey) {
    	bikeTool = new BikeTool(leftKey, rightKey);
    }

    private static IMesh makeColoredTriangle() {
        float[] vertices = { 0, 0.5f, 0, 0.5f, 0, 0.5f, 0, 0, 0.5f };
        float[] colors = { 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 1, 1 };

        DefaultGeometry g = DefaultGeometry.createVC(vertices, colors);
        return new DefaultMesh(IMesh.Primitive.TRIANGLES, new CustomMaterial(2f), g);
    }
    
    public void enable(IController controller) throws IOException {
    	position = new Vec3(0,0,0);
    	rotationAngle = 0;
        this.controller = controller;
        controller.setTool(bikeTool);

        IScene scene = new DefaultScene(controller);
        controller.setScene(scene);
        
        ICamera cam = controller.getCamera(controller.getViews().get(0));
        
		DefaultCameraControl dcc = new DefaultCameraControl(cam);
		

        
        
        final List<IMesh> meshes = new ArrayList<>();

        new ObjReader(getClass().getResource("/assets/Bike/Tron.obj")).getMeshes().forEach(meshes::add);
        
        IMesh grid = Grid.makeGrid();
        final List<IMesh> bike = MeshUtilities.mergeMeshes(meshes);
        
        for (IMesh m : bike) {
            controller.getScene().add3DObject(m);
            controller.getScene().add3DObject(grid);
        }
        
        ILight light = new PointLight(new Vec3(0, -5, 0), RGB.BLACK, RGB.WHITE);
        
        controller.getScene().add3DObject(light);
      

        controller.animate((time, interval) -> {
        	bikeTool.update(position, rotationAngle);
        	position = bikeTool.getPosition();
        	rotationAngle = bikeTool.getRotationAngle();
        	
        	
        	dcc.setPosition(position.add(new Vec3(-5, 0, 4)));
            dcc.setTarget(position);
            //System.out.println("Position: " + (int) position.x + "/" + (int) position.y);
        	
            for (IMesh m : bike) {
                m.setPosition(position);
                m.setTransform(Mat4.rotate(bikeTool.getRotationAngle(), Vec3.Z));
            }
        	
        });
        
        
        
    }

    public void viewResized(float w, float h) {

    }
}
