#version 330

uniform float redGain;

in vec4 vsColor;

out vec4 fragColor;

void main() {
	fragColor = vec4(vsColor.r * redGain, vsColor.g, vsColor.b, 1);
}
