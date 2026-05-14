#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;
in ivec2 UV2;
in vec3 Normal;

out vec4 vertexColor;
out vec2 texCoord0;
out vec4 clipPos;

void main() {
    gl_Position = vec4(Position.xyz, 1.0);
    clipPos = gl_Position;
    vertexColor = Color;
    texCoord0 = UV0;
}