package cz.caver.vr.utils;

import javax.vecmath.Point3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Quad {
    public static final Point3f[] EYE_QUAD_POSITIONS = new Point3f[] {
        new Point3f(-1f, -1f, 0f),
        new Point3f( 1f, -1f, 0f),
        new Point3f( 1f,  1f, 0f),
        new Point3f(-1f,  1f, 0f),
        new Point3f( 0f, -1f, 0f),
        new Point3f( 0f, 1f, 0f)
    };
    public static final Point3f[] QUAD_POSITIONS = new Point3f[] {
        new Point3f(-1f, -1f, 0f),
        new Point3f(1f, -1f, 0f),
        new Point3f(1f,  1f, 0f),
        new Point3f(-1f,  1f, 0f)
    };
}