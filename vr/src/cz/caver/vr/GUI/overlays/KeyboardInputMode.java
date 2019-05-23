package cz.caver.vr.GUI.overlays;

import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum KeyboardInputMode {
    NORMAL, PASSWORD, SUBMIT;
    
    public static int getRepresentation(KeyboardInputMode mode) {
        switch (mode) {
            case NORMAL:
                return VR.EGamepadTextInputMode_k_EGamepadTextInputModeNormal;
            case PASSWORD:
                return VR.EGamepadTextInputMode_k_EGamepadTextInputModePassword;
            case SUBMIT:
                return VR.EGamepadTextInputMode_k_EGamepadTextInputModeSubmit;
        }
        return VR.EGamepadTextInputMode_k_EGamepadTextInputModeNormal;
    }
}
