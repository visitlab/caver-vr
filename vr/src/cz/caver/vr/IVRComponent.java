package cz.caver.vr;

import org.lwjgl.openvr.VREvent;

/**
 * Common interface for each VR component. Defines interface for initialization, disposal, event processing and rendering workflow.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public interface IVRComponent {
    
    /**
     * Initializes component
     * @return 
     */
    public IVRComponent init(VirtualReality vr);
    
    /**
     * Gets init state of the component
     * @return 
     */
    public boolean isInitialized();
    
    /**
     * Disposes all resources
     * @param vr 
     */
    public void dispose(VirtualReality vr);
    
    /**
     * Gets active state of the component
     * @return 
     */
    public boolean isActive();

    /**
     * Enables component globally
     * @param active 
     */
    public void setActive(boolean active);
    
    /**
     * Returns virtual reality object.
     * @return 
     */
    public VirtualReality getVR();
    
    /**
     * Called at the very beginning of frame rendering
     */
    public void onFrameBegin(Eye eye);
    
    /**
     * Returns true if component wants to listen VR events
     * @return 
     */
    public boolean isListeningVREvents();
    
    /**
     * Called when any VR event occurs
     * @param event Event
     */
    void processEvent(VREvent event);
    
    /**
     * Returns true if component wants to do general render.
     * @return 
     */
    public boolean doesGeneralRendering();
    
    /**
     * Does general render, for example overlay render to texture and so on...
     * This render pass is done just before scene render pass and only once pre both eyes render.
     * @param vr
     * @param eye Always GENERIC
     */
    public void renderGeneral(VirtualReality vr, Eye eye);
    
    /**
     * Returns true if component wants to render into VR scene
     * @return 
     */
    public boolean doesSceneRendering();
    
    /**
     * Does actual render to scene framebuffer. All renderers have this buffer binded.
     * @param vr
     * @param eye 
     */
    public void renderScene(VirtualReality vr, Eye eye);
    
    
    /**
     * Called at the very end of frame rendering
     */
    public void onFrameEnd(Eye eye);
}