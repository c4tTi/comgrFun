package ch.fhnw.comgr.tron.models;

import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.IScene;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.ColorMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Serquet on 14.01.2017.
 */
public class Wall {
    private static final float MAX_BIND_DISTANCE = 20f;
    private static final float WALL_HEIGHT = 1.5f;
    private static final float WALL_THICKNESS = 0.1f;

    private static final int MERGED_WALL_PART_SIZE = 15;
    private static final int WALL_SEGMENT_SIZE = MERGED_WALL_PART_SIZE * 20;

    private final IController controller;
    private final Team team;
    private final Player playerA;
    private final Player playerB;

    private IMaterial material;
    private float texOff = 0;
    private float tmpTexOff = 0;
    private float[] colors;

    private boolean isWallBuilding = false;
    private Vec3 previousEdge;
    private Vec3 tmpSegmentStart;
    private List<IMesh> tmpSegmentMeshes = new ArrayList<>();
    private List<IMesh> currentSegmentMeshes = new ArrayList<>();
    private int wallPartCounter = 0;

    private List<WallSegment> segments = new ArrayList<>();
    private WallSegment currentSegment;


    public Wall(IController controller, Team team, Player playerA, Player playerB) {
        this.controller = controller;
        this.team = team;
        this.playerA = playerA;
        this.playerB = playerB;

        if (playerA == playerB || playerA.getTeam() != playerB.getTeam()) {
            throw new IllegalArgumentException();
        }
    }

    public void enable() {
        createMaterial();
        createColors();

        controller.animate((time, interval) -> {
            // TODO: use squared distance maybe
            if (playerA.calculateDistance(playerB) < MAX_BIND_DISTANCE) {
                addWallSegment(playerA.getPointBetween(playerB));
            } else {
                stopWallBuilding();
            }
        });
    }

    public boolean checkCollision(Player p) {
        for (WallSegment s : segments) {
            if (s.checkCollision(p)) {
                return true;
            }
        }
        return false;
    }

    private void createMaterial() {
        IGPUImage t = null;
        try {
            t = IGPUImage.read(Wall.class.getResource("/assets/textures/Microscheme.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        material = new ColorMapMaterial(team.getTeamColor(), t, true);
        //material = new ColorMaterial(team.getTeamColor());
    }

    private void createColors() {
        RGBA teamColor = team.getTeamColor();
        float r = Math.max(0.0f, teamColor.r - 0.2f);
        float g = Math.max(0.0f, teamColor.g - 0.2f);
        float b = Math.max(0.0f, teamColor.b - 0.2f);

        final int nrVertices = 18;
        colors = new float[4 * nrVertices];
        for (int i = 0; i < nrVertices; i++) {
            colors[4 * i] = r;
            colors[4 * i + 1] = g;
            colors[4 * i + 2] = b;
            colors[4 * i + 3] = 1;
        }
    }

    private void addWallSegment(Vec3 newEdge) {
        if (isWallBuilding) {
            addSegment(previousEdge, newEdge);
        } else {
            tmpSegmentStart = newEdge;
        }

        previousEdge = newEdge;
        isWallBuilding = true;
    }

    private void stopWallBuilding() {
        isWallBuilding = false;
        currentSegment = null;

        mergeTmpSegments(previousEdge);
        mergeMeshes();
    }

    private void addSegment(Vec3 start, Vec3 end) {
        wallPartCounter++;

        if (wallPartCounter % MERGED_WALL_PART_SIZE == 0) {
            mergeTmpSegments(end);
            if (currentSegment == null) {
                currentSegment = new WallSegment(tmpSegmentStart, end);
                segments.add(currentSegment);
            } else {
                currentSegment.addEdge(end);
            }
        } else {
            IMesh mesh = makeWallSegment(start.x, start.y, end.x, end.y);
            controller.getScene().add3DObject(mesh);
            tmpSegmentMeshes.add(mesh);
            tmpTexOff = texOff;
        }

        if (wallPartCounter % WALL_SEGMENT_SIZE == 0) {
            mergeMeshes();
        }
    }

    private void mergeTmpSegments(Vec3 tmpSegmentEnd) {
        IScene scene = controller.getScene();

        for (IMesh m : tmpSegmentMeshes) {
            scene.remove3DObject(m);
        }
        tmpSegmentMeshes.clear();

        texOff = tmpTexOff;

        IMesh mesh = makeWallSegment(tmpSegmentStart.x, tmpSegmentStart.y, tmpSegmentEnd.x, tmpSegmentEnd.y);
        controller.getScene().add3DObject(mesh);

        tmpTexOff = texOff;

        tmpSegmentStart = tmpSegmentEnd;
    }

    private void mergeMeshes() {
        wallPartCounter = 0;
        currentSegmentMeshes = MeshUtilities.mergeMeshes(currentSegmentMeshes);
        currentSegment = null;
    }

    private IMesh makeWallSegment(float x0, float y0, float x1, float y1) {
        float dx = (x1 - x0);
        float dy = (y1 - y0);
        float length = 0.5f * (float) Math.sqrt(dx * dx + dy * dy);

        float[] vertices = {
                // LEFT WALL
                x0, y0 + WALL_THICKNESS, 0,
                x0, y0 + WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 + WALL_THICKNESS, 0,

                x1, y1 + WALL_THICKNESS, 0,
                x0, y0 + WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 + WALL_THICKNESS, WALL_HEIGHT,

                // RIGHT WALL
                x1, y1 - WALL_THICKNESS, 0,
                x0, y0 - WALL_THICKNESS, WALL_HEIGHT,
                x0, y0 - WALL_THICKNESS, 0,

                x1, y1 - WALL_THICKNESS, WALL_HEIGHT,
                x0, y0 - WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 - WALL_THICKNESS, 0,

                // TOP
                x0, y0 + WALL_THICKNESS, WALL_HEIGHT,
                x0, y0 - WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 + WALL_THICKNESS, WALL_HEIGHT,

                x0, y0 - WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 - WALL_THICKNESS, WALL_HEIGHT,
                x1, y1 + WALL_THICKNESS, WALL_HEIGHT,
        };
        float[] texCoords = {
                // LEFT WALL
                0, texOff,
                1, texOff,
                0, texOff + length,

                0, texOff + length,
                1, texOff,
                1, texOff + length,

                // RIGHT WALL
                0, texOff + length,
                1, texOff,
                0, texOff,

                1, texOff + length,
                1, texOff,
                0, texOff + length,

                // TOP
                0, 0, //texOff, 0,
                0, 0, //texOff, WALL_THICKNESS,
                0, 0, //texOff + length, 0,

                0, 0, //texOff, WALL_THICKNESS,
                0, 0, //texOff + length, WALL_THICKNESS,
                0, 0, //texOff + length, 0,
        };

        texOff += length;

        // TRIANGLE_STRIP and TRIANGLE_FAN do'nt seem to be supported
        return new DefaultMesh(IMesh.Primitive.TRIANGLES, material, DefaultGeometry.createVCM(vertices, colors, texCoords), IMesh.Queue.DEPTH);
        //return new DefaultMesh(IMesh.Primitive.TRIANGLES, material, DefaultGeometry.createV(vertices), IMesh.Queue.DEPTH);
    }
}
