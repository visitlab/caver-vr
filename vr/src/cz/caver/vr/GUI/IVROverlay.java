package cz.caver.vr.GUI;

import com.jogamp.opengl.GL2GL3;
import cz.caver.vr.devices.Device;
import cz.caver.vr.GUI.overlays.InputMethod;
import cz.caver.vr.GUI.overlays.OverlayFlag;
import cz.caver.vr.GUI.overlays.OverlayType;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author xkleteck
 */
public interface IVROverlay extends IVRGUIPanel {
    
    /**
     * Initializes overlay as dashboard. If overlay had been initialized before, disposes it.
     * @param key Overlay key
     * @param name Pretty name of overlay
     */
    void initAsDashboardOverlay(GL2GL3 gl, String key, String name);
    
    /**
     * Initializes overlay as scene overlay. If overlay had been initialized before, disposes it.
     * @param key Overlay key
     * @param name Pretty name of overlay
     */
    void initAsSceneOverlay(GL2GL3 gl, String key, String name);
    
    /**
     * Returns OpenVR handle for this overlay.
     * @return Handle to overlay
     */
    public long getHandle();
    
    /**
     * Returns OpenVR handle for app icon. Only functional for dashboard overlay.
     * @return Handle to app icon
     */
    public long getIconHandle();
    
    /**
     * Returns width of overlay in meters.
     * @return Overlay width
     */
    public float getVRWidth();
    
    /**
     * Returns overlay input method.
     * @return Overlay input method
     */
    public InputMethod getInputMethod();
    
    /**
     * Returns overlay type
     * @return Overlay type
     */
    public OverlayType getOverlayType();
    
    /**
     * Activates flag for this overlay.
     * @param flag VR flag
     */
    public void setFlag(OverlayFlag flag);
    
    /**
     * Deactivates flag for this overlay.
     * @param flag VR flag
     */
    public void unsetFlag(OverlayFlag flag);
    
    /**
     * Returns desired flag state for this overlay.
     * @param flag VR flag
     * @return State (on/off)
     */
    public boolean getFlag(OverlayFlag flag);
    
    
    /**
     * Sets overlay to follow desired device with given offset
     * @param device Device to follow
     * @param localOffset Offset in local space - eg. vector (1, 0, 0) will offset overlay in front direction of controller.
     */
    public void setOverlayTransformTrackedDeviceRelative(Device device, Vector3f localOffset);
    
    /**
     * Sets overlay transform absolute position.
     * @param transform Transformation matrix
     */
    public void setOverlayTransformAbsolute(Matrix4f transform);
}
