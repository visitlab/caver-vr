package cz.caver.vr.devices;

import com.caversoft.color.Color;
import com.caversoft.log.Log;
import com.caversoft.structure.controller.StructureController;
import cz.caver.controller.selection.SelectionController;
import cz.caver.renderer.renderable.RenderableStructure;
import cz.caver.vr.StructureManipulation;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.raycasting.RaycastHit;
import cz.caver.vr.raycasting.Raycaster;
import cz.caver.vr.rendering.PrimitivesRenderer;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Controller extends Device {
    private static final Log LOG = new Log(Controller.class);
    private final StructureController structController = Lookup.getDefault().lookup(StructureController.class);
    private final SelectionController selectionController = Lookup.getDefault().lookup(SelectionController.class);
    private RenderableStructure activeStructure;
    private RaycastHit lastHit;
    
    public Controller(int index, VirtualReality vr) {
        super(index, vr);
    }
    
    public RaycastHit getLastHit() {
        return lastHit;
    }

    /**
     * Sets last hit and fires GUI panel events if some panel was hit previously or is hit now
     * @param hit 
     */
    public void panelInteraction(RaycastHit hit) {
        if(hit == null) {
            LOG.error("Last hit can not be null");
            return;
        }
        if(hit.getHitPanel() != null) {
            if(lastHit == null) {
                hit.getHitPanel().onPointerEnter(new Vector2f(lastHit.getHitPosition().x, lastHit.getHitPosition().y));
            } else {
                if(lastHit.getHitPanel() != hit.getHitPanel()) {
                    if(lastHit.getHitPanel() != null) {
                        lastHit.getHitPanel().onPointerLeave(new Vector2f(lastHit.getHitPosition().x, lastHit.getHitPosition().y));
                    }
                    
                    if(hit.getHitPanel() != null) {
                        hit.getHitPanel().onPointerEnter(new Vector2f(hit.getHitPosition().x, hit.getHitPosition().y));
                    }
                } else if(lastHit.getHitPanel() == hit.getHitPanel()) {
                    hit.getHitPanel().onPointerMove(new Vector2f(hit.getHitPosition().x, hit.getHitPosition().y));
                }
            }
        }
        
        lastHit = hit;
        if(someButtonDown) {
            if(lastHit.getHitPanel() != null) {
                lastHit.getHitPanel().onPointerClick(new Vector2f(lastHit.getHitPosition().x, lastHit.getHitPosition().y));
            }
        }
    }
    
    @Override
    protected void interact() {
        if(!disabled) {
            RaycastHit hit = Raycaster.doRaycast(getPose().getRay());
            if(activeStructure == null) {
                panelInteraction(hit);
                if(hit.isSuccess()) {
                    if(isButtonDown() && hit.getHitStructure() != null) {
                        if(getButtonDown(DeviceButton.STEAMVR_TRIGGER)) {
                            //structController.clearActiveStructures();
                            structController.setActiveStructure(hit.getHitStructure().getStructure());
                            selectionController.clear();
                            selectionController.select(hit.getHitStructure().getStructure());
                        } else {
                            activeStructure = hit.getHitStructure();
                            StructureManipulation.beginManipulation(this, hit);
                        }
                    }
                }
            } else {
                if(isButton()) {
                    StructureManipulation.continueManipulation(this);
                } else {
                    StructureManipulation.endManipulation(this);
                    activeStructure = null;
                }
            }
            
            if(getButtonDown(DeviceButton.STEAMVR_TRIGGER) && hit.getHitStructure() == null && hit.getHitPanel() == null) {
                structController.clearActiveStructures();
                selectionController.clear();
            }
        }
    }
    
    @Override
    public void renderPointer(){
        if(renderModel != null && deviceClass != DeviceClass.INVALID && !disabled) {
            //Render pointer
            Vector3f pointerStart = pose.getPosition();
            Vector3f pointerEnd = new Vector3f(pose.getDirection());
            if(lastHit != null && lastHit.isSuccess()) {
                pointerEnd.scale(lastHit.getDistance());
            } else {
                pointerEnd.scale(5);
            }
            pointerEnd.add(pointerStart);
            PrimitivesRenderer lr = (PrimitivesRenderer) vr.getRenderer(PrimitivesRenderer.class);
            lr.renderLine(pointerStart, pointerEnd, Color.BLUE);
        }
    }
}
