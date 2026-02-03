#version 150

uniform sampler2D DiffuseSampler;
uniform float Time;
uniform float Ending;
uniform float Fade;

in vec2 texCoord;
out vec4 fragColor;

float noise(vec2 uv) {
    return fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
}

float smoothRandom(float t) {
    float base = floor(t);
    float f = t - base;
    float n0 = noise(vec2(base, 0.0));
    float n1 = noise(vec2(base + 1.0, 0.0));
    float smoothF = f * f * (3.0 - 2.0 * f);
    return mix(n0, n1, smoothF);
}

float rand1(float seed) {
    return fract(sin(seed * 91.3458) * 43758.5453);
}

float randomTarget(float index) {
    return mix(0.45, 0.95, rand1(index * 13.57 + 7.0));
}

float randomDuration(float index) {
    return mix(0.33, 1.0, rand1(index * 5.71 + 3.2));
}

float vignetteEnvelope(float t) {
    const float AVERAGE_WINDOW = 2.0; // seconds
    float controlTime = (t * 0.55) / AVERAGE_WINDOW;
    float segment = floor(controlTime);
    float local = fract(controlTime);

    float duration = randomDuration(segment);
    float progress = clamp(local / duration, 0.0, 1.0);
    float eased = progress * progress * (3.0 - 2.0 * progress);

    float startValue = randomTarget(segment);
    float endValue = randomTarget(segment + 1.0);
    float base = mix(startValue, endValue, eased);

    float linger = smoothstep(0.95, 1.0, local) * (rand1(segment * 2.17 + 11.0) - 0.5) * 0.08;
    float micro = (smoothRandom(t * 0.9 + 31.0) - 0.5) * 0.03;

    return clamp(base + linger + micro, 0.42, 0.96);
}

vec3 applyEdgeBlur(vec2 uv, vec2 center, float ending) {
    float dist = length(center);
    float edgeMask = pow(smoothstep(0.28, 0.98, dist), 1.35) * ending;
    if(edgeMask <= 0.001) {
        return texture(DiffuseSampler, uv).rgb;
    }

    float radius = mix(0.002, 0.0095, clamp(edgeMask * 1.35, 0.0, 1.0));
    vec3 color = vec3(0.0);
    float weightSum = 0.0;

    for(int x = -2; x <= 2; x++) {
        for(int y = -2; y <= 2; y++) {
            vec2 offset = vec2(float(x), float(y));
            float weight = exp(-dot(offset, offset) * 0.22);
            vec2 sampleUV = clamp(uv + offset * radius, 0.0, 1.0);
            color += texture(DiffuseSampler, sampleUV).rgb * weight;
            weightSum += weight;
        }
    }

    return color / max(weightSum, 0.0001);
}

void main() {
    vec2 uv = texCoord;
    vec2 center = uv - 0.5;
    float distFromCenter = length(center);

    vec4 baseSample = texture(DiffuseSampler, uv);
    vec3 baseColor = baseSample.rgb;

    float envelope = vignetteEnvelope(Time);
    float vignetteBase = smoothstep(0.22 - envelope * 0.04, 0.9 - envelope * 0.08, distFromCenter);
    float vignetteStrength = mix(0.45, 0.82, envelope);
    float vignette = 1.0 - vignetteBase * vignetteStrength;

    float aberrationIntensity = 0.0015 + distFromCenter * 0.0045 + Ending * 0.0075;
    vec3 aberratedColor = vec3(
        texture(DiffuseSampler, uv + center * aberrationIntensity).r,
        texture(DiffuseSampler, uv + center * aberrationIntensity * 0.3).g,
        texture(DiffuseSampler, uv - center * aberrationIntensity * 0.55).b
    );

    vec3 blurredColor = applyEdgeBlur(uv, center, Ending);
    float blurMix = min(1.0, smoothstep(0.32, 0.9, distFromCenter) * Ending * 1.45);
    vec3 focusColor = mix(aberratedColor, blurredColor, blurMix);

    vec3 orangeTint = vec3(1.35, 1.0, 0.4);
    float luminance = dot(focusColor, vec3(0.299, 0.587, 0.114));
    vec3 tintedColor = mix(focusColor, focusColor * orangeTint, 0.55 + (1.0 - luminance) * 0.25);
    vec3 vignettedColor = tintedColor * mix(1.0, vignette, 0.9);
    vec3 gradedColor = pow(vignettedColor, vec3(1.05));
    gradedColor *= 1.4;

    vec3 finalColor = mix(baseColor, gradedColor, clamp(Fade, 0.0, 1.0));
    fragColor = vec4(finalColor, baseSample.a);
}
