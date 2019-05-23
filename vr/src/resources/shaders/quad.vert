#version 130

#pragma include("attrib.glsl")
#pragma include("transform.glsl")

out vec2 tc;

void main(void) {
    tc = g_Color.rg;
    gl_Position = g_ModelViewProjectionMatrix * vec4(g_Position, 1.0);
}