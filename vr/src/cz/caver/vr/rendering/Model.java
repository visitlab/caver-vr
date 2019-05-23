package cz.caver.vr.rendering;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureData;
import cz.caver.renderer.buffer.GeomBuffer;
import cz.caver.renderer.buffer.TexturedGeomBuffer;
import cz.caver.renderer.glsl.GLSLProgram;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import one.util.streamex.IntStreamEx;
import org.lwjgl.openvr.RenderModel;
import org.lwjgl.openvr.RenderModelTextureMap;
import org.lwjgl.openvr.RenderModelVertex;

/**
 * Represent renderable model of a VR device.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
public class Model {
    
    public String name;
    private int vertexCount;

    private static enum Buffer {
        VERTEX(0),
        ELEMENT(1),
        MAX(2);
        
        private final int value;
        private Buffer(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /*private IntBuffer bufferName = GLBuffers.newDirectIntBuffer(Buffer.MAX.getValue());
    private IntBuffer vertexArrayName = GLBuffers.newDirectIntBuffer(1);*/
    private IntBuffer textureName = GLBuffers.newDirectIntBuffer(1);
    
    private TexturedGeomBuffer modelBuffer;
    
    private List<Vector3f> positions = new ArrayList<>();
    private List<Vector3f> normals = new ArrayList<>();
    private List<Vector2f> texCoords = new ArrayList<>();
    private List<Integer> indices = new ArrayList<>();
    
    private Texture texture;

    public Model(String name) {
        this.name = name;
    }

    /**
     * Allocates and populates the GL resources for a render model.
     *
     * @param gl
     * @param model
     * @param textureReference
     * @return
     */
    public void init(GL2GL3 gl, RenderModel model, RenderModelTextureMap textureReference) {
        vertexCount = model.unTriangleCount() * 3;
        
        initBuffers(gl, model);
        initVertexArray(gl);
        initTexture(gl, textureReference);
    }
    
    /**
     * Checks if Model has valid buffers.
     * @param gl
     * @return true if model has valid buffers
     */
    public boolean isValid(GL2GL3 gl) {
        return !(modelBuffer.hasGLBuffers() && modelBuffer.getGL() != gl);
    }
    
    /**
     * Invalidates all used buffers and programs.
     */
    public void invalidate() {
        modelBuffer.invalidateGLBuffers();
    }
    
    private void fillBuffer() {
        for (int i = 0; i < positions.size(); i++) {
            modelBuffer.addTexturedVertex(positions.get(i), texCoords.get(i));
        }
        modelBuffer.updateNormals(normals);
        modelBuffer.updateIndices(indices);
    }
    
    /*private void toByteBuffer(RenderModelVertex.Buffer vertices, ByteBuffer buffer) {
        while(vertices.remaining() > 0) {
                RenderModelVertex v = vertices.get();			
                buffer.putFloat(v.vPosition().v(0));
                buffer.putFloat(v.vPosition().v(1));
                buffer.putFloat(v.vPosition().v(2));

                buffer.putFloat(v.vNormal().v(0));
                buffer.putFloat(v.vNormal().v(1));
                buffer.putFloat(v.vNormal().v(2));

                buffer.putFloat(v.rfTextureCoord().get(0));
                buffer.putFloat(v.rfTextureCoord().get(1));
        }
    }*/

    private void initBuffers(GL2GL3 gl, RenderModel model) {
        modelBuffer = new TexturedGeomBuffer(model.unVertexCount(), true, model.unTriangleCount() * 3);
        
        RenderModelVertex.Buffer vertices = model.rVertexData();
        while (vertices.remaining() > 0) {
                RenderModelVertex v = vertices.get();
                
                Vector3f position =  new Vector3f(v.vPosition().v(0),        v.vPosition().v(1),        v.vPosition().v(2));
                Vector3f normal =    new Vector3f(v.vNormal().v(0),          v.vNormal().v(1),          v.vNormal().v(2));
                Vector2f texCooord = new Vector2f(v.rfTextureCoord().get(0), v.rfTextureCoord().get(1));
                
                positions.add(position);
                normals.add(normal);
                texCoords.add(texCooord);
        }
        
        ShortBuffer indices = model.IndexData();
        for (int i = 0; i < model.unTriangleCount() * 3; i++) {
            this.indices.add((int) indices.get(i));
        }
        
        fillBuffer();
        
        modelBuffer.generateGLBuffers(gl);
        
        /*
        gl.glGenBuffers(Buffer.MAX.getValue(), bufferName);

        // Populate a vertex buffer
        int vertexBufferSize = RenderModelVertex.SIZEOF * model.unVertexCount();
        ByteBuffer vertexBuffer = GLBuffers.newDirectByteBuffer(vertexBufferSize);
        RenderModelVertex.Buffer vertices = model.rVertexData();

        toByteBuffer(vertices, vertexBuffer);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX.getValue()));
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexBuffer.capacity(), vertexBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        
        //BufferUtils.destroyDirectBuffer(vertexBuffer);

        // Create and populate the index buffer
        int indexBufferSize = Short.BYTES * vertexCount;
        ByteBuffer elementBuffer = GLBuffers.newDirectByteBuffer(indexBufferSize);
        short[] elements = new short[vertexCount];
        model.IndexData().get(elements);

        IntStreamEx.range(elements.length).forEach(i -> elementBuffer.putShort(i * Short.BYTES, elements[i]));

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT.getValue()));
        gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, elementBuffer.capacity(), elementBuffer, GL.GL_STATIC_DRAW);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

        //BufferUtils.destroyDirectBuffer(elementBuffer);*/
    }

    private void initVertexArray(GL2GL3 gl) {
        /*// create and bind a VAO to hold state for this model
        gl.glGenVertexArrays(1, vertexArrayName);
        gl.glBindVertexArray(vertexArrayName.get(0));

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, bufferName.get(Buffer.VERTEX.getValue()));

        // Identify the components in the vertex buffer
        gl.glEnableVertexAttribArray(0);
        gl.glVertexAttribPointer(0, 3, GL.GL_FLOAT, false, RenderModelVertex.SIZEOF, 0);
        gl.glEnableVertexAttribArray(3);
        gl.glVertexAttribPointer(3, 3, GL.GL_FLOAT, false, RenderModelVertex.SIZEOF, HmdVector3.SIZEOF);
        gl.glEnableVertexAttribArray(1);
        gl.glVertexAttribPointer(1, 2, GL.GL_FLOAT, false, RenderModelVertex.SIZEOF, 2*HmdVector3.SIZEOF);

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);

        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, bufferName.get(Buffer.ELEMENT.getValue()));

        gl.glBindVertexArray(0);*/
    }

    private void initTexture(GL2GL3 gl, RenderModelTextureMap diffuseTexture) {
        // create and populate the texture
        //gl.glGenTextures(1, textureName);
        //gl.glBindTexture(GL.GL_TEXTURE_2D, textureName.get(0));

        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(diffuseTexture.unWidth() * diffuseTexture.unHeight() * 4 * Byte.BYTES);
        byte[] data = new byte[diffuseTexture.unWidth() * diffuseTexture.unHeight() * 4];
        diffuseTexture.rubTextureMapData(data.length).get(data);

        IntStreamEx.range(data.length).forEach(i -> buffer.put(i, data[i]));
        
        texture = new Texture(gl, new TextureData(gl.getGLProfile(), GL.GL_RGBA, diffuseTexture.unWidth(), diffuseTexture.unHeight(), 
                0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, true, false, false, buffer, null));
        texture.setTexParameterf(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameterf(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameterf(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        texture.setTexParameterf(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);
        
        FloatBuffer largest = GLBuffers.newDirectFloatBuffer(1);
        gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, largest);
        texture.setTexParameterf(gl, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest.get(0));
        
        /*gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, diffuseTexture.unWidth(), diffuseTexture.unHeight(),
                0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, buffer);

        gl.glGenerateMipmap(GL.GL_TEXTURE_2D);

        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR_MIPMAP_LINEAR);

        FloatBuffer largest = GLBuffers.newDirectFloatBuffer(1);
        gl.glGetFloatv(GL.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, largest);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAX_ANISOTROPY_EXT, largest.get(0));

        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);*/

        //BufferUtils.destroyDirectBuffer(buffer);
        //BufferUtils.destroyDirectBuffer(largest);
    }

    /**
     * Draws the render model.
     * @param gl 
     */
    public void render(GL2GL3 gl, GLSLProgram program, Matrix4f model) {
        
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(TexturedGeomBuffer.ATTRIBUTE_POSITION,
                program.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(TexturedGeomBuffer.ATTRIBUTE_NORMAL,
                program.getAttributeLocation(GeomBuffer.Attribute.NORMAL.getGLSLName()));
        attributes.put(TexturedGeomBuffer.ATTRIBUTE_TEX_COORD,
                program.getAttributeLocation(GeomBuffer.Attribute.TEX_COORD.getGLSLName()));
        
        modelBuffer.clear();
        fillBuffer();
        modelBuffer.bind(attributes);
        
        
        gl.glActiveTexture(GL.GL_TEXTURE0);
        texture.bind(gl);
        
//        gl.glDrawArrays(GL2GL3.GL_TRIANGLES, 0, vertexCount);
        gl.glDrawElements(GL.GL_TRIANGLES, vertexCount, GL.GL_UNSIGNED_INT, 0);
        
        gl.glBindTexture(GL2GL3.GL_TEXTURE_2D, 0);
        
        modelBuffer.unbind();
        
        /*gl.glBindVertexArray(vertexArrayName.get(0));
        
        gl.glActiveTexture(GL.GL_TEXTURE0 + 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureName.get(0));
        
        gl.glDrawElements(GL.GL_TRIANGLES, vertexCount, GL.GL_UNSIGNED_SHORT, 0);
        
        gl.glBindVertexArray(0);*/
    }
    
    /**
     * Frees the GL resources for a render model.
     * @param gl 
     */
    public void delete(GL2GL3 gl) {
        /*if (gl.glIsBuffer(bufferName.get(0))) {
            gl.glDeleteBuffers(Buffer.MAX.getValue(), bufferName);
        }
        if (gl.glIsVertexArray(vertexArrayName.get(0))) {
            gl.glDeleteVertexArrays(1, vertexArrayName);
        }*/
        /*if (gl.glIsTexture(textureName.get(0))) {
            gl.glDeleteTextures(1, textureName);
        }*/
        //BufferUtils.destroyDirectBuffer(bufferName);
        //BufferUtils.destroyDirectBuffer(vertexArrayName);
        //BufferUtils.destroyDirectBuffer(textureName);
    }
}
