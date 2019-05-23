/*

 */

package cz.caver.vr.rendering;

import com.caversoft.log.Log;
import com.jogamp.opengl.GL2GL3;
import cz.caver.renderer.pipeline.Framebuffer;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public abstract class Renderer implements IRenderer {
    protected static final Log LOG = new Log(Renderer.class);
    
    protected static GL2GL3 gl;
    
    protected Framebuffer renderBuffer = null;
    
    public Renderer(GL2GL3 gl) {
        this.gl = gl;
    }
    
    @Override
    public void bindRenderBuffer(Framebuffer buffer) {
        if(renderBuffer != null) renderBuffer.unbind(gl);
        buffer.bindDraw(gl);
        renderBuffer = buffer;
    }
    
    @Override
    public boolean unbindRenderBuffer() {
        if(renderBuffer != null) {
            renderBuffer.unbind(gl);
            renderBuffer = null;
            return true;
        }
        return false;
    }
}
