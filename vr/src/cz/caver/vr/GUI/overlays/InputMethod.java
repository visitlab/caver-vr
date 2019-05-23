package cz.caver.vr.GUI.overlays;

import org.lwjgl.openvr.VR;

/**
 *
 * @author xkleteck
 */
public enum InputMethod {
    NONE, MOUSE, DUAL_ANALOG;
    
    public static int getRepresentation(InputMethod m) {
        switch (m) {
            case NONE:
                return VR.VROverlayInputMethod_None;
            case MOUSE:
                return VR.VROverlayInputMethod_Mouse;
            case DUAL_ANALOG:
                return VR.VROverlayInputMethod_DualAnalog;
        }
        return VR.VROverlayInputMethod_None;
    }
}
