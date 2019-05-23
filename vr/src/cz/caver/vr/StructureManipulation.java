package cz.caver.vr;

import com.caversoft.log.Log;
import com.caversoft.structure.controller.StructureController;
import cz.caver.vr.devices.Device;
import cz.caver.vr.devices.DeviceButton;
import cz.caver.vr.raycasting.RaycastHit;
import java.util.HashMap;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class StructureManipulation {
    private static final Log LOG = new Log(StructureManipulation.class);
    public static VirtualReality vr;
    private static final float TRANSLATION_SENSITIVITY = 1f;
    private static final float ROTATION_SENSITIVITY = 3f;
    private static final float SCALE_SENSITIVITY = 1f;
    private static final HashMap<Device, ManipulationSnapshot> MANIPULATED_STRUCTURES = new HashMap<>();
    
    public static void beginManipulation(Device device, RaycastHit hit) {
        MANIPULATED_STRUCTURES.put(device, new ManipulationSnapshot(device, hit.getHitStructure(), hit));
    }
    
    public static void continueManipulation(Device device) {
        if (MANIPULATED_STRUCTURES.containsKey(device)) {
            ManipulationSnapshot snapshot = MANIPULATED_STRUCTURES.get(device);
            if (device.getButton(DeviceButton.STEAMVR_TOUCHPAD)) {
                switch (device.getTouchpadAxis()) {
                    case DOWN:
                        StructureManipulation.scale(snapshot);
                        break;
                    case LEFT:
                        StructureManipulation.rotate(snapshot);
                        break;
                    case RIGHT:
                        StructureManipulation.translate(snapshot);
                        break;
                    case UP:
                        //StructureManipulation.rotate(snapshot);
                        //StructureManipulation.scale(snapshot);
                        break;
                    default:
                        break;
                }
            } else if ((device.getButtonDown(DeviceButton.GRIP) && device.getButton(DeviceButton.STEAMVR_TRIGGER)) || (device.getButton(DeviceButton.GRIP) && device.getButtonDown(DeviceButton.STEAMVR_TRIGGER))) {
                LOG.debug("grip + trigger - remove");
                Lookup.getDefault().lookup(StructureController.class).removeStructure(snapshot.getStructure().getStructure());
                endManipulation(device);
            }
        }
    }
    
    public static void endManipulation(Device device) {
        if (MANIPULATED_STRUCTURES.containsKey(device)) {
            MANIPULATED_STRUCTURES.remove(device);
        }
    }
    
    public static boolean isManipulating() {
        return MANIPULATED_STRUCTURES.size() > 0;
    }

    /**
     * Translates given structure according the controller position.
     * 
     * @param devicePosition Controller position
     * @param rs structure
     */
    public static void translate(ManipulationSnapshot snapshot) {
        // get translation of the structure relative to device and translate it
        Vector3f hitPosition = snapshot.getHit().getHitPosition();
        Vector3f devicePosition = snapshot.getDevice().getPose().getPosition();
        Vector3f newPosition = new Vector3f(snapshot.getDevice().getPose().getDirection());
        newPosition.scale(snapshot.getHit().getDistance());
        newPosition.add(devicePosition);
        
        Vector3f position = new Vector3f(snapshot.getStructPosition());
        position.x += (newPosition.x - hitPosition.x) * TRANSLATION_SENSITIVITY;
        position.y += (newPosition.y - hitPosition.y) * TRANSLATION_SENSITIVITY;
        position.z += (newPosition.z - hitPosition.z) * TRANSLATION_SENSITIVITY;
        snapshot.getStructure().setTranslation(position);
    }
    
    /**
     * Rotates given structure according to the controller rotation.
     * 
     * @param rs Renderable structure
     * @param angularVelocity Angle changes since last update
     */
    public static void rotate(ManipulationSnapshot snapshot) {
        Vector3f structCenter = snapshot.getHit().getHitStructure().getTranslation();
        //structCenter.add(snapshot.getHit().getHitStructure().getCenteringTranslation());
        //Vector3f structCenter = snapshot.getHit().getHitPosition();
        Vector3f oldDevicePoint = snapshot.getDevicePosition();
        Vector3f curDevicePoint = new Vector3f(snapshot.getDevice().getPose().getPosition());
        
        Vector3f startVector = new Vector3f(oldDevicePoint);
        startVector.sub(structCenter);
        Vector3f endVector = new Vector3f(curDevicePoint);
        endVector.sub(structCenter);
        Vector3f normal = new Vector3f();
        normal.cross(endVector, startVector);
        normal.normalize();
        float angle = startVector.angle(endVector) * ROTATION_SENSITIVITY;
        
        Quat4f rotation = new Quat4f();
        rotation.set(new AxisAngle4f(normal, angle));
        
        Matrix4f finalRotation = new Matrix4f(snapshot.getStructRotation());
        finalRotation.mul(new Matrix4f(rotation, new Vector3f(), 1));
        snapshot.getStructure().setRotation(finalRotation);
        
        //Debug
        /*LineRenderer r = (LineRenderer) vr.getRenderer(LineRenderer.class);
        r.bindRenderBuffer(vr.getRenderbuffer());
        r.renderPointer(oldDevicePoint, curDevicePoint);
        r.renderPointer(structCenter, curDevicePoint);
        r.renderPointer(structCenter, oldDevicePoint);
        Vector3f normalPt = new Vector3f(structCenter);
        normalPt.add(normal);
        normalPt.add(normal);
        normalPt.add(normal);
        normalPt.add(normal);
        normalPt.add(normal);
        r.renderPointer(structCenter, normalPt);
        r.unbindRenderBuffer();*/
        
        //Matrix4f rsRot = rs.getRotation();
        /*float[] angles = getRotation(rsRot);
        Matrix3f rotMat = new Matrix3f();
        rotMat.rotX(angles[0] + angularVelocity.x * ROTATION_SENSITIVITY);
        rotMat.rotY(angles[1] + angularVelocity.y * ROTATION_SENSITIVITY);
        rotMat.rotZ(angles[2] + angularVelocity.z * ROTATION_SENSITIVITY);
        rsRot.setRotation(rotMat);
        rs.setRotation(rsRot);*/
        /*
        Quat4f rotation = new Quat4f();
        rotation.set(rsRot);
        
        Matrix3f velocityR = new Matrix3f();
        
        Quat4f velocityRot = Quaternion.anglesToQuaternion(angularVelocity);
                
        rotation.mul(velocityRot);
        
        rs.setRotation(new Matrix4f(rotation, new Vector3f(), 1));*/
        // calculate change of rotation since previous update
        /*
        Quat4f relativeRotation = new Quat4f(deviceRotation);
        Quat4f previous = new Quat4f(previousControllerRotation);
        previous.inverse();
        relativeRotation.mul(previous);
        
        // get rotation of the structure relative to device and rotate it
        Quat4f localRotation = new Quat4f();
        localRotation.set(rs.getRotation());
        localRotation.normalize();
        Quat4f rotation = new Quat4f(deviceRotation);
        rotation.inverse();
        localRotation.mul(rotation);
        localRotation.mul(relativeRotation);
        
        // set world rotation of the structure
        Quat4f worldRotation = new Quat4f();
        worldRotation.mul(localRotation, deviceRotation); 
        rs.setRotation(new Matrix4f(worldRotation, new Vector3f(), 1f));*/
    }
    

    /**
     * Scales given structure according the distance between the controller and the structure center.
     * @param rs Renderable structure
     * @param position Controller position
     * @param position Controller velocity
     */
    public static void scale(ManipulationSnapshot snapshot) {
        Vector3f hitPosition = new Vector3f(snapshot.getHit().getHitPosition());
        Vector3f devicePosition = new Vector3f(snapshot.getDevice().getPose().getPosition());
        Vector3f newPosition = new Vector3f(snapshot.getDevice().getPose().getDirection());
        newPosition.normalize();
        newPosition.scale(snapshot.getHit().getDistance());
        newPosition.add(devicePosition);
        
        float factor = newPosition.length() / hitPosition.length() * SCALE_SENSITIVITY;

        snapshot.getStructure().setScale(factor * snapshot.getStructScale().m00);
    }
}
