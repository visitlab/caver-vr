#version 130

#pragma include("attrib.glsl")
#pragma include("transform.glsl")

uniform mat4 matrix;

out vec2 texCoord_;

void main()
{
    texCoord_ = g_TexCoord;
    gl_Position = g_ModelViewProjectionMatrix * vec4(g_Position, 1.0);
}
