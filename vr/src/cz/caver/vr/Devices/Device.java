package cz.caver.vr.devices;

import com.caversoft.log.Log;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.vr.rendering.Model;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.rendering.ModelRenderer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.HashSet;
import org.lwjgl.PointerBuffer;
import org.lwjgl.openvr.RenderModel;
import org.lwjgl.openvr.RenderModelTextureMap;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRControllerState;
import org.lwjgl.openvr.VRRenderModels;
import org.lwjgl.openvr.VRSystem;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Device {
    protected static VirtualReality vr;
    private static final Log LOG = new Log(Device.class);
    
    protected final int index;
    
    protected DeviceClass deviceClass;
    protected DeviceRole deviceRole;
    protected VRControllerState state;
    
    protected long lastPressedFlags;
    protected long curPressedFlags;
    
    protected HashSet<DeviceButton> pressedButtons;
    protected TouchpadAxis touchpadAxis;
    
    protected boolean visible = true;
    protected boolean disabled = false;
    
    protected boolean someButtonDown;
    protected boolean someButton;
    protected boolean someButtonUp;
    
    protected final Pose pose;
    
    private final static HashMap<String, Model> models = new HashMap<>();
    protected Model renderModel;
    protected String renderModelName;
    
    public Device(int index, VirtualReality vr) {
        this.index = index;
        this.vr = vr;
        deviceClass = DeviceClass.getClass(VRSystem.VRSystem_GetTrackedDeviceClass(index));
        pose = new Pose(this);
    }
    
    public void disable() {
        disabled = true;
    }
    
    public void enable() {
        disabled = true;
    }
    
    void refreshModel() {
        if(renderModel == null) {
            // try to find a model we've already set up
            renderModelName = getTrackedDeviceString(index,
                    VR.ETrackedDeviceProperty_Prop_RenderModelName_String);

            renderModel = findOrLoadRenderModel(vr.getGL(), renderModelName);

            if (renderModel == null) {
                String trackingSystemName = getTrackedDeviceString(index,
                        VR.ETrackedDeviceProperty_Prop_TrackingSystemName_String);
                LOG.error("Unable to load render model for tracked device " + index + "("
                        + trackingSystemName + "." + renderModelName + ")");
            }
        }
    }
    
    private String getTrackedDeviceString(int device, int prop) {
        IntBuffer propError = GLBuffers.newDirectIntBuffer(new int[]{VR.ETrackedPropertyError_TrackedProp_Success});
        int requiredBufferLen = VRSystem.VRSystem_GetStringTrackedDeviceProperty(device, prop, null, propError);

        if (requiredBufferLen == 0) {
            return "";
        }

        String deviceString = VRSystem.VRSystem_GetStringTrackedDeviceProperty(device, prop, requiredBufferLen, propError);
        return deviceString;
    }
    
    private Model findOrLoadRenderModel(GL2GL3 gl, String modelName) {
        Model model = models.containsKey(modelName) ? models.get(modelName) : null;

        // load the model if we didn't find one
        if (model == null) {

            IntBuffer errorBuffer = GLBuffers.newDirectIntBuffer(1);
            VR.VR_GetGenericInterface(VR.IVRRenderModels_Version, errorBuffer);            
            if (errorBuffer.get(0) != VR.EVRRenderModelError_VRRenderModelError_None) {
                return null;
            }

            int error;
            PointerBuffer modelPtr = PointerBuffer.allocateDirect(1);
            
            while (true) {

                error = VRRenderModels.VRRenderModels_LoadRenderModel_Async(modelName, modelPtr);
                if (error != VR.EVRRenderModelError_VRRenderModelError_Loading) {
                    break;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException ex) {
                    LOG.error(ex);
                }
            }
            
            if(error == VR.EVRRenderModelError_VRRenderModelError_None) {
                RenderModel renderModel = new RenderModel(modelPtr.getByteBuffer(RenderModel.SIZEOF));

                PointerBuffer texturePtr = PointerBuffer.allocateDirect(1);

                while (true) {
                    error = VRRenderModels.VRRenderModels_LoadTexture_Async(renderModel.diffuseTextureId(), texturePtr);

                    if (error != VR.EVRRenderModelError_VRRenderModelError_Loading) {
                        break;
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ex) {
                        LOG.error(ex);
                    }
                }
                
                if(error == VR.EVRRenderModelError_VRRenderModelError_None) {
                    RenderModelTextureMap renderModelTexture = new RenderModelTextureMap(texturePtr.getByteBuffer(RenderModelTextureMap.SIZEOF));

                    model = new Model(modelName);

                    model.init(gl, renderModel, renderModelTexture);
                    models.put(modelName, model);
                    
                    VRRenderModels.VRRenderModels_FreeTexture(renderModelTexture);
                } else {
                    VRRenderModels.VRRenderModels_FreeRenderModel(renderModel);
                    LOG.error("Unable to load render texture id " + renderModel.diffuseTextureId()
                            + " for render model " + modelName);
                    return null;
                }
                VRRenderModels.VRRenderModels_FreeRenderModel(renderModel);
            } else {
                LOG.error("Unable to load render model " + modelName + " - "
                        + VRRenderModels.VRRenderModels_GetRenderModelErrorNameFromEnum(error));
                return null; // move on to the next tracked device
            }
        };

        return model;
    }
    
    /**
     * Returns index of tracked device.
     */
    public int getIndex() {
        return index;
    }
    
    /**
     * Returns type of this device.
     */
    public DeviceClass getDeviceClass() {
        return deviceClass;
    }
    
    /**
     * Returns role of tracked controller-class device.
     */
    public DeviceRole getDeviceRole() {
        return deviceRole;
    }
    
    /**
     * Returns raw device state.
     */
    public VRControllerState getState() {
        return state;
    }

    /**
     * Returns touchpad axis when it was pressed.
     */
    public TouchpadAxis getTouchpadAxis() {
        return touchpadAxis;
    }
    
    public Pose getPose() {
        return pose;
    }
    
    /**
     * Returns true during the frame the user starts pressing down some button.
     */
    public boolean isButtonDown() {
        return someButtonDown;
    }
    
    /**
     * Returns true when some button is pressed on a controller.
     */
    public boolean isButton() {
        return someButton;
    }
    
    /**
     * Returns true during the frame the user releases any button.
     */
    public boolean isButtonUp() {
        return someButtonUp;
    }

    public boolean isVisible() {
        return visible;
    }    
    
    /**
     * Returns true during the frame the user starts pressing down the given button.
     * 
     * @param button Button
     * @return true if button was firstly pressed
     */
    public boolean getButtonDown(DeviceButton button) {
        long mask = DeviceButton.getMask(button);
        return isButtonPressed(mask) && !wasButtonPressed(mask);
    }
    
    /**
     * Returns true when the button is pressed on a controller.
     * 
     * @param button Button
     * @return true if button is pressed
     */
    public boolean getButton(DeviceButton button) {
        long mask = DeviceButton.getMask(button);
        return isButtonPressed(mask) && wasButtonPressed(mask);
    }
    
    /**
     * Returns true during the frame the user releases the given button.
     * 
     * @param button Button
     * @return true if button was pressed a frame before but now it's not.
     */
    public boolean getButtonUp(DeviceButton button) {
        long mask = DeviceButton.getMask(button);
        return !isButtonPressed(mask) && wasButtonPressed(mask);
    }
    
    /**
     * Checks wheter specific button is pressed on a controller now.
     * 
     * @param buttonMask Button mask
     * @return true if button was pressed
     */
    private boolean isButtonPressed(long buttonMask) {
        return (curPressedFlags & buttonMask) > 0;
    }
    
    /**
     * Checks wheter specific button was pressed on a controller just a frame before.
     * 
     * @param buttonMask Button mask
     * @return true if button was pressed
     */
    private boolean wasButtonPressed(long buttonMask) {
        return (lastPressedFlags & buttonMask) > 0;
    }
    
    /**
     * Returns currently pressed buttons for given device
     * @param device
     * @return 
     */
    public HashSet<DeviceButton> getPressedButtons() {
        HashSet<DeviceButton> buttons = new HashSet<>(5);
        for (DeviceButton button : DeviceButton.values()) {
            if(isButtonPressed(DeviceButton.getMask(button))) {
                buttons.add(button);
            }
        }
        return buttons;
    }
    
    /**
     * Triggers haptic pulse on this device
     * @param duration Duration in microseconds
     * @param axis The axis the haptic pulse should have been applyed to.
     */
    public void triggerHapticPulse(short duration, int axis) {
        VRSystem.VRSystem_TriggerHapticPulse(index, axis, duration);
    }
    
    /**
     * Triggers haptic pulse on this device
     * @param duration Duration in microseconds
     */
    public void triggerHapticPulse(short duration) {
        triggerHapticPulse(duration, 0);
    }
    
    public void renderModel() {
        if(renderModel != null && deviceClass != DeviceClass.INVALID && !disabled) {
            //Render model
            ModelRenderer mr = (ModelRenderer) vr.getRenderer(ModelRenderer.class);
            mr.render(renderModel, pose.getTransform());
        }
    }
    
    public void renderPointer() {}

    protected void refresh(TrackedDevicePose trackedPose) {
        pose.refresh(trackedPose);
        
        //Update device info (class and role)
        deviceClass = DeviceClass.getClass(VRSystem.VRSystem_GetTrackedDeviceClass(index));
        if(deviceClass == DeviceClass.CONTROLLER) {
            deviceRole = DeviceRole.getRole(VRSystem.VRSystem_GetControllerRoleForTrackedDeviceIndex(index));
        } else {
            deviceRole = DeviceRole.INVALID;
        }
    }
    
    /**
     * Called each frame after input refresh to do interaction of this devíce with scene objects
     */
    protected void interact() {}
    
    /**
     * Refresh current state of this device
     */
    protected void refresh(VRControllerState state) {
        //Reset values
        someButtonDown = false;
        someButton = false;
        someButtonUp = false;
        touchpadAxis = TouchpadAxis.NONE;
        
        //Update buttons on controller device
        if(deviceClass == DeviceClass.CONTROLLER) {
            updateButtons(state);
        }
    }
    
    private void updateButtons(VRControllerState state) {
        //Copy last pressed values from current values, read new and device state
        lastPressedFlags = curPressedFlags;
        curPressedFlags = state.ulButtonPressed();
        this.state = state;

        //Update state
        someButtonDown = (curPressedFlags - lastPressedFlags) > 0;
        someButtonUp = (lastPressedFlags - curPressedFlags) > 0;
        someButton = curPressedFlags > 0;

        //Store currently pressed buttons, also update touchpad axis if the touchpad was pressed
        if(someButton) {
            pressedButtons = getPressedButtons();
            if(pressedButtons.contains(DeviceButton.STEAMVR_TOUCHPAD)) {
                updateTouchpadAxis(state);
            }
        }
    }
    
    private void updateTouchpadAxis(VRControllerState state) {
        if (state.rAxis(0).y() < -0.5f) {
            touchpadAxis = TouchpadAxis.DOWN;
        } else if (state.rAxis(0).x() < -0.5f) {
            touchpadAxis = TouchpadAxis.LEFT;
        } else if (state.rAxis(0).x() > 0.5f) {
            touchpadAxis = TouchpadAxis.RIGHT;
        } else if (state.rAxis(0).y() > 0.5f) {
            touchpadAxis = TouchpadAxis.UP;
        }
    }
}
