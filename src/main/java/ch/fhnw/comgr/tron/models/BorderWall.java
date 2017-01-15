package ch.fhnw.comgr.tron.models;

import java.util.ArrayList;

import ch.fhnw.comgr.tron.ui.Grid;
import ch.fhnw.ether.controller.IController;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.scene.mesh.DefaultMesh;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.IMesh.Flag;
import ch.fhnw.ether.scene.mesh.geometry.DefaultGeometry;
import ch.fhnw.ether.scene.mesh.material.ColorMapMaterial;
import ch.fhnw.ether.scene.mesh.material.IMaterial;
import ch.fhnw.util.color.RGBA;

public class BorderWall {
	
	private static final float WALL_HEIGHT = 0.01f;
	public static final float TEXTURE_OFFSET = 2f;
	private IMesh[] wall;
	private float[][] wallVertices;
	private IGPUImage texture;
	private IMaterial material;
	
	public BorderWall(IController controller, float mapSize) {
		try {
			wall = new IMesh[4];
			wallVertices = new float[4][6*4];
			texture = IGPUImage.read(Grid.class.getResource("/textures/tron_wall.jpg"));
			material = new ColorMapMaterial(RGBA.WHITE, texture, false);
			
			
			float[] colors = new float[6*4];
			for(int i = 0; i < colors.length; i++) {
				colors[i] = 1f;
			}
			//float[] texCoords = new float[6*2];
//			for(int i = 0; i < texCoords.length; i++) {
//				texCoords[i] = 1f;
//			}

			int texOffset = (int) mapSize/25;

			float[] texCoordsOne = {
					0, 0,
					0, texOffset,
					2*texOffset, texOffset,
					0, 0,
					2*texOffset, texOffset,
					2*texOffset, 0,
			};
			float[] texCoordsTwo = {
					2*texOffset, texOffset,
					0, texOffset,
					0, 0,

					2*texOffset, 0,
					2*texOffset, texOffset,
					0, 0,
			};
			
			
			float[] v0 = {
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f
			};
			float[] v1 = {
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, 0f,
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, 0f,
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, 0f,
			};
			float[] v2 = {
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, 0f
			};
			float[] v3 = {
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, 0f,
					-mapSize - TEXTURE_OFFSET, mapSize - TEXTURE_OFFSET, WALL_HEIGHT,
					-mapSize - TEXTURE_OFFSET, -mapSize - TEXTURE_OFFSET, 0f,
			};
			wallVertices[0] = v0;
			wallVertices[1] = v1;
			wallVertices[2] = v2;
			wallVertices[3] = v3;
			
			
			for(int i = 0; i < wallVertices.length; i++) {
				wall[i] = new DefaultMesh(IMesh.Primitive.TRIANGLES, material, DefaultGeometry.createVCM(wallVertices[i], colors,(i%2 == 0) ? texCoordsOne : texCoordsTwo), IMesh.Queue.DEPTH);
				controller.getScene().add3DObjects(wall[i]);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
