#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord;
in vec4 vertColor;

out vec4 fragColor;

void main() {
    float progress = vertColor.x;

    float cols = 32.0;
    float rows = 18.0;

    vec2 cell = floor(texCoord * vec2(cols, rows));
    vec2 cellCenter = (cell + 0.5) / vec2(cols, rows);

    float rand = fract(sin(dot(cell, vec2(127.1, 311.7))) * 43758.5453);

    float distFromCenter = abs(cellCenter.x - 0.5) * 2.0;

    float threshold = mix(0.9 + rand * 0.3, rand * 0.3, distFromCenter);

    float timeNoise = fract(sin(dot(cell + floor(GameTime * 2.0), vec2(127.1, 311.7))) * 43758.5453) * 0.1;
    threshold += timeNoise;

    float steps = 8.0;
    float cellAlpha = ceil(clamp((progress - threshold + 0.5) / 0.5, 0.0, 0.8) * steps) / steps;

    vec4 tex = texture(Sampler0, texCoord);
    fragColor = tex * vec4(vec3(1.0), cellAlpha);
}