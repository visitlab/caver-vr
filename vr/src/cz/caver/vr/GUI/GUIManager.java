package cz.caver.vr.GUI;

import com.caversoft.log.Log;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.vr.Eye;
import cz.caver.vr.IVRComponent;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.overlays.impl.Dashboard;
import cz.caver.vr.GUI.walls.impl.DynamicsWall;
import cz.caver.vr.GUI.walls.impl.InfoWall;
import cz.caver.vr.GUI.walls.impl.StructureWall;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VREvent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
@ServiceProvider(service = GUIManager.class)
public class GUIManager implements IVRComponent {
    private static final Log LOG = new Log(GUIManager.class);
    
    private static VirtualReality vr;
    
    private final HashMap<String, GUIPanel> panels = new HashMap<>();
    private final List<GUIPanel> activePanels = new LinkedList<>();
    private IVROverlay dashboardOverlay;
    
    private boolean initialized = false;
    private boolean active = true;
    
    @Override
    public IVRComponent init(VirtualReality vr) {
        GUIManager.vr = vr;
        //TODO uncomment in production phase
        IntBuffer errorBuffer = GLBuffers.newDirectIntBuffer(1);
        VR.VR_GetGenericInterface(VR.IVROverlay_Version, errorBuffer);
        if (errorBuffer.get(0) != VR.EVROverlayError_VROverlayError_None) {
            LOG.error("Overlay initialization failed - " + VR.VR_GetVRInitErrorAsEnglishDescription(errorBuffer.get(0)));
            return this;
        }
        addDashboardOverlay(new Dashboard(vr), false);
        addGUIPanel(new StructureWall(vr), true);
        addGUIPanel(new DynamicsWall(vr), true);
        addGUIPanel(new InfoWall(vr), true);
        
        initialized = true;
        return this;
    }
    
    @Override
    public boolean isInitialized() {
        return initialized;
    }
    
    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public VirtualReality getVR() {
        return vr;
    }
    
    public boolean contains(String name) {
        return panels.containsKey(name);
    }

    public boolean addOverlay(GUIPanel overlay, boolean shown) {
        if(!(overlay instanceof IVROverlay)) {
            LOG.debug("Panel " + overlay.getName() + " is not instance of an overlay");
            return false;
        }
        if(contains(overlay.getName())) { 
            LOG.debug("Overlay " + overlay.getName() + " is already present in GUIManager");
            return false;
        }
        panels.put(overlay.getName(), overlay);
        if(shown) {
            showGUIPanel(overlay);
        }
        return true;
    }
    
    public boolean addDashboardOverlay(GUIPanel overlay, boolean shown) {
        if(!(overlay instanceof IVROverlay)) {
            LOG.debug("Panel " + overlay.getName() + " is not instance of an overlay");
            return false;
        }
        if(dashboardOverlay != null) {
            LOG.debug("The dashboard overlay is already assigned");
            return false;
        }
        dashboardOverlay = (IVROverlay)overlay;
        panels.put("Dashboard", overlay);
        if(shown){
            showGUIPanel(overlay);
        }
        return true;
    }
    
    public boolean addGUIPanel(GUIPanel panel, boolean shown) {
        if(contains(panel.getName())) {
            LOG.debug("Panel " + panel.getName() + " is already present in GUIManager");
            return false;
        }
        panels.put(panel.getName(), panel);
        if(shown) {
            showGUIPanel(panel);
        }
        return true;
    }
    
    public List<GUIPanel> getActivePanels() {
        return activePanels;
    }
    
    @Override
    public void dispose(VirtualReality vr) {}

    @Override
    public void onFrameBegin(Eye eye) {}

    @Override
    public boolean isListeningVREvents() {
        return true;
    }

    @Override
    public void processEvent(VREvent event) {
        for (GUIPanel o : activePanels) {
            if(o.isListeningVREvents()){
                o.processEvent(event);
            }
        }
    
    }

    public void processEvents() {
        if(initialized) {
            //Process events of overlays
            for (GUIPanel o : activePanels) {
                o.processEvents();
            }
        }
    }
    
    @Override
    public boolean doesSceneRendering() {
        return true;
    }
    
    @Override
    public void renderScene(VirtualReality vr, Eye eye) {
        for(GUIPanel p : activePanels) {
            p.renderScene(vr, eye);
        }
    }
    
    @Override
    public boolean doesGeneralRendering() {
        return true;
    }
    
    @Override
    public void renderGeneral(VirtualReality vr, Eye eye) {
        if(initialized) {
            for (IVRGUIPanel o : activePanels) {
                o.renderGeneral(vr, eye);
            }
        }
    }
    
    public IVRGUIPanel getOverlay(String name) {
        if(contains(name)) {
            return panels.get(name);
        }
        return null;
    }
    
    public boolean toggleOverlay(String name) {
        if(contains(name)) {
            GUIPanel o = panels.get(name);
            if(o.isVRVisible()) {
                o.hide();
                activePanels.remove(o);
            } else {
                o.show();
                activePanels.add(o);
            }
            return true;
        }
        return false;
    }
    
    public boolean showGUIPanel(String name) {
        if(contains(name)) {
            GUIPanel o = panels.get(name);
            o.show();
            activePanels.add(o);
            return true;
        }
        return false;
    }
    
    public boolean showGUIPanel(GUIPanel panel) {
        if(panels.containsValue(panel)) {
            panel.show();
            activePanels.add(panel);
            return true;
        }
        return false;
    }
    
    public boolean hideGUIPanel(String name) {
        if(contains(name)) {
            GUIPanel o = panels.get(name);
            if(o.isVRVisible()) {
                o.hide();
                activePanels.remove(o);
            }
            return true;
        }
        return false;
    }
    
    public boolean hideGUIPanel(GUIPanel panel) {
        if(panels.containsValue(panel)) {
            panel.show();
            activePanels.add(panel);
            return true;
        }
        return false;
    }

    @Override
    public void onFrameEnd(Eye eye) {}
}
