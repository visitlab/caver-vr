/*

 */

package cz.caver.vr.GUI.elements;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public enum WrapMode {
    NO_WRAP(0), WRAP(1);
    
    private final int value;
    
    WrapMode(int value) {
        this.value = value;
    }
    
    public int getValue() {
        return value;
    }
}
