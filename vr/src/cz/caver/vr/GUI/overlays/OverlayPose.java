/*

 */

package cz.caver.vr.GUI.overlays;

import cz.caver.vr.GUI.IVRGUIPanel;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.lwjgl.openvr.HmdMatrix34;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class OverlayPose {
    
    // Link to the device
    private final IVRGUIPanel overlay;
    // Transform encoding the position and rotation of the device in tracker space
    protected final Matrix4f transform;

    /* Computed */
    protected final Quat4f rotation;
    
    protected final Vector3f position;

    public OverlayPose(IVRGUIPanel overlay) {
        this.overlay = overlay;
        
        transform = new Matrix4f();
        transform.setIdentity();
        rotation = new Quat4f();
        rotation.set(0, 0, 0, 0);
        position = new Vector3f();
        position.set(0, 0, 0);
    }

    public IVRGUIPanel getDevice() {
        return overlay;
    }

    public Matrix4f getTransform() {
        return transform;
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public Quat4f getRotation() {
        return rotation;
    }
    
    public void setPosition(float x, float y, float z) {
        position.x = x;
        position.y = y;
        position.z = z;
        refresh();
    }
    
    public void setRotation(AxisAngle4f angle) {
        rotation.set(angle);
        refresh();
    }
    
    protected void refresh() {
        transform.m30 = position.x;
        transform.m31 = position.y;
        transform.m32 = position.z;
        transform.setRotation(rotation);
    }
    
    private static void fillTransformMatrix(HmdMatrix34 hmd, Matrix4f mat) {
        mat.m00 = hmd.m(0);
        mat.m01 = hmd.m(4);
        mat.m02 = hmd.m(8);
        mat.m03 = 0f;
        
        mat.m10 = hmd.m(1);
        mat.m11 = hmd.m(5);
        mat.m12 = hmd.m(9);
        mat.m13 = 0f;
        
        mat.m20 = hmd.m(2);
        mat.m21 = hmd.m(6);
        mat.m22 = hmd.m(10);
        mat.m23 = 0f;
        
        mat.m30 = hmd.m(3);
        mat.m31 = hmd.m(7);
        mat.m32 = hmd.m(11);
        mat.m33 = 1f;
    }

}
