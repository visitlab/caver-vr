package cz.caver.vr.rendering;

import com.caversoft.color.Color;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import cz.caver.renderer.buffer.ColoredGeomBuffer;
import cz.caver.renderer.buffer.GeomBuffer;
import cz.caver.renderer.effect.Effect;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.renderer.glsl.GLSLUniformManager;
import cz.caver.renderer.util.GLUtils;
import cz.caver.renderer.util.RenderingUtils;
import cz.caver.vr.utils.Cube;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author xkleteck
 */
public class PrimitivesRenderer extends Renderer {
    private static final ColoredGeomBuffer LINE_BUFFER = new ColoredGeomBuffer(2, false);
    private static final ColoredGeomBuffer GIZMO_CUBE_BUFFER = new ColoredGeomBuffer(36, false);
    private static final ColoredGeomBuffer CUBE_BUFFER = new ColoredGeomBuffer(36, false);
    // OpenGL resources
    private Effect effect;
    
    private GLSLProgram cubeProgram;
    
    public PrimitivesRenderer(GL2GL3 gl) {
        super(gl);
        LINE_BUFFER.generateGLBuffers(gl);
        
        cubeProgram = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/cube.vert")
                .addFragmentShaderResource("/resources/shaders/cube.frag")
                .getDebugProgram(gl, "Cube");
        
        CUBE_BUFFER.generateGLBuffers(gl);
        GIZMO_CUBE_BUFFER.generateGLBuffers(gl);
        for (int i = 0; i < 6; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], Color.RED);
        }
        for (int i = 6; i < 12; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], Color.GREEN);
        }
        for (int i = 12; i < 18; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], Color.BLUE);
        }
        for (int i = 18; i < 24; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], new Color(255/2, 0, 0));
        }
        for (int i = 24; i < 30; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], new Color(0, 255/2, 0));
        }
        for (int i = 30; i < 36; i++) {
            GIZMO_CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], new Color(0, 0, 255/2));
        }
        
        // load effect
        if (effect == null) {
            try {
                effect = Effect.newEffectFromGLFX(gl, "/resources/effects/util.glfx");
            } catch (IOException ex) {
                // TODO log
            }
        }
    }
    
    @Override
    public void dispose() {
        LINE_BUFFER.invalidateAndDeleteGLBuffers();
        GIZMO_CUBE_BUFFER.invalidateAndDeleteGLBuffers();
    }
    
    @Override
    public void flush() {}
    
    public void renderCube(Vector3f center, Vector3f scale, Color color) {
        Matrix4f model = new Matrix4f();
        model.setIdentity();

        model.m00 *= scale.x;
        model.m11 *= scale.y;
        model.m22 *= scale.z;
        model.setTranslation(center);
        
        CUBE_BUFFER.clear();
        for (int i = 0; i < Cube.CUBE_TRIANGLES.length; i++) {
            CUBE_BUFFER.addColoredVertex(Cube.CUBE_TRIANGLES[i], color);
        }
        
        cubeProgram.use();
        
        GLSLUniformManager.getInstance().setModelMatrix(model);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        cubeProgram.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
                GLUtils.asArray(mvp));
        
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                cubeProgram.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                cubeProgram.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        CUBE_BUFFER.bind(attributes);
        
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, Cube.CUBE_TRIANGLES.length);

        CUBE_BUFFER.unbind();
    }
    
    public void renderGizmoCube(Vector3f center, float scale) {
        PrimitivesRenderer.this.renderGizmoCube(center, new Vector3f(scale, scale, scale));
    }
    
    public void renderGizmoCube(Vector3f center, Vector3f scale) {
        Matrix4f model = new Matrix4f();
        model.setIdentity();

        model.m00 *= scale.x;
        model.m11 *= scale.y;
        model.m22 *= scale.z;
        model.setTranslation(center);
        
        cubeProgram.use();
        
        GLSLUniformManager.getInstance().setModelMatrix(model);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        cubeProgram.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
                GLUtils.asArray(mvp));
        
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                cubeProgram.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                cubeProgram.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        GIZMO_CUBE_BUFFER.bind(attributes);
        
        gl.glDrawArrays(GL.GL_TRIANGLES, 0, Cube.CUBE_TRIANGLES.length);

        GIZMO_CUBE_BUFFER.unbind();
    }
    
    public void renderDebugAxes(Vector3f center, float length) {
        Matrix4f model = new Matrix4f();
        model.setIdentity();

        model.setScale(length);
        model.setTranslation(center);
        
        GLSLUniformManager.getInstance().setModelMatrix(model);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        cubeProgram.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
                GLUtils.asArray(mvp));
        
        RenderingUtils.debugRenderAxes(gl, length);
    }
    
    public void renderLine(Vector3f startPoint, Vector3f endPoint, Color color) {
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to line renderer");
            return;
        }
        
        // setup OpenGL state
        FloatBuffer lineWidthData = FloatBuffer.allocate(1);
        gl.glGetFloatv(GL.GL_LINE_WIDTH, lineWidthData);
        gl.glLineWidth(2f);
        
        // set lines geometry
        LINE_BUFFER.clear();
        LINE_BUFFER.addColoredVertex(startPoint, color);
        LINE_BUFFER.addColoredVertex(endPoint, color);
        
        // use program
        GLSLProgram program = effect.getTechnique("Solid").getPass(0).getProgram();
        program.use();
        
        // set MVP matrix
        Matrix4f model = new Matrix4f();
        model.setIdentity();
        GLSLUniformManager.getInstance().setModelMatrix(model);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        program.setUniformMatrix4("g_ModelViewProjectionMatrix", GLUtils.asArray(mvp));
        
        // bind buffer
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                program.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                program.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        LINE_BUFFER.bind(attributes);
        
        gl.glDrawArrays(GL.GL_LINES, 0, 2);
        
        // unbind buffer
        LINE_BUFFER.unbind();
        
        // restore OpenGL state
        gl.glLineWidth(lineWidthData.get());
    }
}
