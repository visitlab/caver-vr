package cz.caver.vr.GUI.overlays;

import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum OverlayFlag {
    NONE, CURVED, RGSS4X, NO_DASHBOARD_TAB, ACCEPTS_GAMETAB_EVENTS, SHOW_GAMEPAD_FOCUS, SEND_VR_SCROLL_EVENTS, SEND_VR_TOUCHPAD_EVENTS, SHOW_TOUCHPAD_SCROLLWHEEL, TRANSFER_OWNERSHIP_TO_INTERNAL_PROCESS, SIDE_BY_SIDE_PARALLEL, SIDE_BY_SIDE_CROSSED, PANORAMA, STEREO_PANORAMA, SORT_WITH_NON_SCENE_OVERLAYS, VISIBLE_IN_DASHBOARD;
    
    public static int getRepresentation(OverlayFlag flag) {
        switch(flag){
            case NONE:
                return VR.VROverlayFlags_None;
            case CURVED:
                return VR.VROverlayFlags_Curved;
            case RGSS4X:
                return VR.VROverlayFlags_RGSS4X;
            case NO_DASHBOARD_TAB:
                return VR.VROverlayFlags_NoDashboardTab;
            case ACCEPTS_GAMETAB_EVENTS:
                return VR.VROverlayFlags_AcceptsGamepadEvents;
            case SHOW_GAMEPAD_FOCUS:
                return VR.VROverlayFlags_ShowGamepadFocus;
            case SEND_VR_SCROLL_EVENTS:
                return VR.VROverlayFlags_SendVRScrollEvents;
            case SEND_VR_TOUCHPAD_EVENTS:
                return VR.VROverlayFlags_SendVRTouchpadEvents;
            case SHOW_TOUCHPAD_SCROLLWHEEL:
                return VR.VROverlayFlags_ShowTouchPadScrollWheel;
            case TRANSFER_OWNERSHIP_TO_INTERNAL_PROCESS:
                return VR.VROverlayFlags_TransferOwnershipToInternalProcess;
            case SIDE_BY_SIDE_PARALLEL:
                return VR.VROverlayFlags_SideBySide_Parallel;
            case SIDE_BY_SIDE_CROSSED:
                return VR.VROverlayFlags_SideBySide_Crossed;
            case PANORAMA:
                return VR.VROverlayFlags_Panorama;
            case STEREO_PANORAMA:
                return VR.VROverlayFlags_StereoPanorama;
            case SORT_WITH_NON_SCENE_OVERLAYS:
                return VR.VROverlayFlags_SortWithNonSceneOverlays;
            case VISIBLE_IN_DASHBOARD:
                return VR.VROverlayFlags_VisibleInDashboard;
            default:
                return VR.VROverlayFlags_None;
        }
    }
}
