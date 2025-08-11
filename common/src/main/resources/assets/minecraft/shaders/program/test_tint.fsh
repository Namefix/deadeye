#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform vec3 TintColor;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec4 originalColor = texture(DiffuseSampler, texCoord);

    // Create a pulsing effect based on time
    float pulse = sin(Time * 0.05) * 0.5 + 0.5;

    // Apply tint with pulsing intensity
    vec3 tintedColor = mix(originalColor.rgb, TintColor, Intensity * pulse);

    // Add a subtle vignette effect
    vec2 center = texCoord - 0.5;
    float vignette = 1.0 - dot(center, center) * 0.8;
    tintedColor *= vignette;

    fragColor = vec4(tintedColor, originalColor.a);
}
