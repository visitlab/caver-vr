package cz.caver.vr;

import org.lwjgl.openvr.VR;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum Eye {
    LEFT(VR.EVREye_Eye_Left), RIGHT(VR.EVREye_Eye_Right), GENERIC(2);

    private final int value;
    private Eye(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
