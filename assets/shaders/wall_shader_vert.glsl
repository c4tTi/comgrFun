#version 330

#include <view_block.glsl>

struct VertexData {
	vec4 position;				// vertex position in eye space
	vec4 color;				    // vertex color in eye space
	vec2 texCoord;				// texture coordinate of color map
};

in vec4 vertexPosition;
in vec4 vertexColor;
in vec2 vertexTexCoord;

out VertexData vd;

void main() {
    vd.position = view.viewProjMatrix * vertexPosition;
    vd.color    = vertexColor;
    vd.texCoord = vertexTexCoord;

	gl_Position = view.viewProjMatrix * vertexPosition;
}
