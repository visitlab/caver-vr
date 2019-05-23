package cz.caver.vr;

import com.caversoft.color.Color;
import com.caversoft.log.Log;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.vr.rendering.PrimitivesRenderer;
import java.nio.FloatBuffer;
import javax.vecmath.Vector3f;
import org.lwjgl.openvr.HmdQuad;
import org.lwjgl.openvr.HmdVector3;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRChaperone;
import org.lwjgl.openvr.VREvent;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author xkleteck
 */
@ServiceProvider(service = Playground.class)
public class Playground implements IVRComponent {
    private static final Log LOG = new Log(Playground.class);
    private static VirtualReality vr;
    
    private static final float HEIGHT = 3;
    
    private Vector3f maxCorner;
    private Vector3f minCorner;
    private final Vector3f size = new Vector3f(new float[]{6.0f, HEIGHT, 6.0f});
    
    private boolean initialized = false;
    private boolean active = true;
    
    @Override
    public IVRComponent init(VirtualReality vr) {
        this.vr = vr;
        initPlayground();
        return this;
    }
    
    private void initPlayground() {
        try {
            //Reset seated position to zero
            //VRSystem.VRSystem_ResetSeatedZeroPose();
            
            //Get playground corners plus width and depth
            HmdQuad sceneRect = HmdQuad.create();
            VRChaperone.VRChaperone_GetPlayAreaRect(sceneRect);
            minCorner = new Vector3f(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
            maxCorner = new Vector3f(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);

            for(int i = 0; i < 4; i++) {
                HmdVector3 cornerVec = sceneRect.vCorners(i);

                float val = cornerVec.v(0);
                minCorner.x = Math.min(minCorner.x, val);
                maxCorner.x = Math.max(maxCorner.x, val);
                val = cornerVec.v(1);
                minCorner.y = Math.min(minCorner.y, val);
                maxCorner.y = Math.max(maxCorner.y, val);
                val = cornerVec.v(2);
                minCorner.z = Math.min(minCorner.z, val);
                maxCorner.z = Math.max(maxCorner.z, val);
            }
            maxCorner.y = Math.max(maxCorner.y, HEIGHT);

            FloatBuffer xSizeBuffer = GLBuffers.newDirectFloatBuffer(1);
            FloatBuffer zSizeBuffer = GLBuffers.newDirectFloatBuffer(1);
            VRChaperone.VRChaperone_GetPlayAreaSize(xSizeBuffer, zSizeBuffer);

            size.x = xSizeBuffer.get(0) == 0 ? size.x : xSizeBuffer.get(0);
            size.y = maxCorner.y;
            size.z = zSizeBuffer.get(0) == 0 ? size.z : zSizeBuffer.get(0);
            
            initialized = true;
        } catch(Exception e) {
            LOG.debug("Chaperone play area can't be obtained.");
        }
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
    public void dispose(VirtualReality vr) {}

    @Override
    public VirtualReality getVR() {
        return vr;
    }

    @Override
    public void onFrameBegin(Eye eye) {}

    @Override
    public boolean isListeningVREvents() {
        return true;
    }

    @Override
    public void processEvent(VREvent event) {
        switch (event.eventType()) {
            case VR.EVREventType_VREvent_ChaperoneSettingsHaveChanged:
            case VR.EVREventType_VREvent_ChaperoneUniverseHasChanged:
                initPlayground();
                break;
        }
    }

    @Override
    public boolean doesGeneralRendering() {
        return false;
    }

    @Override
    public void renderGeneral(VirtualReality vr, Eye eye) {}

    @Override
    public boolean doesSceneRendering() {
        return true;
    }

    @Override
    public void renderScene(VirtualReality vr, Eye eye) {
        PrimitivesRenderer renderer = (PrimitivesRenderer) vr.getRenderer(PrimitivesRenderer.class);
        /*renderer.bindRenderBuffer(vr.getRenderbuffer());
        renderer.renderGizmoCube(new Vector3f(1.8f, 0.1f, -1.9f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(1.8f, 2.2f, -1.9f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(1.8f, 0.1f, 1.6f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(1.8f, 2.2f, 1.6f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(-2.0f, 0.1f, 1.6f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(-2.0f, 2.2f, 1.6f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(-2.0f, 0.1f, -1.9f), 0.1f);
        renderer.renderGizmoCube(new Vector3f(-2.0f, 2.2f, -1.9f), 0.1f);*/
        
        //Floor
        renderer.renderCube(new Vector3f(0f, -0.1f, 0f), new Vector3f(2f, 0.1f, 2f), Color.GRAY);
        
        //Debug axes
        //renderer.renderDebugAxes(new Vector3f(), 5.0f);
        renderer.unbindRenderBuffer();
    }

    @Override
    public void onFrameEnd(Eye eye) {}

    public Vector3f getMaxCorner() {
        if(!initialized) {
            initPlayground();
        }
        return maxCorner;
    }

    public Vector3f getMinCorner() {
        if(!initialized) {
            initPlayground();
        }
        return minCorner;
    }

    public float getXSize() {
        if(!initialized) {
            initPlayground();
        }
        return size.x;
    }
    
    public float getYSize() {
        if(!initialized) {
            initPlayground();
        }
        return size.y;
    }

    public float getZSize() {
        if(!initialized) {
            initPlayground();
        }
        return size.z;
    }
    
    public Vector3f getSize() {
        if(!initialized) {
            initPlayground();
        }
        return size;
    }
}
