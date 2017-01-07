#version 330

#include <view_block.glsl>

in vec4 vertexPosition;
in vec4 vertexColor;

out vec4 vsColor;

void main() {
	vsColor = vertexColor;
	gl_Position = view.viewProjMatrix * vertexPosition;
}
