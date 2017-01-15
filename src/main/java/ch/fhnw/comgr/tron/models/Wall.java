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
import ch.fhnw.util.ArrayUtilities;
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

    private static final int MAX_SMALL_SEGMENTS = 15;
    private static final int MAX_COLLISION_SEGMENT_SIZE = MAX_SMALL_SEGMENTS * 20;
    private static final Vec3 TOP_VEC = new Vec3(0f, 0f, 1f);

    private final IController controller;
    private final Team team;
    private final Player playerA;
    private final Player playerB;

    private IMaterial sideMaterial;
    private IMaterial topMaterial;
    private float[] sideColors;
    private float[] topColors;

    /**
     * List of temporary meshes that represent the nearest wall part.
     * They will be removed and merged to a single wall after a time.
     *
     * If the wall is curved, it will be straightened a little
     */
    private List<IMesh> smallSegmentMeshes = new ArrayList<>();
    private float lSmallTexOff = 0;
    private float rSmallTexOff = 0;
    private WallCorner smallSegmentStart;
    private int smallSegmentCounter = 0;

    /**
     * List of the final meshes of the wall. They are sometimes merged for performance, but the form of the wall remain.
     */
    private List<IMesh> bigSegmentMeshes = new ArrayList<>();
    private float lBigTexOff = 0;
    private float rBigTexOff = 0;
    private WallCorner bigSegmentStart;

    /**
     * Used for collision detection
     */
    private List<WallSegment> collisionSegments = new ArrayList<>();
    private WallSegment currentCollisionSegment;


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
                addWallEdge(playerA.getPointBetween(playerB));
            } else {
                stopWallBuilding();
            }
        });
    }

    public boolean checkCollision(Player p) {
        for (WallSegment s : collisionSegments) {
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

        sideMaterial = new ColorMapMaterial(team.getTeamColor(), t, true);
        topMaterial  = new ColorMaterial(team.getTeamColor());
    }

    private void createColors() {
        RGBA teamColor = team.getTeamColor();
        float r = Math.max(0.0f, teamColor.r - 0.2f);
        float g = Math.max(0.0f, teamColor.g - 0.2f);
        float b = Math.max(0.0f, teamColor.b - 0.2f);

        sideColors = new float[4 * 12];
        for (int i = 0; i < sideColors.length/4; i++) {
            sideColors[4 * i + 0] = r;
            sideColors[4 * i + 1] = g;
            sideColors[4 * i + 2] = b;
            sideColors[4 * i + 3] = 1f;
        }

        topColors = new float[4 * 6];
        for (int i = 0; i < topColors.length/4; i++) {
            topColors[4 * i + 0] = 1;
            topColors[4 * i + 1] = 1;
            topColors[4 * i + 2] = 1;
            topColors[4 * i + 3] = 1f;
        }
    }

    private void addWallEdge(Vec3 newEdge) {
        WallCorner segmentEnd = new WallCorner(newEdge);

        if (smallSegmentStart != null) {
            addSmallSegment(segmentEnd);
        } else {
            bigSegmentStart = segmentEnd;
        }

        smallSegmentStart = segmentEnd;
    }

    private void stopWallBuilding() {
        if (smallSegmentStart != null) {
            mergeSmallSegments(smallSegmentStart, true);
            mergeMeshes();
        }

        smallSegmentStart = null;
        currentCollisionSegment = null;
    }

    private void addSmallSegment(WallCorner segmentEnd) {
        smallSegmentCounter++;

        if (smallSegmentCounter % MAX_SMALL_SEGMENTS == 0) {
            mergeSmallSegments(segmentEnd);

            // refresh current collision-entity
            if (currentCollisionSegment == null) {
                currentCollisionSegment = new WallSegment(bigSegmentStart.mid, segmentEnd.mid);
                collisionSegments.add(currentCollisionSegment);
            } else {
                currentCollisionSegment.addEdge(segmentEnd.mid);
            }
        } else {
            List<IMesh> mesh = createWallSegmentMeshes(smallSegmentStart, segmentEnd, false);
            controller.getScene().add3DObjects(mesh);
            smallSegmentMeshes.addAll(mesh);
        }

        if (smallSegmentCounter % MAX_COLLISION_SEGMENT_SIZE == 0) {
            mergeMeshes();
        }
    }

    private void mergeSmallSegments(WallCorner segmentEnd) {
        mergeSmallSegments(segmentEnd, false);
    }

    private void mergeSmallSegments(WallCorner segmentEnd, boolean isFinalSegment) {
        IScene scene = controller.getScene();

        scene.remove3DObjects(smallSegmentMeshes);
        smallSegmentMeshes.clear();

        lSmallTexOff = lBigTexOff;
        rSmallTexOff = rBigTexOff;

        List<IMesh> mesh = createWallSegmentMeshes(bigSegmentStart, segmentEnd, isFinalSegment);
        controller.getScene().add3DObjects(mesh);
        bigSegmentMeshes.addAll(mesh);

        lBigTexOff = lSmallTexOff;
        rBigTexOff = rSmallTexOff;

        bigSegmentStart = segmentEnd;
    }

    private void mergeMeshes() {
        IScene scene = controller.getScene();

        scene.remove3DObjects(bigSegmentMeshes);
        bigSegmentMeshes = MeshUtilities.mergeMeshes(bigSegmentMeshes);
        scene.add3DObjects(bigSegmentMeshes);

        smallSegmentCounter = 0;
        currentCollisionSegment = null;
    }

    private List<IMesh> createWallSegmentMeshes(WallCorner start, WallCorner end, boolean addClosingPart) {
        Vec3 perpendicular = end.mid.subtract(start.mid).cross(TOP_VEC).normalize();
        end.left = end.mid.add(perpendicular.scale(-WALL_THICKNESS));
        end.right = end.mid.add(perpendicular.scale(WALL_THICKNESS));

        if (start.left == null) {
            start.isStartCorner = true;
            start.left = start.mid.add(perpendicular.scale(-WALL_THICKNESS));
            start.right = start.mid.add(perpendicular.scale(WALL_THICKNESS));
        }
        boolean addStartingPart = start.isStartCorner;


        final float lx0 = start.left.x;
        final float ly0 = start.left.y;
        final float lx1 = end.left.x;
        final float ly1 = end.left.y;

        final float dlx = (lx1 - lx0);
        final float dly = (ly1 - ly0);
        final float llength = 0.5f * (float) Math.sqrt(dlx * dlx + dly * dly);

        final float rx0 = start.right.x;
        final float ry0 = start.right.y;
        final float rx1 = end.right.x;
        final float ry1 = end.right.y;

        final float drx = (rx1 - rx0);
        final float dry = (ry1 - ry0);
        final float rlength = 0.5f * (float) Math.sqrt(drx * drx + dry * dry);

        float[] sideVertices = {
                // LEFT WALL
                lx0, ly0, 0,
                lx0, ly0, WALL_HEIGHT,
                lx1, ly1, 0,

                lx1, ly1, 0,
                lx0, ly0, WALL_HEIGHT,
                lx1, ly1, WALL_HEIGHT,

                // RIGHT WALL
                rx1, ry1, 0,
                rx0, ry0, WALL_HEIGHT,
                rx0, ry0, 0,

                rx1, ry1, WALL_HEIGHT,
                rx0, ry0, WALL_HEIGHT,
                rx1, ry1, 0,
        };
        float[] topVertices = {
                // TOP
                lx0, ly0, WALL_HEIGHT,
                rx0, ry0, WALL_HEIGHT,
                lx1, ly1, WALL_HEIGHT,

                rx0, ry0, WALL_HEIGHT,
                rx1, ry1, WALL_HEIGHT,
                lx1, ly1, WALL_HEIGHT,
        };
        float[] texCoords = {
                // LEFT WALL
                0, lSmallTexOff,
                1, lSmallTexOff,
                0, lSmallTexOff + llength,

                0, lSmallTexOff + llength,
                1, lSmallTexOff,
                1, lSmallTexOff + llength,

                // RIGHT WALL
                0, rSmallTexOff + rlength,
                1, rSmallTexOff,
                0, rSmallTexOff,

                1, rSmallTexOff + rlength,
                1, rSmallTexOff,
                0, rSmallTexOff + rlength,
        };

        lSmallTexOff += llength;
        rSmallTexOff += rlength;

        ArrayList<IMesh> result = new ArrayList<>();
        result.add(new DefaultMesh(IMesh.Primitive.TRIANGLES, sideMaterial, DefaultGeometry.createVCM(sideVertices, sideColors, texCoords), IMesh.Queue.DEPTH));
        result.add(new DefaultMesh(IMesh.Primitive.TRIANGLES, topMaterial, DefaultGeometry.createVC(topVertices, topColors), IMesh.Queue.DEPTH));
        if (addStartingPart) {
            result.add(createClosingPartSegment(start, false));
        }
        if (addClosingPart) {
            result.add(createClosingPartSegment(end, true));
        }
        return result;
    }

    private IMesh createClosingPartSegment(WallCorner corner, boolean isEnd) {
        float[] vertices = {
                corner.right.x, corner.right.y, 0,
                corner.left .x, corner.left .y, WALL_HEIGHT,
                corner.left .x, corner.left .y, 0,

                corner.right.x, corner.right.y, WALL_HEIGHT,
                corner.left .x, corner.left .y, WALL_HEIGHT,
                corner.right.x, corner.right.y, 0,
        };
        if (isEnd) {
            int start = 0;
            int end = vertices.length - 3;

            while (start < end) {
                for (int i = 0; i < 3; i++) {
                    float tmp = vertices[end+i];
                    vertices[end+i] = vertices[start+i];
                    vertices[start+i] = tmp;
                }

                start += 3;
                end -= 3;
            }
        }


        return new DefaultMesh(IMesh.Primitive.TRIANGLES, topMaterial, DefaultGeometry.createVC(vertices, topColors), IMesh.Queue.DEPTH);
    }

    private class WallCorner
    {
        public final Vec3 mid;
        public Vec3 left;
        public Vec3 right;
        public boolean isStartCorner = false;

        public WallCorner(Vec3 mid) {
            this.mid = mid;
        }
    }
}
