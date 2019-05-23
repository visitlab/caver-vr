package cz.caver.vr.GUI;

import com.caversoft.color.Color;
import com.caversoft.log.Log;
import com.jogamp.opengl.DebugGL4;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.renderer.RenderStateManager;
import cz.caver.renderer.buffer.ColoredGeomBuffer;
import cz.caver.renderer.buffer.GeomBuffer;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.renderer.state.BlendState;
import cz.caver.renderer.state.RenderState;
import cz.caver.vr.GUI.elements.Button;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3f;
import org.lwjgl.openvr.VR;

/**
 * Manages and renders UI elements.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
public class UI {
    // menus
    private List<Button> renderingTypeButtons = new ArrayList<>();
    private List<Button> loadStructureButtons = new ArrayList<>();
    private List<Button> dynamicsControlButtons = new ArrayList<>();
    
    public static enum Menu {
        NONE,
        RENDERING_TYPE,
        LOAD_STRUCTURE,
        DYNAMICS_CONTROL
    }
    
    // defines which menu is visible
    private Menu visibleMenu = Menu.NONE;
    
    private static final Point3f[] QUAD_POSITIONS = new Point3f[] {
        new Point3f(-1f, -1f, 0f),
        new Point3f( 1f, -1f, 0f),
        new Point3f( 1f,  1f, 0f),
        new Point3f(-1f,  1f, 0f)
    };
    
    private ColoredGeomBuffer rectangleBuffer = new ColoredGeomBuffer(6, false);
    
    private GLSLProgram elementProgram;
    
    private Log log = new Log(UI.class);

    //private LoadStructureForm loadStructureForm;
    
    private boolean sceneChanged = false;

    public UI(GL2GL3 gl) {
        init(gl);
        
        IntBuffer errorBuffer = GLBuffers.newDirectIntBuffer(1);
        VR.VR_GetGenericInterface(VR.IVROverlay_Version, errorBuffer);
        if (errorBuffer.get(0) != VR.EVROverlayError_VROverlayError_None) {
            String msg = "Overlay initialization failed: " + VR.VR_GetVRInitErrorAsEnglishDescription(errorBuffer.get(0));
            throw new Error(msg);
        }
        
        //loadStructureForm = new LoadStructureForm("Load Structure", gl);
    }
    
    private void init(GL2GL3 gl) {
        rectangleBuffer.generateGLBuffers(gl);
        rectangleBuffer.clear();
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[0], new Color(0f, 0f, 0f));
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[1], new Color(1f, 0f, 0f));
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[2], new Color(1f, 1f, 0f));
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[0], new Color(0f, 0f, 0f));
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[2], new Color(1f, 1f, 0f));
        rectangleBuffer.addColoredVertex(QUAD_POSITIONS[3], new Color(0f, 1f, 0f));
        
        elementProgram = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/element.vert")
                .addFragmentShaderResource("/resources/shaders/element.frag")
                .getDebugProgram(gl, "Element");
        
        addButtons(gl);
    }
    
    /**
     * Returns list of currently visible buttons.
     * 
     * @return List of buttons
     */
    public List<Button> getVisibleButtons() {
        switch (this.visibleMenu) {
            case NONE:
                return new ArrayList<>();
            case RENDERING_TYPE:
                return renderingTypeButtons;
            case LOAD_STRUCTURE:
                return loadStructureButtons;
            case DYNAMICS_CONTROL:
                return dynamicsControlButtons;
            default:
                log.error("Unsupported menu type: " + this.visibleMenu);
                return null;
        }
    }
    
    /**
     * Checks if UI has valid buffers.
     * @param gl
     * @return true if UI has valid buffers
     */
    public boolean isValid(GL2GL3 gl) {
        return !(rectangleBuffer.hasGLBuffers() && rectangleBuffer.getGL() != gl);
    }
    
    /**
     * Invaidates all used buffers and programs.
     */
    public void invalidate() {
        rectangleBuffer.invalidateGLBuffers();
        
        if (elementProgram != null) {
            elementProgram.invalidate();
            elementProgram = null;
        }
    }
    
    private void addButtons(GL2GL3 gl) {
        // create buttons for toggling of structure/tunnel rendering types
        /*
        Button lines = new Button();
        lines.setClicked(true);
        lines.setAnchorOffset(new Vector2f(0.25f, 0.21f));
        lines.setMethod(() -> ButtonFunctions.setStructureRenderingType(Arrays.asList(StructureRenderingType.STRUCTURE_LINES, 
                        StructureRenderingType.STRUCTURE_POINTS_NOBONDS), lines));
        lines.setTexture(loadTexture(gl, "/resources/textures/structure_lines_128.png", TextureIO.PNG));
        renderingTypeButtons.add(lines);
        
        Button sticks = new Button();
        sticks.setAnchorOffset(new Vector2f(0.25f, 0.07f));
        sticks.setMethod(() -> ButtonFunctions.setStructureRenderingType(Arrays.asList(StructureRenderingType.STRUCTURE_STICKS_AO), sticks));
        sticks.setTexture(loadTexture(gl, "/resources/textures/structure_sticks_128.png", TextureIO.PNG));
        renderingTypeButtons.add(sticks);
        
        Button vdw = new Button();
        vdw.setAnchorOffset(new Vector2f(0.25f, -0.07f));
        vdw.setMethod(() -> ButtonFunctions.setStructureRenderingType(Arrays.asList(StructureRenderingType.STRUCTURE_VDW_AO), vdw));
        vdw.setTexture(loadTexture(gl, "/resources/textures/structure_vdw_128.png", TextureIO.PNG));
        renderingTypeButtons.add(vdw);
        
        Button cartoon = new Button();
        cartoon.setAnchorOffset(new Vector2f(0.25f, -0.21f));
        cartoon.setMethod(() -> ButtonFunctions.setStructureRenderingType(Arrays.asList(StructureRenderingType.STRUCTURE_CARTOON), cartoon));
        cartoon.setTexture(loadTexture(gl, "/resources/textures/structure_ss_128.png", TextureIO.PNG));
        renderingTypeButtons.add(cartoon);
        
        Button toggleHdr = new Button();
        toggleHdr.setClicked(true);
        toggleHdr.setAnchorOffset(new Vector2f(-0.25f, 0.21f));
        toggleHdr.setMethod(() -> ButtonFunctions.toggleHydrogenVisibility(toggleHdr));
        toggleHdr.setTexture(loadTexture(gl, "/resources/textures/hydrogen_128.png", TextureIO.PNG));
        renderingTypeButtons.add(toggleHdr);
        
        Button tunnelDots = new Button();
        tunnelDots.setClicked(true);
        tunnelDots.setAnchorOffset(new Vector2f(-0.25f, 0.07f));
        tunnelDots.setMethod(() -> ButtonFunctions.setTunnelRenderingType(Arrays.asList(TunnelRenderingType.TUNNEL_DOTS), tunnelDots));
        tunnelDots.setTexture(loadTexture(gl, "/resources/textures/tunnel_points_128.png", TextureIO.PNG));
        renderingTypeButtons.add(tunnelDots);
        
        Button tunnelLines = new Button();
        tunnelLines.setAnchorOffset(new Vector2f(-0.25f, -0.07f));
        tunnelLines.setMethod(() -> ButtonFunctions.setTunnelRenderingType(Arrays.asList(TunnelRenderingType.TUNNEL_CENTERLINE), tunnelLines));
        tunnelLines.setTexture(loadTexture(gl, "/resources/textures/tunnel_centerline_128.png", TextureIO.PNG));
        renderingTypeButtons.add(tunnelLines);
        
        Button tunnelSpheres = new Button();
        tunnelSpheres.setClicked(true);
        tunnelSpheres.setAnchorOffset(new Vector2f(-0.25f, -0.21f));
        tunnelSpheres.setMethod(() -> ButtonFunctions.setTunnelRenderingType(Arrays.asList(TunnelRenderingType.TUNNEL_SPHERES), tunnelSpheres));
        tunnelSpheres.setTexture(loadTexture(gl, "/resources/textures/tunnel_spheres_128.png", TextureIO.PNG));
        renderingTypeButtons.add(tunnelSpheres);
        
        // create buttons for loading structures and computing tunnels/hydrogens
        Button loadStatic1 = new Button();
        loadStatic1.setAnchorOffset(new Vector2f(-0.25f, 0.21f));
        loadStatic1.setMethod(() -> ButtonFunctions.loadStaticStructure("1cqw"));
        loadStatic1.setTexture(loadTexture(gl, "/resources/textures/1cqw_128.png", TextureIO.PNG));
        loadStructureButtons.add(loadStatic1);
        
        Button loadStatic2 = new Button();
        loadStatic2.setAnchorOffset(new Vector2f(-0.25f, 0.07f));
        loadStatic2.setMethod(() -> ButtonFunctions.loadStaticStructure("1a3n"));
        loadStatic2.setTexture(loadTexture(gl, "/resources/textures/1a3n_128.png", TextureIO.PNG));
        loadStructureButtons.add(loadStatic2);
        
        Button loadStatic3 = new Button();
        loadStatic3.setAnchorOffset(new Vector2f(-0.25f, -0.07f));
        loadStatic3.setMethod(() -> ButtonFunctions.loadStaticStructure("1bkv"));
        loadStatic3.setTexture(loadTexture(gl, "/resources/textures/1bkv_128.png", TextureIO.PNG));
        loadStructureButtons.add(loadStatic3);
        
        Button loadDynamic = new Button();
        loadDynamic.setAnchorOffset(new Vector2f(-0.25f, -0.21f));
        loadDynamic.setMethod(() -> ButtonFunctions.loadDynamicStructure("/resources/dynamics/md/", new String[] {"model.1", "model.2", "model.3", "model.4", "model.5", 
            "model.6", "model.7", "model.8", "model.9", "model.10"}));
        loadDynamic.setTexture(loadTexture(gl, "/resources/textures/md_128.png", TextureIO.PNG));
        loadStructureButtons.add(loadDynamic);
        
        Button computeHdr = new Button();
        computeHdr.setAnchorOffset(new Vector2f(0.25f, 0.07f));
        computeHdr.setMethod(() -> ButtonFunctions.computeHydrogens());
        computeHdr.setTexture(loadTexture(gl, "/resources/textures/hydrogen_computation_128.png", TextureIO.PNG));
        loadStructureButtons.add(computeHdr);
        
        Button computeTun = new Button();
        computeTun.setAnchorOffset(new Vector2f(0.25f, -0.07f));
        computeTun.setMethod(() -> ButtonFunctions.computeTunnels());
        computeTun.setTexture(loadTexture(gl, "/resources/textures/tunnel_computation_128.png", TextureIO.PNG));
        loadStructureButtons.add(computeTun);
        
        // creates button for manipulation with dynamic structures
        Button previousFrame = new Button();
        previousFrame.setAnchorOffset(new Vector2f(-0.21f, -0.20f));
        previousFrame.setMethod(() -> ButtonFunctions.previousFrameButton());
        previousFrame.setTexture(loadTexture(gl, "/resources/textures/dynamics_previous_128.png", TextureIO.PNG));
        dynamicsControlButtons.add(previousFrame);
        
        Button play = new Button();
        play.setAnchorOffset(new Vector2f(-0.07f, -0.20f));
        play.setMethod(() -> ButtonFunctions.playButton(play));
        play.setTexture(loadTexture(gl, "/resources/textures/dynamics_forward_128.png", TextureIO.PNG));
        dynamicsControlButtons.add(play);
        
        Button stop = new Button();
        stop.setAnchorOffset(new Vector2f(0.07f, -0.20f));
        stop.setMethod(() -> ButtonFunctions.stopButton());
        stop.setTexture(loadTexture(gl, "/resources/textures/dynamics_stop_128.png", TextureIO.PNG));
        dynamicsControlButtons.add(stop);
        
        Button nextFrame = new Button();
        nextFrame.setAnchorOffset(new Vector2f(0.21f, -0.20f));
        nextFrame.setMethod(() -> ButtonFunctions.nextFrameButton());
        nextFrame.setTexture(loadTexture(gl, "/resources/textures/dynamics_next_128.png", TextureIO.PNG));
        dynamicsControlButtons.add(nextFrame);
        */
    }
    
    public void setVisibleMenu(Menu menu) {
        this.visibleMenu = menu;
    }

    public void setSceneChanged(boolean sceneChanged) {
        this.sceneChanged = sceneChanged;
    }
    
    public void toggleOverlay() {
        //loadStructureForm.toggleVisible();
        setSceneChanged(true);
    }
    
    public void toggleVisibleMenu(Menu menu) {
        if (this.visibleMenu != menu) {
            this.visibleMenu = menu;
        } else {
            this.visibleMenu = Menu.NONE;
        }
    }
    
    public void onSceneChanged(GL2GL3 gl) {
        /*if (loadStructureForm.isVRVisible()) {
            loadStructureForm.renderToTexture(gl);
        }*/
    }
    
    public void handleEvents() {
        //loadStructureForm.handleEvents();
    }
    
    /**
     * Renders currently visible menu.
     * 
     * @param gl 
     */
    public void render(GL2GL3 gl) {
        handleEvents();
        //log.debug("Overlay visible: " + loadStructureForm.isVRVisible());
        if (sceneChanged) {
            log.debug("Scene changed");
            this.onSceneChanged(gl);
            sceneChanged = false;
        }
        
        if (this.visibleMenu == Menu.NONE) {
            return;
        }
        
        gl = new DebugGL4(gl.getGL4());
        
        elementProgram.use();
        
        RenderStateManager rsm = RenderStateManager.getInstance();
        RenderState rs = rsm.beginRender(gl);
        
        BlendState blend = rs.setBlendEnabled(gl, true);
        blend.setFunc(gl, GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        Map<String, Integer> attributes = new HashMap<>();
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_POSITION,
                elementProgram.getAttributeLocation(GeomBuffer.Attribute.POSITION.getGLSLName()));
        attributes.put(ColoredGeomBuffer.ATTRIBUTE_COLOR,
                elementProgram.getAttributeLocation(GeomBuffer.Attribute.COLOR.getGLSLName()));
        rectangleBuffer.bind(attributes);
         
        /*for (Button button : getVisibleButtons()) {
            button.render(gl, elementProgram);
        }*/

        rsm.endRender(gl);
        rectangleBuffer.unbind();
    }

    /**
     * Loades and returnes texture with given name.
     * 
     * @param gl
     * @param filename Path to the texture
     * @param suffix Image format of the texture
     * @return Loaded texture
     */
    private Texture loadTexture(GL2GL3 gl, String filename, String suffix) {
        try (InputStream is = UI.class.getResourceAsStream(filename)) {
            Texture texture = TextureIO.newTexture(is, false, suffix);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            return texture;
        } catch (IOException ex) { 
            log.error("Failed loading texture: " + filename, ex);
            return null;
        }
    }
}
