#version 330

// A previa do arquiteto do REHWandHandler da 4.2.3.5. As faces sao o vidro protegido com o glTexEnvi(GL_ADD) do
// original: a cor soma com a textura e so o alfa multiplica. As setas (ADD desligado) multiplicam, como de costume.

uniform sampler2D Sampler0;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 tex = texture(Sampler0, texCoord0);
#ifdef ADD
    vec4 color = vec4(min(vertexColor.rgb + tex.rgb, vec3(1.0)), vertexColor.a * tex.a);
#else
    vec4 color = vertexColor * tex;
#endif
    if (color.a < 0.003921569) {
        discard;
    }
    fragColor = color;
}
