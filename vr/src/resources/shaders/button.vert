#version 130

#pragma include("attrib.glsl")
#pragma include("transform.glsl")

out vec2 tex_coord;

void main(void) {
    tex_coord = g_Color.rg;
    gl_Position = g_ModelViewProjectionMatrix * vec4(g_Position, 1.0);
}