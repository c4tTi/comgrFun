package ch.fhnw.comgr.tron.ui;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.LineMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Vec3;

public class Grid {
	
	public static IMesh makeGrid() {
		List<Vec3> lines = new ArrayList<>();

		int gridNumLines = 1200;
		float gridSpacing = 1f;

		// add axis lines
		float e = 0.5f * gridSpacing * (gridNumLines + 1);
		MeshUtilities.addLine(lines, -e, 0, e, 0);
		MeshUtilities.addLine(lines, 0, -e, 0, e);

		// add grid lines
		int n = gridNumLines / 2;
		for (int i = 1; i <= n; ++i) {
			MeshUtilities.addLine(lines, i * gridSpacing, -e, i * gridSpacing, e);
			MeshUtilities.addLine(lines, -i * gridSpacing, -e, -i * gridSpacing, e);
			MeshUtilities.addLine(lines, -e, i * gridSpacing, e, i * gridSpacing);
			MeshUtilities.addLine(lines, -e, -i * gridSpacing, e, -i * gridSpacing);
		}

		return new DefaultMesh(Primitive.LINES, new LineMaterial(RGBA.GRAY), DefaultGeometry.createV(Vec3.toArray(lines)), Queue.TRANSPARENCY);
	}

}
