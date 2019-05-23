package cz.caver.vr.GUI;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 *
 * @author xkleteck
 */
public abstract class Transform2D {
    private final Matrix4f transform = new Matrix4f();
    private final Vector2f translation = new Vector2f();
    private float rotation;
    private final Vector2f scale = new Vector2f();

    private void updateTransform() {
        transform.setIdentity();
        AxisAngle4f aa = new AxisAngle4f(0, 0, 1, rotation);
        transform.set(aa);
        transform.m00 = transform.m00 * scale.x;
        transform.m11 = transform.m11 * scale.y;
        transform.m03 = translation.x;
        transform.m13 = translation.y;
    }
    
    public final Matrix4f getTransform() {
        return transform;
    }
    
    public final Vector2f getTranslation() {
        return translation;
    }

    public void setTranslation(float x, float y) {
        this.translation.x = x;
        this.translation.y = y;
        updateTransform();
    }

    public final float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
        updateTransform();
    }

    public final Vector2f getScale() {
        return scale;
    }

    public void setScale(float x, float y) {
        this.scale.x = x;
        this.scale.y = y;
        updateTransform();
    }
}