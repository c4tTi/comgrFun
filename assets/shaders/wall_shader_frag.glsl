#version 330

#define M_PI 3.1415926535897932384626433832795

uniform sampler2D lineMap;
uniform vec4 teamColor;
uniform float whiteGain;

struct VertexData {
	vec4 position;				// vertex position in eye space
	vec4 color;				    // vertex color in eye space
	vec2 texCoord;				// texture coordinate of color map
};

in VertexData vd;

out vec4 fragColor;

vec4 test(sampler2D image, vec2 uv, vec4 color) {
    float posGain = sin(uv.y) + 1;
    float absGain = abs(posGain - whiteGain);

    color += texture(image, uv) * absGain * absGain;
    //color += texture(image, uv) * (whiteGain * uv.y);
    //color += texture(image, uv) * 1/(1 +  abs(mod(uv.y, 10) - whiteGain - 5));
    //color += texture(image, uv) * teamColor;
    return color;
}

void main() {
	//fragColor = texture(lineMap, vsTexCoord);
	fragColor = test(lineMap, vd.texCoord, vd.color);
}
