#version 130

uniform sampler2D diffuse;

in vec2 texCoord_;

out vec4 fragColor;

void main()
{
   fragColor = texture(diffuse, texCoord_);
}
