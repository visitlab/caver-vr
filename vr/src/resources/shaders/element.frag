#version 130

uniform sampler2D elemTexture;

uniform float transparency;

in vec2 tex_coord;

out vec4 fragColor;

void main() { 
    vec4 color = texture(elemTexture, tex_coord);
    fragColor = vec4(color.rgb, transparency * color.a);
}

