package cz.caver.vr.GUI.overlays;

import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum KeyboardLineInputMode {
    SINGLE_LINE, MULTIPLE_LINES;
    public static int getRepresentation(KeyboardLineInputMode mode) {
        switch (mode) {
            case SINGLE_LINE:
                return VR.EGamepadTextInputLineMode_k_EGamepadTextInputLineModeSingleLine;
            case MULTIPLE_LINES:
                return VR.EGamepadTextInputLineMode_k_EGamepadTextInputLineModeMultipleLines;
        }
        return VR.EGamepadTextInputLineMode_k_EGamepadTextInputLineModeSingleLine;
    }
}
