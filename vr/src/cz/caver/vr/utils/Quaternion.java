package cz.caver.vr.utils;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author xkleteck
 */
public class Quaternion {
    public static Quat4f anglesToQuaternion(Vector3f angles) {
        float cr = (float) Math.cos(angles.x * 0.5);
        float sr = (float) Math.sin(angles.x * 0.5);
        float cp = (float) Math.cos(angles.y * 0.5);
        float sp = (float) Math.sin(angles.y * 0.5);
        float cy = (float) Math.cos(angles.z * 0.5);
        float sy = (float) Math.sin(angles.z * 0.5);

        Quat4f q = new Quat4f();
        q.setX(cy * cp * sr - sy * sp * cr);
        q.setY(sy * cp * sr + cy * sp * cr);
        q.setZ(sy * cp * cr - cy * sp * sr);
        q.setW(cy * cp * cr + sy * sp * sr);
        
        return q;
    }
    
}
