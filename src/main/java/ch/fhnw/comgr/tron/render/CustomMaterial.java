package ch.fhnw.comgr.tron.render;

import ch.fhnw.comgr.tron.main.TronTeam;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.builtin.ColorArray;
import ch.fhnw.ether.render.variable.builtin.PositionArray;
import ch.fhnw.ether.render.variable.builtin.ViewUniformBlock;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.AbstractMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;

public class CustomMaterial extends AbstractMaterial implements ICustomMaterial {
        private static class Shader extends AbstractShader {
            public Shader() {
                super(TronTeam.class, "tron.custom_shader", "/assets/shaders/custom_shader", IMesh.Primitive.TRIANGLES);
                addArray(new PositionArray());
                addArray(new ColorArray());

                addUniform(new ViewUniformBlock());
                addUniform(new FloatUniform("custom.red_gain", "redGain"));
            }
        }

        private final IShader shader = new Shader();
        private float redGain;

        public CustomMaterial(float redGain) {
            super(provide(new MaterialAttribute<Float>("custom.red_gain")), require(IGeometry.POSITION_ARRAY));
            this.redGain = redGain;
        }

        public float getRedGain() {
            return redGain;
        }

        public void setRedGain(float redGain) {
            this.redGain = redGain;
            updateRequest();
        }

        @Override
        public IShader getShader() {
            return shader;
        }

        @Override
        public Object[] getData() {
            return data(redGain);
        }
    }

