#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord;
in vec4 vertColor;

out vec4 fragColor;

void main() {
    float time = GameTime * 30000.0;
    vec4 outColor = texture(Sampler0, texCoord);

    if (outColor.a == 1.0 && outColor.b == 1.0) {
        vec2 pixel = floor(vec2(texCoord.x * 64.0, mod(texCoord.y * 256.0, 64.0)));
        float dist = length(pixel - vec2(33.0, 33.0));

        float spacing = 6.0;
        float ring = mod(dist - time, spacing);
        outColor = vec4(0.5 + 0.5 * step(spacing * 0.5, ring), 0.0, 0.0, 1.0);
    }

    outColor.a *= vertColor.a;
    fragColor = outColor;
}