#version 130

#pragma include("attrib.glsl")
#pragma include("transform.glsl")

out vec3 color;

void main(void) {
    color = g_Color;
    gl_Position = g_ModelViewProjectionMatrix * vec4(g_Position, 1.0);
}