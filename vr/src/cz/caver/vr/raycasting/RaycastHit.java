package cz.caver.vr.raycasting;

import com.caversoft.structure.primary.Atom;
import com.caversoft.tunnel.model.Tunnel;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.pick.PickResult;
import cz.caver.renderer.renderable.RenderableStructure;
import javax.vecmath.Vector3f;
import org.openide.util.Lookup;
import cz.caver.vr.GUI.IVRGUIPanel;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class RaycastHit {
    private static RenderManager rm = Lookup.getDefault().lookup(RenderManager.class);
    private RenderableStructure hitStructure = null;
    private PickResult pickResultObject = null;
    private IVRGUIPanel hitPanel = null;
    
    //TODO - compute precise hit position on structure, currently the center of an atom or tunnel sphere is used
    private Vector3f hitPosition = null;
    private float hitPrecision = Float.POSITIVE_INFINITY; //Radius where the hit could happen
    private float distance = Float.POSITIVE_INFINITY;
    private boolean success = false;
    
    public RenderableStructure getHitStructure() {
        return hitStructure;
    }

    public IVRGUIPanel getHitPanel() {
        return hitPanel;
    }

    public Vector3f getHitPosition() {
        return hitPosition;
    }

    public float getDistance() {
        return distance;
    }

    public boolean isSuccess() {
        return success;
    }

    protected void setHitStructure(RenderableStructure hitStructure) {
        this.hitStructure = hitStructure;
        this.hitPanel = null;
        if(hitStructure != null) {
            this.success = true;
        }
    }

    protected void setHitPanel(IVRGUIPanel hitPanel) {
        this.hitPanel = hitPanel;
        this.hitStructure = null;
        if(hitPanel != null) {
            this.success = true;
        }
    }

    protected void setHitPosition(Vector3f hitPosition) {
        this.hitPosition = hitPosition;
    }

    protected void setDistance(float distance) {
        this.distance = Math.abs(distance);
    }

    protected void setSuccess(boolean success) {
        this.success = success;
    }
    
    protected void considerPickResult(PickResult r) {
        if(r != null) {
            if(r.getType() != PickResult.NOTHING && r.getDistance() < distance && r.getDistance() >= 0) {
                switch(r.getType()) {
                    case PickResult.ATOM:
                        Atom a = (Atom)r.getItem();
                        hitStructure = rm.getRenderableStructure(a.getStructure());
                        break;
                    case PickResult.TUNNEL:
                        Tunnel t = (Tunnel)r.getItem();
                        hitStructure = rm.getRenderableStructure(t.getStructure());
                        break;
                }
                hitPosition = new Vector3f(r.getPickSphereCenter());
                hitPrecision = r.getPickSphereRadius();
                distance = r.getDistance();
                pickResultObject = r;
                success = true;
            }
        
        }
    }
}
