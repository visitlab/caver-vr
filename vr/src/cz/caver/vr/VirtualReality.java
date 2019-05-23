package cz.caver.vr;

import cz.caver.vr.rendering.ModelRenderer;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.GLAnimatorControl;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.controller.selection.SelectionController;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.RenderingPostProcessor;
import cz.caver.renderer.RenderingPreProcessor;
import cz.caver.renderer.RenderingType;
import cz.caver.renderer.StructureRenderingType;
import cz.caver.renderer.display.Camera;
import cz.caver.renderer.display.DisplayManager;
import cz.caver.renderer.display.DisplayType;
import cz.caver.renderer.pipeline.Framebuffer;
import cz.caver.renderer.strategy.structure.SphereAOStrategy;
import cz.caver.vr.devices.Device;
import cz.caver.vr.devices.DeviceManager;
import cz.caver.vr.devices.Pose;
import cz.caver.vr.GUI.GUIManager;
import cz.caver.vr.raycasting.Raycaster;
import cz.caver.vr.rendering.IRenderer;
import cz.caver.vr.rendering.PrimitivesRenderer;
import cz.caver.vr.rendering.QuadRenderer;
import cz.caver.vr.rendering.UIRenderer;
import static cz.caver.vr.utils.Matrices.hmdMatrix34ToMatrix4f;
import static cz.caver.vr.utils.Matrices.hmdMatrix44ToMatrix4f;
import cz.caver.vr.utils.Utils;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.vecmath.Matrix4f;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import javax.vecmath.Vector3f;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.*;

/**
 * Rendering pre- and post- processor. Tracks devices, processes OpenVR events 
 * and updates and renders the scene
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
@ServiceProvider(service = VirtualReality.class)
public class VirtualReality implements RenderingPreProcessor, RenderingPostProcessor {
    private static final Log LOG = new Log(VirtualReality.class);
    private static final String ACTION_MANIFEST_PATH = "/resources/vr/action_manifest.json";
    private static final float NEAR_PLANE = 0.1f;
    private static final float FAR_PLANE = 100f;
    
    private boolean useVR = true;
    private boolean hasVR = false;
    
    private boolean VRInitialized = false;
    private boolean VRInitializing = false;
    private boolean VRInitError = false;
    private boolean GLInitialized = false;
    
    IntBuffer VRErrorBuffer = null;
    
    // projection and eye matrices
    private final Matrix4f[] projectionMatrix = new Matrix4f[2];
    private final Matrix4f[] eyeMatrix = new Matrix4f[2];
    
    private static GL2GL3 gl;
    
    private int width = 1750;
    private int height = 1945;
    private boolean reshape = false;
    
    private final RenderManager rm = Lookup.getDefault().lookup(RenderManager.class);
    private final DisplayManager dm = Lookup.getDefault().lookup(DisplayManager.class);
    
    /**
     * Components
     */
    private final Playground playground = Lookup.getDefault().lookup(Playground.class);
    private final DeviceManager devices = Lookup.getDefault().lookup(DeviceManager.class);
    private final GUIManager guiManager = Lookup.getDefault().lookup(GUIManager.class);
    
    private final List<IVRComponent> components = new LinkedList<>();
    private final List<IVRComponent> eventListeners = new LinkedList<>();
    private final List<IVRComponent> sceneRenderers = new LinkedList<>();
    private final List<IVRComponent> generalRenderers = new LinkedList<>();
    
    private final List<IRenderer> renderers = new ArrayList<>();
    
    private Raycaster raycaster;
    
    /**
     * Default renderTextureQuad manager values.
     */
    private Framebuffer defaultFbo;
    private final int[] caverAppViewport = new int[4];
    /**
     * Framebuffers
     */
    private Framebuffer renderFbo;
    private Framebuffer leftFbo;
    private Framebuffer rightFbo;
    
    /**
     * Eye.
     */
    private Eye eye = Eye.LEFT;
    
    public enum VRRenderingType implements RenderingType {
        STRUCTURE_VDW_AO
    }
    
    /**
     * Checks if VR is currently present.
     * @return true if current operating system has VR installed and also HMD is present.
     */
    private boolean checkVR() {
        return VR.VR_IsRuntimeInstalled() /*&& VR.VR_IsHmdPresent()*/;
    }
    
    private void initGL(GL2GL3 gl) {
        if (renderFbo == null) {
            VirtualReality.gl = gl;
            renderFbo = null;
            leftFbo = null;
            rightFbo = null;
            renderFbo = new Framebuffer(gl, width, height, 4,
            Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8,
            Framebuffer.AttachmentType.RENDERBUFFER, GL.GL_DEPTH_COMPONENT24);
            
            gl.glEnable(GL2GL3.GL_DEPTH_TEST);
            
            leftFbo = Framebuffer.newColorFramebuffer(gl, width, height, Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8);
            rightFbo = Framebuffer.newColorFramebuffer(gl, width, height, Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8);
            
            gl.glBindTexture(GL.GL_TEXTURE_2D, leftFbo.getColorAttachment(0).getAttachmentObject());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_BASE_LEVEL, 0);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);

            gl.glBindTexture(GL.GL_TEXTURE_2D, rightFbo.getColorAttachment(0).getAttachmentObject());
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_BASE_LEVEL, 0);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);

            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            
            renderers.add(new ModelRenderer(gl));
            renderers.add(new PrimitivesRenderer(gl));
            renderers.add(new QuadRenderer(gl));
            renderers.add(new UIRenderer(gl));
            
            int lodDistsSpheres[] = { 80, 250, 1000 };
            
            rm.registerRenderingStrategy(VRRenderingType.STRUCTURE_VDW_AO, new SphereAOStrategy(4, lodDistsSpheres, lodDistsSpheres, 25, false));
            
            rm.reshape(gl, width, height);
            reshape = false;
        }
        GLInitialized = true;
    }
    
    /**
     * Initializes VR runtime, compositor and sets up other related variables.
     */
    private void initVR() {
        
        // AJ: schedule animator start
        // TODO introduce real time mode API
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                GLAnimatorControl animator = dm.getAnimator(DisplayType.DEFAULT);
                if (animator.isPaused()) {
                    LOG.debug("VR animator started");
                    animator.resume();
                }
            }
        }, 2000);
        
        LOG.debug("VR_IsRuntimeInstalled() = " + VR.VR_IsRuntimeInstalled());
        LOG.debug("VR_RuntimePath() = " + VR.VR_RuntimePath());
        LOG.debug("VR_IsHmdPresent() = " + VR.VR_IsHmdPresent());
        
        VRErrorBuffer = GLBuffers.newDirectIntBuffer(1);
        
        // initialize VR runtime
        int token = VR.VR_InitInternal(VRErrorBuffer, VR.EVRApplicationType_VRApplication_Scene);   
        if (VRErrorBuffer.get(0) != VR.EVRInitError_VRInitError_None) {
            VRInitError = true;
            String msg = "VR_Init failed: " + VR.VR_GetVRInitErrorAsEnglishDescription(VRErrorBuffer.get(0));
            LOG.debug(msg);
            //throw new Error(msg);
            return;
        }
        
        OpenVR.create(token);
        LOG.debug("Model Number : " + VRSystem.VRSystem_GetStringTrackedDeviceProperty(
            VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_ModelNumber_String, VRErrorBuffer));
        LOG.debug("Serial Number: " + VRSystem.VRSystem_GetStringTrackedDeviceProperty(
            VR.k_unTrackedDeviceIndex_Hmd, VR.ETrackedDeviceProperty_Prop_SerialNumber_String, VRErrorBuffer));
        
        VR.VR_GetGenericInterface(VR.IVRCompositor_Version, VRErrorBuffer);
        if (VRErrorBuffer.get(0) != VR.EVRCompositorError_VRCompositorError_None) {
            String msg = "Compositor initialization failed: " + VR.VR_GetVRInitErrorAsEnglishDescription(VRErrorBuffer.get(0));
            throw new Error(msg);
        }
        
        // get required rendering resolution
        IntBuffer widthBuffer = GLBuffers.newDirectIntBuffer(1);
        IntBuffer heightBuffer = GLBuffers.newDirectIntBuffer(1);
        VRSystem.VRSystem_GetRecommendedRenderTargetSize(widthBuffer, heightBuffer);
        width = widthBuffer.get(0);
        height = heightBuffer.get(0);
        
        // setup eye matrices
        for (int i = 0; i < 2; i++) {
            projectionMatrix[i] = hmdMatrix44ToMatrix4f(VRSystem.VRSystem_GetProjectionMatrix(i, NEAR_PLANE, FAR_PLANE, HmdMatrix44.create()));
            projectionMatrix[i].transpose();
            eyeMatrix[i] = hmdMatrix34ToMatrix4f(VRSystem.VRSystem_GetEyeToHeadTransform(i, HmdMatrix34.create()));
            eyeMatrix[i].invert();
        }
        
        raycaster = new Raycaster(this);
        
        //Show window on the primary monitor to display what is being shown in the headset
        VRCompositor.VRCompositor_ShowMirrorWindow();
        
        StructureManipulation.vr = this;
        
        /*URL actionManifest = VirtualReality.class.getClassLoader().getResource(ACTION_MANIFEST_PATH);
        try {
            String s = actionManifest.getPath();
            ByteBuffer path = getBufferFromString(s);
            VRInput.VRInput_SetActionManifestPath(path);
        } catch (CharacterCodingException ex) {}*/
        
        /*List<File> files = Lookup.getDefault().lookup(LoadStructureManager.class).getAvailableFiles();
        for (File f : files) {
            log.debug(f.getName());
        }*/
        // disable these to up performance
        /*trackedDevicePosesReference.setAutoRead(false);
        trackedDevicePosesReference.setAutoWrite(false);
        trackedDevicePosesReference.setAutoSynch(false);
        for (int i = 0; i < VR.EVREye.Max; i++) {
            trackedDevicePose[i].setAutoRead(false);
            trackedDevicePose[i].setAutoWrite(false);
            trackedDevicePose[i].setAutoSynch(false);
        }*/
    }
    
    /**
     * Disposes of all allocated resources.
     */
    public void dispose() {
        for(IVRComponent component : components) {
            component.dispose(this);
        }
        
        for(IRenderer renderer : renderers) {
            renderer.dispose();
        }
        
        VR.VR_ShutdownInternal();
    }
    
    public void registerComponent(IVRComponent component) {
        components.add(component);
        if(component.isListeningVREvents()) {
            eventListeners.add(component);
        }
        if(component.doesSceneRendering()) {
            sceneRenderers.add(component);
        }
        if(component.doesGeneralRendering()) {
            generalRenderers.add(component);
        }
        component.init(this);
    }
    
    @Override
    public void preProcess(GL2GL3 gl) {
        if(!hasVR) {
            hasVR = checkVR();
        } else {
            if(!VRInitialized && !VRInitializing && hasVR){
                VRInitializing = true;
                useVR = false;
                useVR = Utils.showConfirmBox("Enable VR?", "VR system has been detected. Do you want to switch into VR?");
                if(useVR) {
                    if (!VRInitialized && !VRInitError) {
                        initVR();
                        if(!GLInitialized) {
                            initGL(gl);
                        }
                        registerComponent(playground);
                        registerComponent(devices);
                        registerComponent(guiManager);
                        VRInitialized = true;
                    }
                    if(!GLInitialized) {
                        initGL(gl);
                    }
                } else {
                    VRInitialized = true;
                }
                VRInitializing = false;
            }
            if(useVR && GLInitialized && VRInitialized) {
                // set custom framebuffer, keep default
                defaultFbo = rm.getFramebuffer();
                rm.setFramebuffer(renderFbo);

                // preserve caver viewport and setup viewport for current eye
                gl.glGetIntegerv(GL.GL_VIEWPORT, caverAppViewport, 0);
                gl.glViewport(0, 0, width, height);
                if (reshape) {
                    rm.reshape(gl, width, height);
                    reshape = false;
                }
                
                if (VRInitError) {
                    projectionMatrix[0] = new Matrix4f(0.7554825f, 0.0f, -0.05427399f, 0.0f, 
                                        0.0f, 0.68013144f, -0.0011704417f, 0.0f, 
                                        0f, 0f, -1.001001f, -0.1001001f, 
                                        0.0f, 0.0f, -1f, 0.0f);
                    eyeMatrix[0] = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 
                                              0.0f, 1.0f, 0.0f, 0.0f, 
                                              0.0f, 0.0f, 1.0f, 0.0f, 
                                              0.0307f, 0.0f, -0.015f, 1.0f);

                    projectionMatrix[1] = new Matrix4f(0.7545491f, 0.0f, 0.058457125f, 0.0f, 
                                                    0.0f, 0.6794828f, -0.004624166f, 0.0f, 
                                                    0f, 0f, -1.001001f, -0.1001001f, 
                                                    0.0f, 0.0f, -1f, 0.0f);
                    eyeMatrix[1] = new Matrix4f(1.0f, 0.0f, 0.0f, 0.0f, 
                                              0.0f, 1.0f, 0.0f, 0.0f, 
                                              0.0f, 0.0f, 1.0f, 0.0f, 
                                              -0.0307f, 0.0f, -0.015f, 1.0f);
                }
                
                for(IVRComponent component : components) {
                    if(!component.isInitialized()) {
                        component.init(this);
                    }
                    if(component.isActive()) {
                        try {
                            component.onFrameBegin(eye);
                        } catch (Exception e) {
                            LOG.error("Frame begin error: ", e);
                        }
                    }
                }
                
                setupCamera();
                //TODO setup stencil buffer and culling - https://github.com/ValveSoftware/openvr/wiki/IVRSystem::GetHiddenAreaMesh
            }
        }
    }
    
    @Override
    public void postProcess(GL2GL3 gl) {
        if(hasVR && useVR && GLInitialized && VRInitialized) {
            //testing.renderTextureQuad();
            try {
                
                if (!VRInitError) {
                    handleInput(gl);
                }
                
                // Process OpenVR events
                VREvent event = VREvent.create();
                while (VRSystem.VRSystem_PollNextEvent(event, event.sizeof())) {
                    for(IVRComponent component : eventListeners) {
                        if(component.isActive()) component.processEvent(event);
                    }
                }

                guiManager.processEvents();
                if(eye == Eye.LEFT) {
                    for(IVRComponent component : generalRenderers) {
                        if(component.isActive()) {
                            try {
                                component.renderGeneral(this, Eye.GENERIC);
                            } catch (Exception e) {
                                LOG.error("General rendering error: ", e);
                            }
                        }
                    }
                }
                
                //Flush all renderer queued jobs
                for(IRenderer renderer : renderers) {
                    renderer.flush();
                }
                
                // can be used to force fragment write when depth peeling active
                //gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
                for(IVRComponent component : sceneRenderers) {
                    if(component.isActive()) {
                        try {
                            // bind renderFbo as it is not bound anymore at time of post processing,
                            // defaultFbo (FBO with OpenGL id 0) is bound instead
                            renderFbo.bind(gl);

                            // refresh viewport
                            gl.glViewport(0, 0, width, height);

                            component.renderScene(this, eye);
                        } catch (Exception e) {
                            LOG.error("Rendering error: ", e);
                        }
                    }
                }
                
                //Flush all renderer queued jobs
                for(IRenderer renderer : renderers) {
                    renderer.flush();
                }
                
                renderFbo.unbind(gl);

                // resolve multisampled (MSAA) to non-MS texture
                renderFbo.bindRead(gl);
                getEyeFramebuffer(eye).bindDraw(gl);

                gl.glBlitFramebuffer(0, 0, width, height,
                        0, 0, width, height,
                        GL.GL_COLOR_BUFFER_BIT, GL.GL_NEAREST);

                renderFbo.unbindRead(gl);
                getEyeFramebuffer(eye).unbindDraw(gl);

                if (eye == Eye.RIGHT) {
                    if (!VRInitError) {
                        for (int i = 0; i < 2; i++) {
                            // sumbmit both eye textures to compositor
                            int textureHandle = getEyeFramebuffer(i).getColorAttachment(0).getAttachmentObject();
                            Texture eyeTexture = Texture.create();
                            eyeTexture.set(textureHandle, VR.ETextureType_TextureType_OpenGL, 
                                            VR.EColorSpace_ColorSpace_Gamma);
                            VRCompositor.VRCompositor_Submit(i, eyeTexture, null, VR.EVRSubmitFlags_Submit_Default);
                        }
                    }

                    // renderTextureQuad composite image to default framebuffer
                    QuadRenderer r = (QuadRenderer) getRenderer(QuadRenderer.class);
                    r.bindRenderBuffer(defaultFbo);
                    r.renderEyeQuads(leftFbo, rightFbo, caverAppViewport);
                    r.unbindRenderBuffer();
                }
            } catch (GLException e) {
                LOG.error("General rendering error: ", e);
            }

            for(IVRComponent component : components) {
                if(component.isActive()) {
                    component.onFrameEnd(eye);
                }
            }
            
            rm.setFramebuffer(defaultFbo);
            gl.glViewport(0, 0, caverAppViewport[2], caverAppViewport[3]);
            
            //Switch eyes
            eye = eye == Eye.LEFT ? Eye.RIGHT : Eye.LEFT;
        }
    }
    
    private Matrix4f getCurrentViewMatrix() {
        Matrix4f eyeView;
        if(devices.getHmd() != null) {
            eyeView = new Matrix4f(devices.getHmd().getPose().getTransform());
            eyeView.invert();
        } else {
            eyeView = new Matrix4f(Pose.getDefaultHmdPose());
        }
        eyeView.mul(eyeMatrix[eye.getValue()]);
        return eyeView;
    }
    
    private Matrix4f getCurrentProjectionMatrix() {
        return projectionMatrix[eye.getValue()];
    }
    
    /**
     * Setups required camera matrices according current eye position.
     */
    private void setupCamera() {
        Camera camera = dm.getCamera(DisplayType.DEFAULT);
        
        Matrix4f view = getCurrentViewMatrix();
        Vector3f position = new Vector3f(view.m30, view.m31, view.m32);
        camera.setProjectionMatrix2(getCurrentProjectionMatrix());
        
        camera.setRotationMatrixTo(view);
        camera.computeFromRotationMatrix();
        camera.moveCamera(position);
        camera.setCenterDistance(0.0f);
        //Renderer.setView(eye, view);
        //camera.setAspect(1);
//        Matrix4f mat = new Matrix4f(camera.getMatrix());
//        mat.invert();
//        Vector3f cameraPosition = new Vector3f(mat.m30, mat.m31, mat.m32);
//        log.debug("Camera position: " + cameraPosition);
    }
    
    public GL2GL3 getGL() {
        return gl;
    }
    
    /**
     * Returns render buffer for currently rendered eye
     * @return 
     */
    public Framebuffer getRenderbuffer() {
        return renderFbo;
    }
    
    public IRenderer getRenderer(String typename) {
        for(IRenderer r : renderers) {
            if(r.getClass().getTypeName().equals(typename)) {
                return r;
            }
        }
        return null;
    }
    
    public IRenderer getRenderer(Class c) {
        for(IRenderer r : renderers) {
            if(r.getClass().equals(c)) {
                return r;
            }
        }
        return null;
    }
    
    public GUIManager getGUIManager() {
        return guiManager;
    }
    
    public Playground getPlayground() {
        return playground;
    }
    
    public Device getHMD() {
        if(devices != null) {
            return devices.getHmd();
        }
        return null;
    }
    
    public List<Device> getActiveDevices() {
        return devices.getDevices();
    }
    
    private Framebuffer getEyeFramebuffer(Eye eye) {
        return getEyeFramebuffer(eye.getValue());
    }
    
    private Framebuffer getEyeFramebuffer(int eye) {
        if (eye == VR.EVREye_Eye_Left) {
            return leftFbo;
        } else {
            return rightFbo;
        }
    }
    
    /**
     * Processes OpenVR events and checks state of controllers.
     * 
     * @param gl 
     */
    private void handleInput(GL2GL3 gl) {
        /*if(devices.isSomeButton()) {
            for(Device device : devices.getDevices()) {
                if(device.getPressedButtons().isEmpty()) {
                    StructureManipulation.endManipulation(device);
                }
                if(device.isButton()) {
                    RaycastHit hit = Raycaster.doRaycast(device);
                    
                    //Structure manipulation & selection
                    if(device.isButtonDown() && hit.isSuccess() && hit.getHitStructure() != null) {
                        StructureManipulation.beginManipulation(device, hit);
                        
                        if(device.getButtonDown(DeviceButton.STEAMVR_TRIGGER)) {
                            sc.select(hit.getHitStructure().getStructure());
                        }
                    }
                    StructureManipulation.continueManipulation(device);
                    //Clear selection
                    if(device.getButtonDown(DeviceButton.STEAMVR_TRIGGER) && !hit.isSuccess()) {
                        sc.clear();
                    }
                    
                    
                    //TODO
                    //GUI panels input
                    if(device.isButtonDown()) {
                        //overlays.showGUIPanel("Structure Wall");
                        //overlays.getOverlay("Dashboard").showKeyboard(null, "label", "text", 12, KeyboardInputMode.NORMAL, KeyboardLineInputMode.SINGLE_LINE, false, 0);
                        if (device.getDeviceRole() == DeviceRole.LEFT_HAND) {
                            toggleMenus(device);
                        }
                        checkUIInteraction(device);
                    }
                }
            }
        }
        //End manipulation
        if(devices.isSomeButtonUp()) {
            for(Device device : devices.getDevices()) {
                if(device.getPressedButtons().isEmpty()) {
                    StructureManipulation.endManipulation(device);
                }
            }
        }*/
    }
    
    /**
     * Checks which axis of the touchpad was pressed and toggles corresponding menu.
     * @param state Controller state
     */
    private void toggleMenus(Device device) {
        /*
        switch (device.getTouchpadAxis()) {
            case DOWN:
                ui.toggleVisibleMenu(UI.Menu.DYNAMICS_CONTROL);
                break;
            case LEFT:
                ui.toggleVisibleMenu(UI.Menu.LOAD_STRUCTURE);
                break;
            case RIGHT:
                ui.toggleVisibleMenu(UI.Menu.RENDERING_TYPE);
                break;
            case UP:
                ui.toggleOverlay();
                break;
                
        }*/
    }

    /**
     * Checks whether user clicked a button with the controller.
     * 
     * @param device Device
     */
    private void checkUIInteraction(Device device) {
        /*for (UIElement button : ui.getVisibleButtons()) {
            Vector3f buttonPosition = new Vector3f(button.getTranslation());
            
            // get device position in view space
            Vector3f devicePosition = device.getPose().getPosition();
            Matrix4f view = new Matrix4f(dm.getCamera(DisplayType.DEFAULT).getMatrix());
            view.transpose();
            view.transform(devicePosition);
            
            if (checkCollision(devicePosition, buttonPosition, 0.1f)) {
                if (device.getButtonDown(DeviceButton.STEAMVR_TRIGGER)) {
                    VRSystem.VRSystem_TriggerHapticPulse(device.getIndex(), 0, (short) 3999);
                    log.debug("trigger - button");
                    button.click();
                    return;
                }
            }
        }*/
    }
    
    @Override
    public void reshape(GL2GL3 gl, int width, int height) {
        reshape = true;
    }
}