package ch.fhnw.comgr.tron.ui;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.IMesh.Primitive;
import ch.fhnw.ether.scene.mesh.IMesh.Queue;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
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

	
	public static IMesh createSquareMapStandardMat(float lengthMap, int materialCount){
		
		try {
			IGPUImage t = IGPUImage.read(Grid.class.getResource("/textures/tron_floor.png"));
			//IMaterial m = new ColorMapMaterial();
			IMaterial m = new ColorMapMaterial(RGBA.WHITE, t, false);

			return createSquareMap(lengthMap, m, materialCount);
			
			//scene.add3DObjects(createSquareMap(100f, m, 100));
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("cant load image");
			System.exit(1);
		}
		return null;		
	} 
	
	public static IMesh createSquareMap(float lengthMap, IMaterial material, int materialCount){
		//how long is one sub Mesh?
		float materialLength = lengthMap/ (float) (materialCount);
		System.out.println(materialLength);
		IMesh meshToInstanciate = MeshUtilities.createGroundPlane(material, materialLength);
		List<IMesh> myMeshes = new ArrayList<IMesh>();
		for(int x = 0; x < materialCount; x ++){
			for (int y = 0; y < materialCount; y++)
			{
				IMesh myMesh = meshToInstanciate.createInstance();
				myMesh.setPosition(new Vec3(materialLength * x * 2 , materialLength * y * 2 ,0f));
				myMeshes.add(myMesh);
			}
		}
		IMesh myReturnMesh = MeshUtilities.mergeMeshes(myMeshes).get(0);
		myReturnMesh.setPosition(new Vec3(-lengthMap,-lengthMap , 0));
		
		return myReturnMesh;
	}
	
}
