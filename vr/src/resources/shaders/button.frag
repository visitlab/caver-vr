#version 130

/*struct UIElement {
    sampler2D texture;
    vec2 offset;
    vec2 size;
    float alpha;
    bvec2 stretch;
};*/

//uniform UIElement element;
//uniform vec2 overlaySize;
uniform sampler2D tex;
uniform float alpha;
in vec2 tex_coord;
out vec4 fragColor;

void main() {
    //fragColor = vec4(1, 0, 0, 1);
    /*vec2 texcoord = vec2(0, 0);
    ivec2 elementTexSize = textureSize(element.texture, 0);
    if (element.stretch.x) {
        texcoord.x = clamp(0, 1, (gl_FragCoord.x - element.offset.x) / element.size.x);
    } else {
        texcoord.x = (gl_FragCoord.x - element.offset.x) / elementTexSize.x;
    }

    if (element.stretch.y) {
        texcoord.y = clamp(0, 1, ((overlaySize.y - gl_FragCoord.y) - element.offset.y) / element.size.y);
    } else {
        texcoord.y = ((overlaySize.y - gl_FragCoord.y) - element.offset.y) / elementTexSize.y;
    }*/

    //if(texcoord.x == texcoord.y) {
    //    fragColor = vec4(0, 1, 0, 1);
    //} else {

        vec4 color = texture(tex, tex_coord);
        fragColor = vec4(color.rgb, alpha * color.a);
    //}
}

