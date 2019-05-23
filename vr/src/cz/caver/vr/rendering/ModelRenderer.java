package cz.caver.vr.rendering;

import com.caversoft.log.Log;
import com.jogamp.opengl.DebugGL4;
import com.jogamp.opengl.GL2GL3;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.renderer.glsl.GLSLUniformManager;
import cz.caver.renderer.util.GLUtils;
import javax.vecmath.Matrix4f;

/**
 * Loades, manages and renders models of tracked devices.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
public class ModelRenderer extends Renderer {
    private static final Log LOG = new Log(ModelRenderer.class);
    
    private GLSLProgram program;

    public ModelRenderer(GL2GL3 gl) {
        super(gl);
        
        program = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/model.vert")
                .addFragmentShaderResource("/resources/shaders/model.frag")
                .getDebugProgram(gl, "Models");
    }
    
    @Override
    public void dispose() {
        program.invalidate();
    }
    
    @Override
    public void flush() {}

    public void render(Model m, Matrix4f modelPose) {   
        if(renderBuffer == null) {
            LOG.error("No render buffer is attached to model renderer");
            return;
        }
        
        program.use();
        modelPose.transpose();

        GLSLUniformManager.getInstance().setModelMatrix(modelPose);
        Matrix4f mvp = GLSLUniformManager.getInstance().getModelViewProjectionMatrix();
        program.setUniformMatrix4(GLSLUniformManager.Uniform.MODEL_VIEW_PROJECTION_MATRIX.getGLSLName(),
            GLUtils.asArray(mvp));
        
        //TODO
        m.render(gl, program, modelPose);
    }
}
