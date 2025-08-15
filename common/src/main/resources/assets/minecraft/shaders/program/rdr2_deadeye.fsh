#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float Ending;

in vec2 texCoord;
out vec4 fragColor;

// Noise function
float noise(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

// Continuous smooth noise
float smoothRandom(float t) {
    float base = floor(t);
    float f = t - base;
    float n0 = noise(vec2(base, 0.0));
    float n1 = noise(vec2(base + 1.0, 0.0));
    float smoothF = f * f * (3.0 - 2.0 * f);
    return mix(n0, n1, smoothF);
}

void main() {
    vec2 uv = texCoord;
    vec2 center = uv - 0.5;
    float distFromCenter = length(center);

    vec3 orangeTint = vec3(1.5, 1.0, 0.3);

    // Strong smooth random pulse
    float t = Time * 0.25; // base time speed
    float r1 = smoothRandom(t);
    float r2 = smoothRandom(t * 0.5 + 37.0);
    float r3 = smoothRandom(t * 1.7 + 123.0);

    // Combine layers
    float pulseIntensity = (r1 * 0.5 + r2 * 0.35 + r3 * 0.15);

    pulseIntensity = pow(clamp(pulseIntensity, 0.0, 1.0), 0.8); // lower exponent = stronger variation

    // vignette
    float vignetteBase = smoothstep(0.35, 0.95, distFromCenter);

    float vignette = 1.0 - vignetteBase * mix(0.0, 1.0, pulseIntensity);

    // Chromatic aberration
    float aberrationIntensity = 0.0015 + distFromCenter * 0.004 + Ending * 0.008;
    vec2 redOffset   = center * aberrationIntensity;
    vec2 greenOffset = center * aberrationIntensity * 0.3;
    vec2 blueOffset  = center * aberrationIntensity * -0.7;

    float red   = texture(DiffuseSampler, uv + redOffset).r;
    float green = texture(DiffuseSampler, uv + greenOffset).g;
    float blue  = texture(DiffuseSampler, uv + blueOffset).b;

    vec3 aberratedColor = vec3(red, green, blue);

    // Edge blur based on Ending
    float blurRadius = Ending * 0.002;
    if (blurRadius > 0.0) {
        vec3 blurredColor = vec3(0.0);
        float blurWeight = 0.0;

        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                vec2 sampleUV = uv + vec2(float(x), float(y)) * blurRadius;
                if (sampleUV.x >= 0.0 && sampleUV.x <= 1.0 && sampleUV.y >= 0.0 && sampleUV.y <= 1.0) {
                    blurredColor += texture(DiffuseSampler, sampleUV).rgb;
                    blurWeight += 1.0;
                }
            }
        }

        if (blurWeight > 0.0) {
            blurredColor /= blurWeight;
        }

        float blurFactor = smoothstep(0.3, 0.8, distFromCenter) * Ending;
        aberratedColor = mix(aberratedColor, blurredColor, blurFactor);
    }

    // Orange tint
    float originalLuminance = dot(aberratedColor, vec3(0.299, 0.587, 0.114));
    float tintStrength = 0.8 + (1.0 - originalLuminance) * 0.3;
    vec3 tintedColor = mix(aberratedColor, aberratedColor * orangeTint, tintStrength * 0.7);

    // Apply vignette
    tintedColor *= vignette;

    // Contrast boost
    tintedColor = pow(tintedColor, vec3(1.05));

    float originalAlpha = texture(DiffuseSampler, uv).a;
    fragColor = vec4(tintedColor, originalAlpha);
}
