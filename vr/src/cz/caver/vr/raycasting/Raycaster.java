/*

 */

package cz.caver.vr.raycasting;

import com.caversoft.core.math.geometry.utilities.Ray;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.pick.PickManager;
import cz.caver.renderer.pick.PickResult;
import cz.caver.vr.GUI.GUIPanel;
import cz.caver.vr.VirtualReality;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Raycaster {
    private static VirtualReality vr;
    private static PickManager pm = Lookup.getDefault().lookup(PickManager.class);
    private static RenderManager rm = Lookup.getDefault().lookup(RenderManager.class);
    
    private static RaycastHit lastHit = null;
    private static RaycastHit curHit = null;
    
    public Raycaster(VirtualReality vr) {
        Raycaster.vr = vr;
    }
    
    public RaycastHit getLastHit() {
        return lastHit;
    }
    
    public RaycastHit getCurHit() {
        return curHit;
    }
    
    public static RaycastHit doRaycast(Ray r) {
        curHit = null;
        RaycastHit hit = new RaycastHit();
        //Do raycast for structures
        hit = doStructuresRaycast(hit, r);
        //Do raycast for GUI
        hit = doGUIRaycast(hit, r);
        
        if(hit.isSuccess()) {
            lastHit = hit;
            curHit = hit;
        }
        return hit;
    }
    
    protected static RaycastHit doStructuresRaycast(RaycastHit hit, Ray r) {
        PickResult pick = pm.doRayPick(r, rm.getRenderableStructures(), false);
        hit.considerPickResult(pick);
        return hit;
    }
    
    protected static RaycastHit doGUIRaycast(RaycastHit hit, Ray r) {
        for(GUIPanel panel : vr.getGUIManager().getActivePanels()) {
            Ray panelRay = panel.getNormalRay(); //Ray with panel position as origin and normal as direction vector
            float distance = PickManager.linePlaneIntersection(r, panelRay);
            
            if(distance < hit.getDistance() && distance >= 0) {
                Vector4f intersection = new Vector4f(getPointAlongRay(r, distance));
                intersection.w = 1;
                //Transforming intersection point back into panel space
                Matrix4f model = new Matrix4f(panel.getTransform());
                model.invert();
                model.transform(intersection);
                
                //Get point from hogenous coordinates
                Vector3f intersectionPoint = new Vector3f(intersection.x / intersection.w, intersection.y / intersection.w, intersection.z / intersection.w);
                
                //Normalize point from quad <-1, 1> range to <0, 1> range
                intersectionPoint.add(new Vector3f(1, 1, 0));
                intersectionPoint.scale(0.5f);
                
                //If point is in range <0, 1>, fire click event
                if(intersectionPoint.x >= 0 && intersectionPoint.x <= 1 && intersectionPoint.y >= 0 && intersectionPoint.y <= 1) {
                    hit.setDistance(distance);
                    hit.setHitPanel(panel);
                    hit.setHitPosition(intersectionPoint);
                }
            }
        }
        return hit;
    }
    
    /**
     * Returns point with distance d from ray origin lying along direction of ray r
     * @param r Ray
     * @param d Distance
     * @return Point
     */
    public static Vector3f getPointAlongRay(Ray r, float d) {
        Vector3f point = new Vector3f(r.getOrigin());
        Vector3f direction = new Vector3f(r.getDirection());
        direction.normalize();
        direction.scale(d);
        point.add(direction);
        return point;
    }
}
