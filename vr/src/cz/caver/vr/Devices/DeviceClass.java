package cz.caver.vr.devices;

import java.util.EnumSet;
import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum DeviceClass {
    INVALID, HMD, CONTROLLER, GENERIC_TRACKER, TRACKING_REFERENCE, DISPLAY_REDIRECT;

    public static final EnumSet<DeviceButton> ALL_OPTS = EnumSet.allOf(DeviceButton.class);
    
    public static final int getRepresentation(DeviceClass c) {
        switch(c) {
            case INVALID:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_Invalid;
            case HMD:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_HMD;
            case CONTROLLER:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_Controller;
            case GENERIC_TRACKER:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_GenericTracker;
            case TRACKING_REFERENCE:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_TrackingReference;
            case DISPLAY_REDIRECT:
                return VR.ETrackedDeviceClass_TrackedDeviceClass_DisplayRedirect;
        }
        return 0;
    }
    
    public static final DeviceClass getClass(int representation) {
        switch(representation) {
            case VR.ETrackedDeviceClass_TrackedDeviceClass_HMD:
                return DeviceClass.HMD;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_Controller:
                return DeviceClass.CONTROLLER;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_GenericTracker:
                return DeviceClass.GENERIC_TRACKER;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_TrackingReference:
                return DeviceClass.TRACKING_REFERENCE;
            case VR.ETrackedDeviceClass_TrackedDeviceClass_DisplayRedirect:
                return DeviceClass.DISPLAY_REDIRECT;
        }
        return DeviceClass.INVALID;
    }
}
