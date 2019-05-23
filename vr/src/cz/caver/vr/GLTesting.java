package cz.caver.vr;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import cz.caver.renderer.pipeline.Framebuffer;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.GUI.buttons.impl.FindStructureButton;
import cz.caver.vr.GUI.overlays.impl.Dashboard;
import cz.caver.vr.rendering.UIRenderer;
import javax.vecmath.Vector3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class GLTesting {
    private static UIRenderer renderer;
    
    private final Framebuffer renderFbo;
    
    private final Button button;
    
    public GLTesting(VirtualReality vr) {
        GL2GL3 gl = vr.getGL();
        renderer = (UIRenderer) vr.getRenderer(UIRenderer.class);
        renderFbo = Framebuffer.newColorFramebuffer(gl, 1000, 1000, Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8);
        gl.glBindTexture(GL.GL_TEXTURE_2D, renderFbo.getColorAttachment(0).getAttachmentObject());
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);
        
        button = new FindStructureButton(new Dashboard(vr));
        button.setLeftTopOffset(new Vector3f(100, 100, 0));
        button.setWidth(800);
        button.setHeight(100);
    }

    public void render() {

        int[] viewport = {0, 0, 1000, 1000};
        
        //Button
        //renderer.render(renderFbo, button, viewport);
        
        
        /*try {
            renderFbo.bindRead(gl);
            Utils.saveImage(gl, 1000, 1000);
        } catch (IOException e){}*/
    }
}
