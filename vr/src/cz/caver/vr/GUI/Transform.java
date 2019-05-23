package cz.caver.vr.GUI;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

/**
 *
 * @author xkleteck
 */
public abstract class Transform {
    private final Matrix4f transform = new Matrix4f();
    private final Vector3f translation = new Vector3f();
    private final Matrix3f rotation = new Matrix3f();
    private final Matrix3f rotationScale = new Matrix3f();
    private final Vector3f scale = new Vector3f();

    private void updateTransform() {
        transform.setIdentity();        
        
        //Scale
        transform.m00 = transform.m00 * scale.x;
        transform.m11 = transform.m11 * scale.y;
        transform.m22 = transform.m22 * scale.z;
        
        //Rotate
        Matrix4f rotationMatrix = new Matrix4f(rotation, new Vector3f(), 1);
        rotationMatrix.mul(transform);
        transform.set(rotationMatrix);
        
        //Translate
        transform.m03 = translation.x;
        transform.m13 = translation.y;
        transform.m23 = translation.z;
    }
    
    public final Matrix4f getTransform() {
        return transform;
    }

    public final void setTransform(Matrix4f transform) {
        this.transform.set(transform);
        this.transform.get(translation);
        this.transform.get(rotation);
        this.transform.getRotationScale(rotationScale);
        this.scale.set(rotationScale.m00 / rotation.m00, rotationScale.m11 / rotation.m11, rotationScale.m22 / rotation.m22);
    }
    
    public final Vector3f getTranslation() {
        return translation;
    }

    public final void setTranslation(Vector3f translation) {
        this.translation.set(translation);
        updateTransform();
    }

    public final Matrix3f getRotation() {
        return rotation;
    }

    public final void setRotation(Matrix3f rotation) {
        this.rotation.set(rotation);
        updateTransform();
    }

    public final Vector3f getScale() {
        return scale;
    }

    public final void setScale(Vector3f scale) {
        this.scale.set(scale);
        updateTransform();
    }
}