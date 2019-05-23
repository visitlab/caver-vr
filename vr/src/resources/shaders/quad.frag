#version 130

uniform sampler2D glyphTex;

in vec2 tc;

out vec4 fragColor;

void main() { 
    fragColor = texture(glyphTex, tc);
}
