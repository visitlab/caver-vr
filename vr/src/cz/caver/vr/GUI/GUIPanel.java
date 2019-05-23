package cz.caver.vr.GUI;

import com.caversoft.core.math.geometry.utilities.Ray;
import com.caversoft.core.vecmath.Vector2i;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.renderer.pipeline.Framebuffer;
import cz.caver.vr.Eye;
import cz.caver.vr.GUI.buttons.impl.LockButton;
import cz.caver.vr.IVRComponent;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.overlays.KeyboardInputMode;
import cz.caver.vr.GUI.overlays.KeyboardLineInputMode;
import cz.caver.vr.GUI.elements.UIElement;
import cz.caver.vr.devices.DeviceManager;
import cz.caver.vr.rendering.QuadRenderer;
import cz.caver.vr.rendering.UIRenderer;
import static cz.caver.vr.utils.Utils.getBufferFromString;
import static cz.caver.vr.utils.Utils.getStringFromBuffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VROverlay;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public abstract class GUIPanel extends Transform implements IVRGUIPanel, IVRComponent {
    private static final Log LOG = new Log(GUIPanel.class);
    private static final DeviceManager dm = Lookup.getDefault().lookup(DeviceManager.class);
    
    protected static VirtualReality vr;
    
    protected String key;
    protected String name;
    
    protected boolean isVisible = false;
    protected boolean isInitialized = false;
    protected boolean isKeyboardVisible = false;
    protected boolean isLocked = false;
    
    //Handle to VR Overlay for keyboard purposes
    protected long handle = VR.k_ulOverlayHandleInvalid;
    protected IVRKeyboardCallback keyboardCallback = null;
    protected int lastKeyboardCharacters = 1;
    protected String keyBoardText = "";
    
    protected final List<UIElement> uiElements = new ArrayList<>();
    
    protected Framebuffer renderFbo;
    
    protected float sceneWidth = 0;
    protected float sceneHeight = 0;
    protected float aspect;
    
    protected final List<Vector3f> points = new LinkedList<>();
    
    public GUIPanel(VirtualReality vr) {
        GUIPanel.vr = vr;
    }

    @Override
    public IVRComponent init(VirtualReality vr) {
        dispose(vr);
        
        GL2GL3 gl = vr.getGL();
        renderFbo = Framebuffer.newColorFramebuffer(gl, getRenderTextureWidth(), getRenderTextureHeight(), Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8);
        gl.glBindTexture(GL.GL_TEXTURE_2D, renderFbo.getColorAttachment(0).getAttachmentObject());
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, getRenderTextureWidth(), getRenderTextureHeight(), 0, GL.GL_BGRA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, null);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_BASE_LEVEL, 0);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
        
        ByteBuffer keyBuffer, nameBuffer;
        LongBuffer overlayHandle = GLBuffers.newDirectLongBuffer(1);
        try {
            keyBuffer = getBufferFromString(key);
            nameBuffer = getBufferFromString(name);
            int error = VROverlay.VROverlay_CreateOverlay(keyBuffer, nameBuffer, overlayHandle);
            if (error == VR.EVROverlayError_VROverlayError_None) {
                handle = overlayHandle.get(0);
                if(handle != VR.k_ulOverlayHandleInvalid) {
                    isInitialized = true;
                }
            }
        } catch(CharacterCodingException e) {}
        
        LockButton lock = new LockButton(this);
        lock.setLeftTopOffset(getRenderTextureWidth() - 60, 10);
        lock.setWidth(50);
        lock.setHeight(50);
        uiElements.add(lock);
        
        int[] viewport = {0, 0, getRenderTextureWidth(), getRenderTextureHeight()};
        UIRenderer renderer = (UIRenderer)vr.getRenderer(UIRenderer.class);
        renderer.bindRenderBuffer(renderFbo);
        renderer.renderPanel(this, viewport, true);
        renderer.unbindRenderBuffer();
        return this;
    }
    
    private void handleError(int error) {
        if (error != VR.EVROverlayError_VROverlayError_None) {
            LOG.error("GUI panel overlay error: " + VROverlay.VROverlay_GetOverlayErrorNameFromEnum(error));
        }
    }
    
    @Override
    public boolean isInitialized() {
        return isInitialized;
    }
    
    @Override
    public boolean isActive() {
        return isVisible;
    }

    @Override
    public void setActive(boolean active) {
        this.isVisible = active;
    }

    @Override
    public void dispose(VirtualReality vr) {
    
    }
    
    @Override
    public final VirtualReality getVR() {
        return vr;
    }
    
    @Override
    public final String getName() {
        return name;
    }
    
    @Override
    public final int getRenderTextureHeight() {
        return Math.round(getRenderTextureWidth() * aspect);
    }
    
    @Override
    public final Collection<UIElement> getUIElements() {
        return uiElements;
    }
    
    @Override
    public final void setWidth(float width) {
        if(width == 0) {
            LOG.debug("Width of a panel can not be zero");
            return;
        }
        //Divide width by two as the rendered quad has unit length of two
        width/=2;
        sceneWidth = width;
        aspect = sceneHeight / sceneWidth;
        if(sceneHeight != 0 && sceneWidth != 0) setScale(new Vector3f(sceneWidth, sceneHeight, 1));
    }

    @Override
    public final void setHeight(float height) {
        if(height == 0) {
            LOG.debug("Height of a panel can not be zero");
            return;
        }
        //Divide height by two as the rendered quad has unit length of two
        height /= 2;
        sceneHeight = height;
        if(sceneWidth != 0) {
            aspect = sceneHeight / sceneWidth;
        }
        if(sceneHeight != 0 && sceneWidth != 0) setScale(new Vector3f(sceneWidth, sceneHeight, 1));
    }

    @Override
    public void onFrameBegin(Eye eye) {}

    @Override
    public boolean isListeningVREvents() {
        return false;
    }

    @Override
    public void processEvent(VREvent event) {}

    @Override
    public boolean doesGeneralRendering() {
        return true;
    }

    @Override
    public void renderGeneral(VirtualReality vr, Eye eye) {
        if (!hasChanged() || !isInitialized) return;
        
        //Render UI into texture
        int[] viewport = {0, 0, getRenderTextureWidth(), getRenderTextureHeight()};
        
        UIRenderer renderer = (UIRenderer)vr.getRenderer(UIRenderer.class);
        renderer.bindRenderBuffer(renderFbo);
        renderer.renderPanel(this, viewport, false);
        renderer.unbindRenderBuffer();
    }
    
    @Override
    public boolean doesSceneRendering() {
        return true;
    }

    @Override
    public void renderScene(VirtualReality vr, Eye eye) {
        //TODO test
        //Render panel texture into scene
        if(isInitialized && isVisible) {
            QuadRenderer qr = (QuadRenderer) vr.getRenderer(QuadRenderer.class);
            qr.bindRenderBuffer(vr.getRenderbuffer());
            qr.renderTextureQuad(renderFbo.getColorAttachment(0).getAttachmentObject(), getTransform());
            qr.unbindRenderBuffer();
            
            /*PrimitivesRenderer pr = (PrimitivesRenderer) vr.getRenderer(PrimitivesRenderer.class);
            pr.bindRenderBuffer(vr.getRenderbuffer());
            for(Vector3f point : points){
                pr.renderCube(point, new Vector3f(0.02f, 0.02f, 0.02f), Color.RED);
            }
            pr.unbindRenderBuffer();*/
        }
    }

    @Override
    public void onFrameEnd(Eye eye) {}

    @Override
    public void show() {
        if(isVisible) return;
        isVisible = true;
    }

    @Override
    public void hide() {
        isVisible = false;
    }
    
    @Override
    public boolean isVRVisible() {
        return isVisible;
    }

    @Override
    public void lock() {
        isLocked = true;
    }

    @Override
    public void unlock() {
        isLocked = false;
    }
    
    @Override
    public boolean isLocked() {
        return isLocked;
    }
    
    @Override
    public void onPointerEnter(Vector2f position) {
        //TODO
    }
    
    @Override
    public void onPointerMove(Vector2f position) {
        //TODO
    }
    
    @Override
    public void onPointerClick(Vector2f position) {
        //Pixel position with 0,0 coords in bottom left corner
        Vector2i pixelClickPosition = new Vector2i(Math.round(position.x * getRenderTextureWidth()), Math.round(position.y * getRenderTextureHeight()));
        for(UIElement b : uiElements) {
            if((b.isLockable() && !isLocked) || !b.isLockable()) {
                if(b.isClicked(pixelClickPosition)) {
                    b.click();
                }
            }
        }
        position.scale(2);
        position.sub(new Vector2f(1, 1));
        Vector4f pointH = new Vector4f(position.x, position.y, 0, 1);
        getTransform().transform(pointH);
        points.add(new Vector3f(pointH.x / pointH.w, pointH.y / pointH.w, pointH.z / pointH.w));
    }
    
    @Override
    public void onPointerLeave(Vector2f position) {
        //TODO
    }
    
    @Override
    public void processEvents() {
        if(isKeyboardVisible){
            VREvent event = VREvent.create();
            while(VROverlay.VROverlay_PollNextOverlayEvent(handle, event)) {
                switch(event.eventType()) {
                    case VR.EVREventType_VREvent_KeyboardClosed:
                        onHideKeyboard(false);
                        break;
                    case VR.EVREventType_VREvent_KeyboardDone:
                        onHideKeyboard(true);
                        break;
                    case VR.EVREventType_VREvent_KeyboardCharInput:
                        try{
                            onKeyboardInput(getKeyboardText());
                        } catch(CharacterCodingException e) {}
                        break;
                }
            }
        }
    }
    
    @Override
    public void onKeyboardInput(String currentInput) {}

    @Override
    public void showKeyboard(IVRKeyboardCallback callback, String label, String existingText, int maxCharacters, KeyboardInputMode inputMode, KeyboardLineInputMode lineInputMode, boolean useMinimalMode, long userValue) {
        //Ignore keyboard request from same UIElement
        if(callback != null && keyboardCallback == callback && isKeyboardVisible) return;
        //Show new keyboard for keyborad requested from different UIElement
        if(callback != null && keyboardCallback != callback && keyboardCallback != null) onHideKeyboard(false);
        keyBoardText = "";
        dm.disableControllers();
        try {
            ByteBuffer descBuffer = getBufferFromString(label);
            ByteBuffer textBuffer = getBufferFromString(existingText);
            VROverlay.VROverlay_ShowKeyboardForOverlay(handle,
                    KeyboardInputMode.getRepresentation(inputMode),
                    KeyboardLineInputMode.getRepresentation(lineInputMode),
                    descBuffer,
                    maxCharacters,
                    textBuffer,
                    useMinimalMode,
                    userValue);
            keyboardCallback = callback;
            lastKeyboardCharacters = maxCharacters;
            isKeyboardVisible = true;
        } catch (CharacterCodingException e) {}
    }

    @Override
    public void onHideKeyboard(boolean success) {
        if(!isKeyboardVisible) return;
        dm.enableControllers();
        if(keyboardCallback != null) {
            try {
                keyboardCallback.keyboardClosed(getKeyboardText(), success);
            } catch(CharacterCodingException e) {}
            keyboardCallback = null;
        }
        isKeyboardVisible = false;
    }

    @Override
    public String getKeyboardText() throws CharacterCodingException {
        if(!isKeyboardVisible) return null;
        ByteBuffer textBuffer = GLBuffers.newDirectByteBuffer(2);
        VROverlay.VROverlay_GetKeyboardText(textBuffer);
        keyBoardText += getStringFromBuffer(textBuffer);
        return keyBoardText;
    }

    @Override
    public boolean isKeyboardVisible() {
        return isKeyboardVisible;
    }

    @Override
    public boolean hasChanged() {
        return uiElements.stream().anyMatch(x -> x.hasChanged());
    }

    public Vector3f getNormal() {
        Vector3f normal = new Vector3f(0, 0, 1);
        getRotation().transform(normal);
        return normal;
    }
    
    public Ray getNormalRay() {
        return new Ray(getTranslation(), getNormal());
    }
}
