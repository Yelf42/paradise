#version 150

uniform sampler2D Sampler1;
uniform vec2 ScreenSize;

in vec4 vertexColor;
in vec2 texCoord0;
in vec4 clipPos;

out vec4 fragColor;

void main() {
    vec2 screenUV = (clipPos.xy / clipPos.w) * 0.5 + 0.5;

    int edgeMask = int(round(vertexColor.b * 255.0));
    bool edgeLeft   = (edgeMask & 1) != 0;
    bool edgeRight  = (edgeMask & 2) != 0;
    bool edgeBottom = (edgeMask & 4) != 0;
    bool edgeTop    = (edgeMask & 8) != 0;

    float quadWidthPx  = max(vertexColor.r * ScreenSize.x, 1.0);
    float quadHeightPx = max(vertexColor.g * ScreenSize.y, 1.0);
    float edgeThicknessPx = 8.0;
    float maxThicknessX = 0.08 * quadWidthPx;
    float maxThicknessY = 0.08 * quadHeightPx;
    float thicknessPx = min(edgeThicknessPx, min(maxThicknessX, maxThicknessY));
    float thickU = thicknessPx / quadWidthPx;
    float thickV = thicknessPx / quadHeightPx;

    float distLeft   = edgeLeft   ? (texCoord0.x / thickU)         : 1.0;
    float distRight  = edgeRight  ? ((1.0 - texCoord0.x) / thickU) : 1.0;
    float distBottom = edgeTop    ? (texCoord0.y / thickV)         : 1.0;
    float distTop    = edgeBottom ? ((1.0 - texCoord0.y) / thickV) : 1.0;

    float edgeDist = min(min(distLeft, distRight),
                         min(distBottom, distTop));
    edgeDist = clamp(edgeDist, 0.0, 1.0);

    float pixelSize = max(1.0, floor(vertexColor.a * 32.0));
    vec2 snapped = floor(screenUV * (ScreenSize / pixelSize)) / (ScreenSize / pixelSize);

    if (edgeDist < 1.0) {
        fragColor = vec4(1.0, 0.0, 0.0, 1.0);
    } else {
        fragColor = texture(Sampler1, snapped);
    }
}