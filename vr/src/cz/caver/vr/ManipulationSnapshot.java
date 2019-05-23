package cz.caver.vr;

import cz.caver.renderer.renderable.RenderableStructure;
import cz.caver.vr.devices.Device;
import cz.caver.vr.raycasting.RaycastHit;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

/**
 * Manipulation snapshot wraps renderable structure and stores its transformation snapshot as well as device and device transformations for manipulation purposes
 * @author xkleteck
 */
public final class ManipulationSnapshot {
    private RenderableStructure structure;
    private RaycastHit hit;
    
    private Device device;
    private Vector3f devicePosition;
    private Quat4f deviceRotation;
    private Vector3f structPosition;
    private Matrix4f structRotation;
    private Matrix4f structScale;
    
    public ManipulationSnapshot(Device d, RenderableStructure structure, RaycastHit hit) {
        device = d;
        setStructure(structure);
        setHit(hit);
        setDevicePosition(d.getPose().getPosition());
        setDeviceRotation(d.getPose().getRotation());
        setStructPosition(structure.getTranslation());
        setStructRotation(structure.getRotation());
        setStructScale(structure.getScale());
    }

    public Device getDevice() {
        return device;
    }

    public void setDevice(Device device) {
        this.device = device;
    }
    
    public RenderableStructure getStructure() {
        return structure;
    }

    public void setStructure(RenderableStructure structure) {
        this.structure = structure;
    }

    public RaycastHit getHit() {
        return hit;
    }

    public void setHit(RaycastHit hit) {
        this.hit = hit;
    }

    public Vector3f getDevicePosition() {
        return devicePosition;
    }

    public void setDevicePosition(Vector3f devicePosition) {
        this.devicePosition = new Vector3f(devicePosition);
    }

    public Quat4f getDeviceRotation() {
        return deviceRotation;
    }

    public void setDeviceRotation(Quat4f devicePose) {
        this.deviceRotation = new Quat4f(devicePose);
    }

    public Vector3f getStructPosition() {
        return structPosition;
    }

    public void setStructPosition(Vector3f position) {
        this.structPosition = new Vector3f(position);
    }

    public Matrix4f getStructRotation() {
        return structRotation;
    }

    public void setStructRotation(Matrix4f rotation) {
        this.structRotation = new Matrix4f(rotation);
    }

    public Matrix4f getStructScale() {
        return structScale;
    }

    public void setStructScale(Matrix4f scale) {
        this.structScale = new Matrix4f(scale);
    } 
}