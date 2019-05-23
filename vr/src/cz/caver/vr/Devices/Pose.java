package cz.caver.vr.devices;

import com.caversoft.core.math.geometry.utilities.Ray;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.TrackedDevicePose;
import org.lwjgl.openvr.VR;

/**
* Represents the pose of a {@link Device}, including its
* transform, velocity and angular velocity. Also indicates
* whether the pose is valid.
* @author Jiri Kletecka <433728@mail.muni.cz>
*/
public class Pose {
    // Link to the device
    private final Device device;
    // Transform encoding the position and rotation of the device in tracker space
    protected final Matrix4f transform;
    // The velocity in m/s in tracker space space
    protected final Vector3f velocity;
    // The angular velocity in radians/s in tracker space
    protected final Vector3f angularVelocity;
    // Whether the pose is valid our invalid, e.g. outdated because of tracking failure
    protected boolean valid;

    /* Computed */
    protected final Quat4f rotation;
    
    protected final Vector3f position;
    
    protected final Vector3f direction;

    public Pose(Device device) {
        this.device = device;
        
        transform = new Matrix4f();
        transform.setIdentity();
        velocity = new Vector3f();
        velocity.set(0, 0, 0);
        angularVelocity = new Vector3f();
        angularVelocity.set(0, 0, 0);
        valid = false;
        rotation = new Quat4f();
        rotation.set(0, 0, 0, 0);
        position = new Vector3f();
        position.set(0, 0, 0);
        direction = new Vector3f();
        direction.set(0, 0, -1);
    }

    public Device getDevice() {
        return device;
    }

    public Matrix4f getTransform() {
        return transform;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getAngularVelocity() {
        return angularVelocity;
    }
    
    public Vector3f getPosition() {
        return position;
    }
    
    public Quat4f getRotation() {
        return rotation;
    }
    
    public Vector3f getDirection() {
        return direction;
    }
    
    public Ray getRay() {
        return new Ray(position, direction);
    }

    public boolean isValid() {
        return valid;
    }
    
    protected void refresh(TrackedDevicePose trackedPose) {
        valid = trackedPose.bPoseIsValid();
        if(valid) {
            fillTransformMatrix(trackedPose.mDeviceToAbsoluteTracking(), transform);
            
            //transform.transpose();
            velocity.set(trackedPose.vVelocity().v(0), trackedPose.vVelocity().v(1), trackedPose.vVelocity().v(2));
            angularVelocity.set(trackedPose.vAngularVelocity().v(0), trackedPose.vAngularVelocity().v(1), trackedPose.vAngularVelocity().v(2));
            
            position.set(transform.m30, transform.m31, transform.m32);
            rotation.set(transform);
            direction.x = -transform.m20;
            direction.y = -transform.m21;
            direction.z = -transform.m22;
            direction.normalize();
        }
    }
    
    private static void fillTransformMatrix(HmdMatrix34 hmd, Matrix4f mat) {
        mat.setIdentity();
        mat.m00 = hmd.m(0);
        mat.m01 = hmd.m(4);
        mat.m02 = hmd.m(8);
        
        mat.m10 = hmd.m(1);
        mat.m11 = hmd.m(5);
        mat.m12 = hmd.m(9);
        
        mat.m20 = hmd.m(2);
        mat.m21 = hmd.m(6);
        mat.m22 = hmd.m(10);
        
        mat.m30 = hmd.m(3);
        mat.m31 = hmd.m(7);
        mat.m32 = hmd.m(11);
    }
    
    /**
     * Helper function which returns one of HMD pose matrices when testing without HMD.
     */
    public static Matrix4f getDefaultHmdPose() {
        Matrix4f mat1 = new Matrix4f(0.0186513f, -0.17176092f, -0.98496205f, -0.0f, 
                                     0.027297545f, 0.9848537f, -0.1712251f, 0.0f, 
                                     0.9994533f, -0.023693478f, 0.023057448f, 0.0f, 
                                     -0.2727458f, -1.2373263f, -2.5561297f, 1.0f);
        
        Matrix4f mat2 = new Matrix4f(-0.9941916f, -0.07908176f, -0.073001236f, -0.0f, 
                                     -0.0019223425f, 0.6912339f, -0.7226286f, -0.0f, 
                                     0.10760765f, -0.7182909f, -0.68737096f, -0.0f, 
                                     0.12717973f, -1.444781f, -0.60729617f, 1.0f);
        
        Matrix4f mat3 = new Matrix4f(0.9999952f, -0.0022490618f, -0.0021687492f, 0.0f, 
                                     0.0018774845f, 0.98739547f, -0.15826146f, 0.0f, 
                                     0.0024973624f, 0.15825659f, 0.9873949f, 0.0f, 
                                     0.35680336f, -1.309262f, -2.848488f, 1.0f);
        
        Matrix4f mat4 = new Matrix4f(-0.021320572f, 0.18249051f, 0.9829765f, 0.0f, 
                                     -0.07591744f, 0.9800657f, -0.18359676f, 0.0f, 
                                     -0.9968862f, -0.07853944f, -0.007041351f, -0.0f, 
                                     0.059826266f, -0.9094609f, -1.656485f, 1.0f);
        
        Matrix4f mat5 = new Matrix4f(-0.99060214f, -0.12887134f, 0.045821134f, -0.0f, 
                                     -0.03069448f, -0.11700515f, -0.9926569f, -0.0f, 
                                     0.13328627f, -0.9847345f, 0.11194986f, -0.0f, 
                                     0.018854324f, -0.021962577f, 0.05910499f, 1.0f);
        
        Matrix4f mat6 = new Matrix4f(0.9987013f, -0.01730576f, -0.047918025f, 0.0f, 
                                     0.04918013f, 0.08190365f, 0.99542606f, 0.0f, 
                                     -0.013301956f, -0.99649f, 0.08264838f, -0.0f, 
                                     -0.11500124f, -0.7005662f, -0.54888636f, 1.0f);
        return mat1;
        //hmdPose = mat1;
    }
}
