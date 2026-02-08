#version 150

uniform sampler2D DiffuseSampler;
uniform float TonicDuration;

in vec2 texCoord;
out vec4 fragColor;

float ease(float t) {
    return t * t * (3.0 - 2.0 * t);
}

void main() {
    vec2 uv = texCoord;
    vec4 baseSample = texture(DiffuseSampler, uv);
    vec3 baseColor = baseSample.rgb;

    float t = clamp(TonicDuration, 0.0, 1.0);
    float fade = ease(t) * (1.0 - t);

    vec2 center = uv - 0.5;
    float dist = length(center);
    float radial = smoothstep(1.0, 0.15, dist);
    float strength = pow(fade, 0.7) * radial;

    vec3 goldTint = vec3(1.35, 1.05, 0.45);
    vec3 washed = mix(baseColor, vec3(1.0), 0.35 * strength);
    vec3 tinted = mix(washed, washed * goldTint, 1.0 * strength);

    float vignette = smoothstep(0.95, 0.3, dist);
    vignette = mix(1.0, vignette, 1.35 * strength);

    vec3 finalColor = tinted * vignette;
    finalColor = mix(finalColor, finalColor * 1.15, strength);
    fragColor = vec4(finalColor, baseSample.a);
}
