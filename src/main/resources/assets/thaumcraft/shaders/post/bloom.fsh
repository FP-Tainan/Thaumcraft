#version 330

// O bloom2 do Thaumcraft 4.2.3.5: o brilho em volta do que é claro.

uniform sampler2D InSampler;

in vec2 texCoord;

out vec4 fragColor;

// a largura dos passos, o número de passos e a força do brilho do original
const float blurWidth = 0.002;
const float blurSteps = 0.1;
const float amount = 5.0;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float brightness = (color.r + color.g + color.b) / 3.0;
    vec4 sum = vec4(0.0);
    const float foo = 10.0 / (((blurSteps * 2.0) + 1.0) * 4.0);
    for (float i = -blurSteps; i < blurSteps; i++) {
        sum += texture(InSampler, texCoord + vec2(i, i) * blurWidth * 0.7);
        sum += texture(InSampler, texCoord + vec2(i, -i) * blurWidth * 0.7);
        sum += texture(InSampler, texCoord + vec2(0.0, i) * blurWidth);
        sum += texture(InSampler, texCoord + vec2(i, 0.0) * blurWidth);
    }
    sum *= foo;
    vec4 modifier = sum * sum * (0.015 - (brightness * 0.01));
    vec4 outputColor = modifier * amount + color;
    fragColor = outputColor * 0.9;
}
