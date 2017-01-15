package ch.fhnw.comgr.tron.ui;

import java.util.ArrayList;
import java.util.List;

import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.MeshUtilities;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.color.RGBA;
import ch.fhnw.util.math.Mat4;
import ch.fhnw.util.math.Vec3;

public class Skybox {

	public static IMaterial createMaterial(String myPath)
	{
		try {
			IGPUImage t = IGPUImage.read(Skybox.class.getResource(myPath));
			IMaterial m = new ColorMapMaterial(RGBA.WHITE, t, false);

			return m;
			
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("cant load image");
			System.exit(1);
		}
		return null;	
		
	}
	
	
	public static List<IMesh> createSkybox(float lengthMap){
		IMesh topMesh, bottomMesh, frontMesh, leftMesh, rightMesh, backMesh;
		
		IMaterial topMat, bottomMat, frontMat, leftMat, rightMat, backMat;
		
		topMat = createMaterial("/textures/darkcity_up.png");
		bottomMat = createMaterial("/textures/darkcity_dn.png");
		frontMat = createMaterial("/textures/darkcity_ft.png");
		leftMat = createMaterial("/textures/darkcity_lf.png");
		rightMat = createMaterial("/textures/darkcity_rt.png");
		backMat = createMaterial("/textures/darkcity_bk.png");
		
		topMesh = MeshUtilities.createGroundPlane(topMat, lengthMap);
		bottomMesh = MeshUtilities.createGroundPlane(bottomMat, lengthMap);
		frontMesh = MeshUtilities.createGroundPlane(frontMat, lengthMap);
		leftMesh = MeshUtilities.createGroundPlane(leftMat, lengthMap);
		rightMesh = MeshUtilities.createGroundPlane(rightMat, lengthMap);
		backMesh = MeshUtilities.createGroundPlane(backMat, lengthMap);
		
		List<IMesh> mySkyboxMeshes = new ArrayList<IMesh>();
		
		bottomMesh.setPosition(new Vec3(0f,0f,-lengthMap));
		frontMesh.setTransform(Mat4.rotate(90, Vec3.X));
		frontMesh.setPosition(new Vec3(0f, lengthMap, 0f));

		leftMesh.setTransform(Mat4.multiply(Mat4.rotate(90, Vec3.X), Mat4.rotate(90, Vec3.Y)));
		leftMesh.setPosition(new Vec3(-lengthMap, 0f, 0f));
		
		rightMesh.setTransform(Mat4.multiply(Mat4.rotate(90, Vec3.X), Mat4.rotate(-90, Vec3.Y)));
		rightMesh.setPosition(new Vec3(lengthMap, 0f, 0f));
		
		backMesh.setTransform(Mat4.multiply(Mat4.rotate(90, Vec3.X), Mat4.rotate(180, Vec3.Y)));
		backMesh.setPosition(new Vec3(0f, -lengthMap, 0f));
		
		topMesh.setTransform(Mat4.rotate(180, Vec3.X));
		topMesh.setPosition(new Vec3(0f,0f, lengthMap));
		
		mySkyboxMeshes.add(topMesh);
		mySkyboxMeshes.add(bottomMesh);
		mySkyboxMeshes.add(frontMesh);
		mySkyboxMeshes.add(leftMesh);
		mySkyboxMeshes.add(rightMesh);
		mySkyboxMeshes.add(backMesh);
		
		return mySkyboxMeshes;		
	} 
	
	
	
}
