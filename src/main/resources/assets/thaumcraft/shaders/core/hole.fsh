#version 330

// O buraco do Buraco Portatil: o TileHoleRenderer da 4.2.3.5, que e o ceu de estrelas do portal do End do
// jogo daquela epoca com as texturas do Thaumcraft. A primeira camada e o tunel roxo a um decimo da luz; as
// quinze seguintes sao o campo de particulas, somando luz, cada uma com a cor que o original sorteia com a
// semente 31100 (calculadas por scratchpad/cores-buraco.js).

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:matrix.glsl>
#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

in vec4 texProj0;
in float sphericalVertexDistance;
in float cylindricalVertexDistance;

const vec3[] COLORS = vec3[](
    vec3(0.100000, 0.100000, 0.100000),
    vec3(0.006317, 0.050960, 0.047539),
    vec3(0.014739, 0.054234, 0.053507),
    vec3(0.024945, 0.058866, 0.061520),
    vec3(0.034947, 0.063375, 0.052333),
    vec3(0.034537, 0.047068, 0.066975),
    vec3(0.046264, 0.061088, 0.090753),
    vec3(0.053619, 0.084766, 0.050085),
    vec3(0.058973, 0.072858, 0.108439),
    vec3(0.054968, 0.061981, 0.105316),
    vec3(0.076295, 0.079016, 0.084904),
    vec3(0.040837, 0.141944, 0.137545),
    vec3(0.118059, 0.085740, 0.128817),
    vec3(0.029551, 0.197086, 0.201231),
    vec3(0.136450, 0.260007, 0.201378),
    vec3(0.060716, 0.236115, 0.496118)
);

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 hole_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (GameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );
    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));
    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);
    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

out vec4 fragColor;

void main() {
    vec3 color = textureProj(Sampler0, texProj0).rgb * COLORS[0];
    for (int i = 1; i < 16; i++) {
        color += textureProj(Sampler1, texProj0 * hole_layer(float(i))).rgb * COLORS[i];
    }
    fragColor = apply_fog(vec4(color, 1.0), sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
