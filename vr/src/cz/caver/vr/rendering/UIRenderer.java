package cz.caver.vr.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.awt.gl2es2.TextRenderer;
import cz.caver.renderer.RenderStateManager;
import cz.caver.renderer.buffer.ColoredGeomBuffer;
import cz.caver.renderer.buffer.GeomBuffer;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.renderer.glsl.GLSLUniformManager;
import cz.caver.renderer.state.BlendState;
import cz.caver.renderer.state.RenderState;
import cz.caver.renderer.util.GLUtils;
import cz.caver.vr.GUI.GUIPanel;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.GUI.elements.Label;
import cz.caver.vr.GUI.elements.UIElement;
import static cz.caver.vr.rendering.Renderer.LOG;
import cz.caver.vr.utils.Quad;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 * TODO
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class UIRenderer extends Renderer {
    public static boolean FORCE_RERENDER = true;
    
    private static GLSLProgram buttonProgram;
    
    private static GLSLProgram fillProgram;
    
    //private static final DefaultGeomBuffer RECTANGLE_BUFFER = new DefaultGeomBuffer(6, false);
    
    private static final ColoredGeomBuffer RECTANGLE_BUFFER = new ColoredGeomBuffer(6, false);
    
    private static final Map<Font, TextRenderer> TEXT_RENDERERS = new HashMap<>();
    
    public UIRenderer(GL2GL3 gl) {
        super(gl);
        
        buttonProgram = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/button.vert")
                .addFragmentShaderResource("/resources/shaders/button.frag")
                .getDebugProgram(gl, "UIElement");
        fillProgram = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/fill.vert")
                .addFragmentShaderResource("/resources/shaders/fill.frag")
                .getDebugProgram(gl, "Fill");
        
        RECTANGLE_BUFFER.generateGLBuffers(gl);
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[0], new Vector3f(0, 0, 0));
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[1], new Vector3f(1, 0, 0));
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[2], new Vector3f(1, 1, 0));
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[0], new Vector3f(0, 0, 0));
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[2], new Vector3f(1, 1, 0));
        RECTANGLE_BUFFER.addColoredVertex(Quad.QUAD_POSITIONS[3], new Vector3f(0, 1, 0));
    }
    
    @Override
    public void dispose() {
        buttonProgram.invalidate();
        fillProgram.invalidate();
        
        RECTANGLE_BUFFER.invalidateAndDeleteGLBuffers();
        
        for(TextRenderer r : TEXT_RENDERERS.values()) {
            r.dispose();
        }
    }
    
    @Override
    public void flush() {
        for(TextRenderer r : TEXT_RENDERERS.values()) {
            r.flush();
        }
        //TODO implement batched rendering also for buttons
    }
    
    public void renderPanel(GUIPanel panel, int[] viewport, boolean forceRerender) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to UI renderer");
            return;
        }
        
        RenderStateManager rsm = RenderStateManager.getInstance();
        RenderState rs = rsm.beginRender(gl);
        
        BlendState blend = rs.setBlendEnabled(gl, true);
        blend.setFunc(gl, GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        gl.glDisable(GL.GL_DEPTH_TEST);
        
        //Fill background
        if(forceRerender || FORCE_RERENDER){
            renderFill(panel.getBackgroundColor(), viewport);
        }
        
        //Draw UI elements
        for(UIElement element : panel.getUIElements()) {
            if(element instanceof Button) {
                renderButton((Button)element, panel.getBackgroundColor(), viewport, forceRerender || FORCE_RERENDER);
            } else if (element instanceof Label) {
                renderLabel((Label)element, panel.getBackgroundColor(), viewport, forceRerender || FORCE_RERENDER);
            }
        }
        
        gl.glEnable(GL.GL_DEPTH_TEST);
        rsm.endRender(gl);
        
        //RenderingUtils.debugSaveTexture(gl, "texture.png", renderBuffer.getColorAttachment(0).getAttachmentObject());
    }
    
    public void renderButton(Button button, Color background, int[] viewport, boolean forceRerender) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to UI renderer");
            return;
        }
        if(!button.hasChanged() && !forceRerender && !FORCE_RERENDER) return;
        if(button.getRenderTexture() != null) {
            //Render background
            Matrix4f mp = new Matrix4f(button.getTransform());
            //renderFill(mp, background, viewport);
            
            //Render button texture
            buttonProgram.use();
            Map<String, Integer> attributes = new HashMap<>();
            attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                    buttonProgram.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
            attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                    buttonProgram.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
            RECTANGLE_BUFFER.bind(attributes);
            
            buttonProgram.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(), GLUtils.asArray(mp));
            gl.glUniform1f(buttonProgram.getUniformLocation("alpha"), button.getAlpha());
            gl.glUniform1i(buttonProgram.getUniformLocation("tex"), 0);

            gl.glActiveTexture(GL.GL_TEXTURE0);
            button.getRenderTexture().bind(gl);
            //gl.glBindTexture(GL.GL_TEXTURE_2D, button.getRenderTexture().getTextureObject());

            gl.glViewport(0, 0, viewport[2], viewport[3]);
            
            gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);

            //RenderingUtils.debugSaveTexture(gl, "button.png", button.getRenderTexture().getTextureObject());

            RECTANGLE_BUFFER.unbind();

            gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, 0);
            button.setChanged(false);
        }
    }
    
    public void renderFill(Color fillColor, int[] viewport) {
        Matrix4f mp = new Matrix4f();
        mp.setIdentity();

        renderFill(mp, fillColor, viewport);
    }
    
    public void renderFill(Matrix4f transform, Color fillColor, int[] viewport) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to UI renderer");
            return;
        }
        
        fillProgram.use();

        fillProgram.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(), GLUtils.asArray(transform));
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                fillProgram.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        RECTANGLE_BUFFER.bind(attributes);
        
        gl.glUniform4f(fillProgram.getUniformLocation("fillColor"), fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), fillColor.getAlpha());

        gl.glViewport(0, 0, viewport[2], viewport[3]);

        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
        
        RECTANGLE_BUFFER.unbind();
    }

    public void renderLabel(Label label, Color background, int[] viewport, boolean forceRerender) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to UI renderer");
            return;
        }
        if(!label.hasChanged() && !forceRerender && !FORCE_RERENDER) return;
        
        //Find renderer or create one
        TextRenderer renderer;
        if(!TEXT_RENDERERS.containsKey(label.getFont())) {
            TEXT_RENDERERS.put(label.getFont(), new TextRenderer(label.getFont(), true, true));
            renderer = TEXT_RENDERERS.get(label.getFont());
            measureLabel(label, renderer);
        } else {
            renderer = TEXT_RENDERERS.get(label.getFont());
        }
        
        //Render label    
        renderLabel(label, background, renderer, viewport);
    }
    
    private void renderLabel(Label label, Color background, TextRenderer renderer, int[] viewport) {
        //Render background
        
        Matrix4f mp = new Matrix4f(label.getTransform());
        //renderFill(mp, background, viewport);
            
        renderer.beginRendering(viewport[2], viewport[3]);
        // optionally set the color
        renderer.setColor(label.getColor());
        try {
            renderer.draw(label.getText(), Math.round(label.getLeftTopCornerOffset().x), Math.round(viewport[3] - label.getLeftTopCornerOffset().y - label.getHeight()));
        } catch (Exception e) {}
        
        measureLabel(label, renderer);
        renderer.endRendering();
        label.setChanged(false);
    }
    
    private void measureLabel(Label label, TextRenderer renderer) {
        Rectangle2D rect = renderer.getBounds(label.getText());
        label.setScale((int)rect.getWidth() + 5, (int)rect.getHeight() + 5);
    }
}