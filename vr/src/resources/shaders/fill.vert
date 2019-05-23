#version 130

#pragma include("attrib.glsl")
#pragma include("transform.glsl")

void main(void) {
    gl_Position = g_ModelViewProjectionMatrix * vec4(g_Position, 1.0);
}