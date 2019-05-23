package cz.caver.vr.utils;

import javax.vecmath.Point3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Cube {
    public static final Point3f[] CUBE_TRIANGLES = new Point3f[] {
        //  x
        new Point3f( 1f,  1f,  1f),
        new Point3f( 1f, -1f,  1f),
        new Point3f( 1f, -1f, -1f),
        
        new Point3f( 1f,  1f,  1f),
        new Point3f( 1f, -1f, -1f),
        new Point3f( 1f,  1f, -1f),
        //  y
        new Point3f( 1f,  1f,  1f),
        new Point3f(-1f,  1f, -1f),
        new Point3f(-1f,  1f,  1f),
          
        new Point3f( 1f,  1f,  1f),
        new Point3f( 1f,  1f, -1f),
        new Point3f(-1f,  1f, -1f),
        //  z
        new Point3f( 1f,  1f,  1f),
        new Point3f(-1f,  1f,  1f),
        new Point3f( 1f, -1f,  1f),
          
        new Point3f(-1f,  1f,  1f),
        new Point3f(-1f, -1f,  1f),
        new Point3f( 1f, -1f,  1f),
        // -x
        new Point3f(-1f, -1f, -1f),
        new Point3f(-1f,  1f,  1f),
        new Point3f(-1f,  1f, -1f),
        
        new Point3f(-1f,  1f,  1f),
        new Point3f(-1f, -1f, -1f),
        new Point3f(-1f, -1f,  1f),
        // -y
        new Point3f( 1f, -1f,  1f),
        new Point3f(-1f, -1f, -1f),
        new Point3f( 1f, -1f, -1f),
          
        new Point3f( 1f, -1f,  1f),
        new Point3f(-1f, -1f,  1f),
        new Point3f(-1f, -1f, -1f),
        // -z
        new Point3f(-1f, -1f, -1f),
        new Point3f(-1f,  1f, -1f),
        new Point3f( 1f,  1f, -1f),
          
        new Point3f(-1f, -1f, -1f),
        new Point3f( 1f,  1f, -1f),
        new Point3f( 1f, -1f, -1f)
    };
}
