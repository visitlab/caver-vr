package cz.caver.vr.devices;

import java.util.EnumSet;
import org.lwjgl.openvr.VR;

/**
 * Contains all allowed input button types.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum DeviceButton {
    SYSTEM, APPLICATION_MENU, GRIP, DPAD_LEFT, DPAD_UP, DPAD_RIGHT, DPAD_DOWN, A, AXIS0, AXIS1, AXIS2, AXIS3, AXIS4, STEAMVR_TOUCHPAD, STEAMVR_TRIGGER, DASHBOARD_BACK;
    
    public static final EnumSet<DeviceButton> ALL_OPTS = EnumSet.allOf(DeviceButton.class);
    
    // controller button masks
    public static final long k_EButton_System_Mask = (1L << VR.EVRButtonId_k_EButton_System);
    public static final long k_EButton_ApplicationMenu_Mask = (1L << VR.EVRButtonId_k_EButton_ApplicationMenu);
    public static final long k_EButton_Grip_Mask = (1L << VR.EVRButtonId_k_EButton_Grip);
    public static final long k_EButton_DPad_Left_Mask = (1L << VR.EVRButtonId_k_EButton_DPad_Left);
    public static final long k_EButton_DPad_Up_Mask = (1L << VR.EVRButtonId_k_EButton_DPad_Up);
    public static final long k_EButton_DPad_Right_Mask = (1L << VR.EVRButtonId_k_EButton_DPad_Right);
    public static final long k_EButton_DPad_Down_Mask = (1L << VR.EVRButtonId_k_EButton_DPad_Down);
    public static final long k_EButton_A_Mask = (1L << VR.EVRButtonId_k_EButton_A);

    public static final long k_EButton_Axis0_Mask = (1L << VR.EVRButtonId_k_EButton_Axis0);
    public static final long k_EButton_Axis1_Mask = (1L << VR.EVRButtonId_k_EButton_Axis1);
    public static final long k_EButton_Axis2_Mask = (1L << VR.EVRButtonId_k_EButton_Axis2);
    public static final long k_EButton_Axis3_Mask = (1L << VR.EVRButtonId_k_EButton_Axis3);
    public static final long k_EButton_Axis4_Mask = (1L << VR.EVRButtonId_k_EButton_Axis4);
    
    /**
     * Converts desired button to its mask
     * @param button Button
     * @return Mask for masking pressed state of any button from raw VR input
     */
    public static long getMask(DeviceButton button) {
        switch(button) {
            case SYSTEM:
                return k_EButton_System_Mask;
            case APPLICATION_MENU:
                return k_EButton_ApplicationMenu_Mask;
            case GRIP:
            case DASHBOARD_BACK:
                return k_EButton_Grip_Mask;
            case DPAD_LEFT:
                return k_EButton_DPad_Left_Mask;
            case DPAD_UP:
                return k_EButton_DPad_Up_Mask;
            case DPAD_RIGHT:
                return k_EButton_DPad_Right_Mask;
            case DPAD_DOWN:
                return k_EButton_DPad_Down_Mask;
            case A:
                return k_EButton_A_Mask;
            case AXIS0:
            case STEAMVR_TOUCHPAD:
                return k_EButton_Axis0_Mask;
            case AXIS1:
            case STEAMVR_TRIGGER:
                return k_EButton_Axis1_Mask;
            case AXIS2:
                return k_EButton_Axis2_Mask;
            case AXIS3:
                return k_EButton_Axis3_Mask;
            case AXIS4:
                return k_EButton_Axis4_Mask;
            default:
                throw new AssertionError("Button " + button.name() + " was not found!");
        }
    }
}
