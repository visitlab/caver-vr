package cz.caver.vr.devices;

import java.util.EnumSet;

/**
 * Contains all axes for STEAMVR_TOUCHPAD input button.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum TouchpadAxis {
    UP, DOWN, RIGHT, LEFT, NONE;

    public static final EnumSet<DeviceButton> ALL_OPTS = EnumSet.allOf(DeviceButton.class);
}
