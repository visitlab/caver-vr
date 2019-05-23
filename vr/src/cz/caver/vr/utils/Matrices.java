/*

 */

package cz.caver.vr.utils;

import javax.vecmath.Matrix4f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Matrices {
    public static Matrix4f hmdMatrix44ToMatrix4f(HmdMatrix44 hmdMat) {
        return new Matrix4f(hmdMat.m(0), hmdMat.m(4), hmdMat.m(8), hmdMat.m(12),
                            hmdMat.m(1), hmdMat.m(5), hmdMat.m(9), hmdMat.m(13),
                            hmdMat.m(2), hmdMat.m(6), hmdMat.m(10), hmdMat.m(14),
                            hmdMat.m(3), hmdMat.m(7), hmdMat.m(11), hmdMat.m(15));
    }
    
    public static Matrix4f hmdMatrix34ToMatrix4f(HmdMatrix34 hmdMat) {
        return new Matrix4f(hmdMat.m(0), hmdMat.m(4), hmdMat.m(8),  0f,
                            hmdMat.m(1), hmdMat.m(5), hmdMat.m(9),  0f,
                            hmdMat.m(2), hmdMat.m(6), hmdMat.m(10), 0f,
                            hmdMat.m(3), hmdMat.m(7), hmdMat.m(11), 1f);
    }
    
    public static HmdMatrix34 matrix4fToHmdMatrix34(Matrix4f mat) {
        HmdMatrix34 hmdMat = HmdMatrix34.create();
        float[] values = new float[12];
        values[0] = mat.m00;
        values[1] = mat.m10;
        values[2] = mat.m20;
        values[3] = mat.m30;
        values[4] = mat.m01;
        values[5] = mat.m11;
        values[6] = mat.m21;
        values[7] = mat.m31;
        values[8] = mat.m02;
        values[9] = mat.m12;
        values[10] = mat.m22;
        values[11] = mat.m32;
        hmdMat.m().put(values);
        return hmdMat;
    }
}
