package cz.caver.vr.devices;

import com.caversoft.log.Log;
import cz.caver.renderer.glsl.GLSLProgram;
import cz.caver.renderer.glsl.GLSLProgramGenerator;
import cz.caver.vr.Eye;
import cz.caver.vr.IVRComponent;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.rendering.PrimitivesRenderer;
import cz.caver.vr.rendering.ModelRenderer;
import java.util.LinkedList;
import java.util.List;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VRControllerState;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VRSystem;
import org.openide.util.lookup.ServiceProvider;

/**
 * Class for handling VR input. Just call refresh function to update its state.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
@ServiceProvider(service = DeviceManager.class)
public class DeviceManager implements IVRComponent {
    private final static Log LOG = new Log(DeviceManager.class);
    private final TrackedDevicePose.Buffer trackedDevicePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
    private final TrackedDevicePose.Buffer trackedDeviceGamePoses = TrackedDevicePose.create(VR.k_unMaxTrackedDeviceCount);
    private static VirtualReality vr;
    
    Device[] devices; //For each controller one entry
    
    List<Device> validDevices = new LinkedList<>();
    
    //Shortcuts to devices
    Device hmd;
    Device leftController;
    Device rightController;
    
    private boolean someButtonDown;
    private boolean someButton;
    private boolean someButtonUp;
    
    private boolean initialized = false;
    private boolean active = true;
    private boolean controllersDisabled = false;
    
    private GLSLProgram program;
    
    @Override
    public IVRComponent init(VirtualReality vr) {
        devices = new Device[VR.k_unMaxTrackedDeviceCount];
        DeviceManager.vr = vr;
        for(int i = 0; i < VR.k_unMaxTrackedDeviceCount; i++) {
            if(DeviceClass.getClass(VRSystem.VRSystem_GetTrackedDeviceClass(i)) == DeviceClass.CONTROLLER) {
                devices[i] = new Controller(i, vr);
            } else {
                devices[i] = new Device(i, vr);
            }
            
        }
        program = new GLSLProgramGenerator()
                .addVertexShaderResource("/resources/shaders/model.vert")
                .addFragmentShaderResource("/resources/shaders/model.frag")
                .getDebugProgram(vr.getGL(), "Models");
        
        refreshModels();
        initialized = true;
        return this;
    }
    
    private void refreshModels() {
        for(Device d : devices) {
            if(d.getDeviceClass() == DeviceClass.CONTROLLER || d.getDeviceClass() == DeviceClass.GENERIC_TRACKER) {
                d.refreshModel();
            }
        }
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    public void disableControllers() {
        controllersDisabled = true;
    }
    
    public void enableControllers() {
        controllersDisabled = false;
    }
    
    @Override
    public void dispose(VirtualReality vr) {}
    
    @Override
    public VirtualReality getVR() {
        return vr;
    }
    
    public boolean isSomeButtonDown() {
        return someButtonDown;
    }
    
    public boolean isSomeButton() {
        return someButton;
    }
    
    public boolean isSomeButtonUp() {
        return someButtonUp;
    }
    
    /**
     * Returns currently pressed buttons for given device
     * @param device
     * @return 
     */
    public Device getDevice(int index) {
        if(index < 0 || index >= VR.k_unMaxTrackedDeviceCount) {
            return null;
        }
        return devices[index];
    }
    
    public List<Device> getDevices() {
        return validDevices;
    }

    public Device getHmd() {
        return hmd;
    }

    public Device getLeftController() {
        return leftController;
    }

    public Device getRightController() {
        return rightController;
    }

    @Override
    public void onFrameBegin(Eye eye) {
        refreshPositioning();
        refreshInput();
        if(!controllersDisabled){
            for(Device d : devices) {
                d.interact();
            }
        }
    }

    @Override
    public boolean isListeningVREvents() {
        return true;
    }

    @Override
    public void processEvent(VREvent event) {
        switch(event.eventType()) {
            case VR.EVREventType_VREvent_ButtonPress:
            case VR.EVREventType_VREvent_ButtonTouch:
            case VR.EVREventType_VREvent_ButtonUnpress:
            case VR.EVREventType_VREvent_ButtonUntouch:
            case VR.EVREventType_VREvent_TrackedDeviceUserInteractionEnded:
            case VR.EVREventType_VREvent_TrackedDeviceUpdated:
                refreshModels();
                break;
            case VR.EVREventType_VREvent_TrackedDeviceActivated:
            case VR.EVREventType_VREvent_TrackedDeviceUserInteractionStarted:
            case VR.EVREventType_VREvent_TrackedDeviceDeactivated:
            case VR.EVREventType_VREvent_TrackedDeviceRoleChanged:
                if(event.trackedDeviceIndex() > 0) {
                    LOG.debug("Device " + event.trackedDeviceIndex() + " changed. Updating ...\n");
                    if(DeviceClass.getClass(VRSystem.VRSystem_GetTrackedDeviceClass(event.trackedDeviceIndex())) != devices[event.trackedDeviceIndex()].deviceClass) {
                        if(DeviceClass.getClass(VRSystem.VRSystem_GetTrackedDeviceClass(event.trackedDeviceIndex())) == DeviceClass.CONTROLLER) {
                            devices[event.trackedDeviceIndex()] = new Controller(event.trackedDeviceIndex(), vr);
                        } else {
                            devices[event.trackedDeviceIndex()] = new Device(event.trackedDeviceIndex(), vr);
                        }
                    }
                    devices[event.trackedDeviceIndex()].refreshModel();
                }
                break;
        }
    }
    
    @Override
    public boolean doesGeneralRendering() {
        return false;
    }
    
    @Override
    public void renderGeneral(VirtualReality vr, Eye eye) {}
    
    @Override
    public boolean doesSceneRendering() {
        return true;
    }

    @Override
    public void renderScene(VirtualReality vr, Eye eye) {
        if(!controllersDisabled) {
            ModelRenderer mr = (ModelRenderer) vr.getRenderer(ModelRenderer.class);
            mr.bindRenderBuffer(vr.getRenderbuffer());
            for(Device d : devices) {
                d.renderModel();
            }
            mr.unbindRenderBuffer();

            PrimitivesRenderer lr = (PrimitivesRenderer) vr.getRenderer(PrimitivesRenderer.class);
            lr.bindRenderBuffer(vr.getRenderbuffer());
            for(Device d : devices) {
                d.renderPointer();
            }
            lr.unbindRenderBuffer();
        }
    }

    @Override
    public void onFrameEnd(Eye ye) {}
    
    private void refreshPositioning() {
        validDevices.clear();
        //Get poses - in batched way
        VRCompositor.VRCompositor_WaitGetPoses(trackedDevicePoses, trackedDeviceGamePoses);
        //Update devices
        for (int index = 0; index < VR.k_unMaxTrackedDeviceCount; index++) {
            TrackedDevicePose pose = trackedDevicePoses.get(index);
            Device d = devices[index];
            d.refresh(pose);
            
            if(d.deviceClass != DeviceClass.INVALID) {
                validDevices.add(d);
                //Update shortcuts
                if(d.deviceClass == DeviceClass.CONTROLLER) {
                    if(d.deviceRole == DeviceRole.LEFT_HAND) {
                        leftController = d;
                    } else if(d.deviceRole == DeviceRole.RIGHT_HAND) {
                        rightController = d;
                    }
                } else if(d.deviceClass == DeviceClass.HMD) {
                    hmd = d;
                }
            }
        }
    }
    
    private void refreshInput() {
        //Reset state
        someButtonDown = false;
        someButton = false;
        someButtonUp = false;
        
        //TODO do it only for valid devices
        //Update devices
        for (int index = 0; index < VR.k_unMaxTrackedDeviceCount; index++) {
            VRControllerState state = VRControllerState.create();
            if (VRSystem.VRSystem_GetControllerState(index, state)) {
                Device d = devices[index];
                d.refresh(state);
                
                if(d.deviceClass != DeviceClass.INVALID) {
                    
                    //Update buttons
                    if(d.deviceClass == DeviceClass.CONTROLLER) {
                        someButtonDown |= d.someButtonDown;
                        someButtonUp |= d.someButtonUp;
                        someButton |= d.someButton;

                        //Update shortcuts
                        if(d.deviceRole == DeviceRole.LEFT_HAND) {
                            leftController = d;
                        } else if(d.deviceRole == DeviceRole.RIGHT_HAND) {
                            rightController = d;
                        }
                    } else if(d.deviceClass == DeviceClass.HMD) {
                        hmd = d;
                    }
                }
            }
        }
    }
}