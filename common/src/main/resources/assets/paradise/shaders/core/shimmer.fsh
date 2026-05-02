#version 150

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 vertColor;

out vec4 fragColor;

void main() {

    vec4 outColor = texture(Sampler0, texCoord);
    outColor.a *= vertColor.a;
    fragColor = outColor;
}