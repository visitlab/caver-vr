package cz.caver.vr.rendering;

import com.caversoft.color.Color;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.Texture;
import cz.caver.renderer.RenderStateManager;
import cz.caver.renderer.buffer.ColoredGeomBuffer;
import cz.caver.renderer.buffer.GeomBuffer;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.renderer.glsl.GLSLUniformManager;
import cz.caver.renderer.pipeline.Framebuffer;
import cz.caver.renderer.state.RenderState;
import cz.caver.renderer.util.GLUtils;
import cz.caver.vr.utils.Quad;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Matrix4f;

/**
 *
 * @author xkleteck
 */
public class QuadRenderer extends Renderer {
    
    // program used to renderTextureQuad both eyes
    private static GLSLProgram program;
    
    private static final ColoredGeomBuffer quadBuffer = new ColoredGeomBuffer(6, false);
    private static final ColoredGeomBuffer leftGeomBuffer = new ColoredGeomBuffer(6, false);
    private static final ColoredGeomBuffer rightGeomBuffer = new ColoredGeomBuffer(6, false);
    
    public QuadRenderer(GL2GL3 gl) {
        super(gl);
        
        quadBuffer.invalidateGLBuffers();
        quadBuffer.generateGLBuffers(gl);
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[0], new Color(0f, 0f, 0f));
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[1], new Color(1f, 0f, 0f));
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[3], new Color(0f, 1f, 0f));
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[3], new Color(0f, 1f, 0f));
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[1], new Color(1f, 0f, 0f));
        quadBuffer.addColoredVertex(Quad.QUAD_POSITIONS[2], new Color(1f, 1f, 0f));
        
        leftGeomBuffer.invalidateGLBuffers();
        leftGeomBuffer.generateGLBuffers(gl);
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[0], new Color(0f, 0f, 0f));
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[4], new Color(1f, 0f, 0f));
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[5], new Color(1f, 1f, 0f));
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[0], new Color(0f, 0f, 0f));
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[5], new Color(1f, 1f, 0f));
        leftGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[3], new Color(0f, 1f, 0f));
        
        rightGeomBuffer.invalidateGLBuffers();
        rightGeomBuffer.generateGLBuffers(gl);
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[4], new Color(0f, 0f, 0f));
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[1], new Color(1f, 0f, 0f));
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[2], new Color(1f, 1f, 0f));
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[4], new Color(0f, 0f, 0f));
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[2], new Color(1f, 1f, 0f));
        rightGeomBuffer.addColoredVertex(Quad.EYE_QUAD_POSITIONS[5], new Color(0f, 1f, 0f));
        
        if (program != null) {
            program.invalidate();
        }
        program = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/quad.vert")
                .addFragmentShaderResource("/resources/shaders/quad.frag")
                .getDebugProgram(gl, "Eyes");
    }
    
    @Override
    public void dispose() {
        program.invalidate();
        quadBuffer.invalidateAndDeleteGLBuffers();
        leftGeomBuffer.invalidateAndDeleteGLBuffers();
        rightGeomBuffer.invalidateAndDeleteGLBuffers();
    }
    
    @Override
    public void flush() {}
    
    /**
     * Draws quad into binded framebuffer
     * @param gl 
     */
    public void renderTextureQuad(int textureTarget, Matrix4f quadTransform) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to quad renderer");
            return;
        }
        program.use();
        
        //RenderStateManager rsm = RenderStateManager.getInstance();
        //RenderState rs = rsm.beginRender(gl);
        
        //rs.setDepthTestEnabled(gl, false);
        
        // bind geomBuffer attributes
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                program.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                program.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        quadBuffer.bind(attributes);
        
        //Set model transform
        GLSLUniformManager.getInstance().setModelMatrix(quadTransform);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        program.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
                GLUtils.asArray(mvp));
        
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, textureTarget);
        
        // draw quad
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
        
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, 0);
        
        quadBuffer.unbind();
        
        //rsm.endRender(gl);
    }
    
    /**
     * Draws quad into binded framebuffer
     * @param gl 
     */
    public void renderTextureQuad(Texture texture, Matrix4f quadTransform, int[] viewport) {
        gl.glViewport(0, 0, viewport[2], viewport[3]);
        renderTextureQuad(texture.getTarget(), quadTransform);
    }
    
    /**
     * Draws eye quads into binded framebuffer
     * @param gl 
     */
    public void renderEyeQuads(Framebuffer leftEye, Framebuffer rightEye, int[] viewport) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to renderer");
            return;
        }
        program.use();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        gl.glViewport(0, 0, viewport[2], viewport[3]);
        
        RenderStateManager rsm = RenderStateManager.getInstance();
        RenderState rs = rsm.beginRender(gl);
        
        rs.setDepthTestEnabled(gl, false);        
        
        // bind geomBuffer attributes
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                program.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                program.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        leftGeomBuffer.bind(attributes);
        
        //Set identity mvp matrix
        Matrix4f transform = new Matrix4f();
        transform.setIdentity();
        program.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
                GLUtils.asArray(transform));
        
        int textureHandle = leftEye.getColorAttachment(0).getAttachmentObject();
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, textureHandle);
        
        // draw quad
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
        
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, 0);
        
        leftGeomBuffer.unbind();
        
        // draw right eye        
        rightGeomBuffer.bind(attributes);
        
        textureHandle = rightEye.getColorAttachment(0).getAttachmentObject();
        gl.glActiveTexture(GL.GL_TEXTURE0);
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, textureHandle);
        
        // draw quad
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, 6);
        
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, 0);

        // unbind buffer
        rightGeomBuffer.unbind();
        
        rsm.endRender(gl);
    }
}