/*

 */
package cz.caver.vr.rendering;

import cz.caver.renderer.pipeline.Framebuffer;

/**
 *
 * @author Avith
 */
public interface IRenderer {
    
    /**
     * Binds a renderbuffer
     * @param buffer 
     */
    public void bindRenderBuffer(Framebuffer buffer);
    
    /**
     * Unbinds a renderbuffer
     * @param buffer 
     */
    public boolean unbindRenderBuffer();    
    
    /**
     * Disposes all held resources
     */
    public void dispose();
    
    /**
     * Flushes and does all stacked rendering jobs. Currently implemented only for UIRenderer
     */
    public void flush();
}
