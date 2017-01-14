package ch.fhnw.comgr.tron.models;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Wall {
    private static final float MAX_BIND_DISTANCE = 20f;
    private static final float WALL_HEIGHT = 1.5f;

    private final IController controller;
    private final Team team;
    private final Player playerA;
    private final Player playerB;

    private IMaterial material;
    private float texOff = 0;
    private float texOff2 = 0;
    private float[] colors;

    private boolean isWallBuilding = false;
    private Vec3 previousEdge;
    private Vec3 tmpSegmentStart;
    private List<IMesh> tmpSegmentMeshes = new ArrayList<>();
    private List<IMesh> currentSegmentMeshes = new ArrayList<>();
    private int segmentCounter = 0;


    public Wall(IController controller, Team team, Player playerA, Player playerB) {
        this.controller = controller;
        this.team = team;
        this.playerA = playerA;
        this.playerB = playerB;

        if (playerA == playerB) {
            throw new RuntimeException();
        }
    }

    public void enable() {
        createMaterial();
        createColors();

        controller.animate((time, interval) -> {
            // TODO: use squared distance maybe
            if (playerA.calculateDistance(playerB) < MAX_BIND_DISTANCE) {
                //TODO: Generate laser thing between players 'player' and 'teamMember'
                addWallSegment(playerA.getPointBetween(playerB));
            } else {
                stopWallBuilding();
            }
        });
    }

    private void createMaterial() {
//        IGPUImage t = null;
//        try {
//            t = IGPUImage.read(Wall.class.getResource("/assets/textures/Microscheme.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        material = new ColorMapMaterial(team.getTeamColor(), t, true);
        material = new ColorMaterial(team.getTeamColor());
    }

    private void createColors() {
        RGBA teamColor = team.getTeamColor();
        float r = Math.max(0.0f, teamColor.r - 0.2f);
        float g = Math.max(0.0f, teamColor.g - 0.2f);
        float b = Math.max(0.0f, teamColor.b - 0.2f);

        colors = new float[4 * 4];
        for (int i = 0; i < 4; i++) {
            colors[3 * i] = r;
            colors[3 * i + 1] = g;
            colors[3 * i + 2] = b;
            colors[3 * i + 3] = 1;
        }
    }

    public void addWallSegment(Vec3 newEdge) {
        if (isWallBuilding) {
            addSegment(previousEdge, newEdge);
        }
        else
        {
            tmpSegmentStart = newEdge;
        }

        previousEdge = newEdge;
        isWallBuilding = true;
    }

    public void stopWallBuilding() {
        isWallBuilding = false;

        mergeTmpSegments(previousEdge);
        mergeMeshes();
    }

    public void addSegment(Vec3 start, Vec3 end) {
        segmentCounter++;

        if (segmentCounter % 10 == 0) {
            mergeTmpSegments(end);
        } else {
            IMesh mesh = makeWallSegment(start.x, start.y, end.x, end.y);
            controller.getScene().add3DObject(mesh);
            tmpSegmentMeshes.add(mesh);
        }

        if (segmentCounter % 100 == 0) {
            mergeMeshes();
        }
    }

    private void mergeTmpSegments(Vec3 tmpSegmentEnd) {
        IScene scene = controller.getScene();

        for (IMesh m : tmpSegmentMeshes) {
            scene.remove3DObject(m);
        }
        tmpSegmentMeshes.clear();

        IMesh mesh = makeWallSegment(tmpSegmentStart.x, tmpSegmentStart.y, tmpSegmentEnd.x, tmpSegmentEnd.y);
        controller.getScene().add3DObject(mesh);

        tmpSegmentStart = tmpSegmentEnd;
    }

    private void mergeMeshes() {
        segmentCounter = 0;
        currentSegmentMeshes = MeshUtilities.mergeMeshes(currentSegmentMeshes);
    }

    public IMesh makeWallSegment(float x0, float y0, float x1, float y1) {
        float dx = (x1 - x0);
        float dy = (y1 - y0);
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        float[] vertices = {
                x0, y0, 0,
                x0, y0, WALL_HEIGHT,
                x1, y1, 0,
                x1, y1, WALL_HEIGHT
        };
        float[] texCoords = {
                0, texOff,
                1, texOff,
                0, texOff + length,
                1, texOff + length};

        texOff += length;

        return new DefaultMesh(IMesh.Primitive.TRIANGLE_STRIP, material, DefaultGeometry.createV(vertices), IMesh.Queue.DEPTH);
    }
}
