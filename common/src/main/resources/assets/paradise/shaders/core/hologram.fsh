#version 150

uniform sampler2D Sampler0;
uniform float GameTime;

in vec2 texCoord;
in vec4 vertColor;

out vec4 fragColor;

void main() {
    float time = GameTime * 20000.0;
    vec2 TextureSize = vertColor.xy * 255.0;
    float frameCount = vertColor.z * 255.0;

    float pixelRow = floor(texCoord.y * TextureSize.y);
    float scanlineRow = floor(mod(time, TextureSize.y));
    float scanlineRow2 = floor(mod(time + 8.0, TextureSize.y * 3.333));

    float alpha = 0.45 + (mod(pixelRow, 2.0) + 1.0) * 0.25;

    // Shift scanline left / right
    vec2 uv = texCoord;
    if (pixelRow == scanlineRow) {
        uv.x = texCoord.x + (1.0 / (TextureSize.x * frameCount));
    } else if (pixelRow == scanlineRow - 1.0) {
        uv.x = texCoord.x - (1.0 / (TextureSize.x * frameCount));
    } else {
        // 2nd scanline
        if (pixelRow == scanlineRow2) {
            uv.x = texCoord.x + (1.0 / (TextureSize.x * frameCount));
        }
    }

    // Decrease alpha upwards from scanline
    float levels = 5.0;
    float dist1 = scanlineRow - pixelRow;
    float dist2 = scanlineRow2 - pixelRow;
    float dist = min(
        dist1 >= 0.0 ? dist1 : 100.0,
        dist2 >= 0.0 ? dist2 : 100.0
    );
    if (dist <= levels) {
        alpha += 0.4 * (1.0 - (dist / levels));
    }

    vec4 tex = texture(Sampler0, uv);
    fragColor = tex * vec4(vec3(1.0), alpha);
}