package ch.fhnw.comgr.tron.render;

import ch.fhnw.comgr.tron.main.TronTeam;
import ch.fhnw.ether.image.IGPUImage;
import ch.fhnw.ether.image.IGPUTexture;
import ch.fhnw.ether.render.shader.IShader;
import ch.fhnw.ether.render.shader.base.AbstractShader;
import ch.fhnw.ether.render.variable.base.FloatUniform;
import ch.fhnw.ether.render.variable.base.Vec4FloatUniform;
import ch.fhnw.ether.render.variable.builtin.*;
import ch.fhnw.ether.scene.mesh.IMesh;
import ch.fhnw.ether.scene.mesh.geometry.IGeometry;
import ch.fhnw.ether.scene.mesh.material.AbstractMaterial;
import ch.fhnw.ether.scene.mesh.material.ICustomMaterial;
import ch.fhnw.util.math.IVec4;
import ch.fhnw.util.math.Vec4;

import java.util.function.Supplier;

/**
 * Created by Serquet on 15.01.2017.
 */
public class WallMaterial extends AbstractMaterial implements ICustomMaterial {
    private static class WallShader extends AbstractShader {
        public WallShader(Supplier<IGPUTexture> texture) {
            super(TronTeam.class, "tron.wall_shader", "/assets/shaders/wall_shader", IMesh.Primitive.TRIANGLES);
            addArray(new PositionArray());
            addArray(new ColorArray());
            addArray(new ColorMapArray());

            addUniform(new ViewUniformBlock());
            addUniform(new ColorMapUniform("lineMap", texture));
            //addUniform(new Vec4FloatUniform("tron.team_color", "teamColor"));
            addUniform(new FloatUniform("tron.white_gain", "whiteGain"));
        }
    }

    private final IShader shader;
    private IVec4 teamColor;
    private float whiteGain;

    public WallMaterial(IVec4 teamColor, IGPUImage texture) {
        super(provide(new MaterialAttribute<Float>("tron.white_gain")), require(IGeometry.POSITION_ARRAY, IGeometry.COLOR_ARRAY, IGeometry.COLOR_MAP_ARRAY));
        this.teamColor = teamColor;
        shader = new WallShader(() -> texture);
    }

    public IVec4 getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(IVec4 teamColor) {
        this.teamColor = teamColor;
        updateRequest();
    }

    public float getWhiteGain() {
        return whiteGain;
    }

    public void setWhiteGain(float whiteGain) {
        this.whiteGain = whiteGain;
        updateRequest();
    }

    @Override
    public IShader getShader() {
        return shader;
    }

    @Override
    public Object[] getData() {
        return data(whiteGain);
    }
}
