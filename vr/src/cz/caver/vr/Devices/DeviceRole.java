package cz.caver.vr.devices;

import java.util.EnumSet;
import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum DeviceRole {
    INVALID, LEFT_HAND, RIGHT_HAND, OPT_OUT, MAX;

    public static final EnumSet<DeviceButton> ALL_OPTS = EnumSet.allOf(DeviceButton.class);
    
    public static final DeviceRole getRole(int representation) {
        switch(representation) {
            case VR.ETrackedControllerRole_TrackedControllerRole_LeftHand:
                return DeviceRole.LEFT_HAND;
            case VR.ETrackedControllerRole_TrackedControllerRole_RightHand:
                return DeviceRole.RIGHT_HAND;
            case VR.ETrackedControllerRole_TrackedControllerRole_OptOut:
                return DeviceRole.OPT_OUT;
            case VR.ETrackedControllerRole_TrackedControllerRole_Max:
                return DeviceRole.MAX;
        }
        return DeviceRole.INVALID;
    }
}
