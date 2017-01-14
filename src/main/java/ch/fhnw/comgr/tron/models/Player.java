package ch.fhnw.comgr.tron.models;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.formats.obj.ObjReader;
import ch.fhnw.ether.scene.camera.DefaultCameraControl;
import ch.fhnw.ether.scene.camera.ICamera;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.view.IView;
import ch.fhnw.util.math.Vec3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Player {
    private final IController controller;
    private final IView view;
    private final ICamera cam;
    private final DefaultCameraControl cameraControl;
    private List<IMesh> bike;

    public Player(IController controller, IView view, ICamera cam) {
        this.controller = controller;
        this.view = view;
        this.cam = cam;
        this.cameraControl = new DefaultCameraControl(cam);
    }

    public void enable(Vec3 test) throws IOException {
        bike = LoadBikeModel();

        for (IMesh m : bike) {
            controller.getScene().add3DObject(m);
        }

        controller.animate((time, interval) -> {
            for (IMesh m : bike) {
                Vec3 move = test;

                m.setPosition(m.getPosition().add(move));

                cameraControl.setPosition(cam.getPosition().add(move));
                cameraControl.setTarget(m.getPosition());
            }
        });

    }

    private List<IMesh> LoadBikeModel() throws IOException {
        final List<IMesh> meshes = new ArrayList<>();
        new ObjReader(getClass().getResource("/assets/Bike/Tron.obj")).getMeshes().forEach(meshes::add);
        return MeshUtilities.mergeMeshes(meshes);
    }
}
