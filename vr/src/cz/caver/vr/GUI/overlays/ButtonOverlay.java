package cz.caver.vr.GUI.overlays;

import cz.caver.vr.GUI.IVROverlay;
import com.caversoft.core.vecmath.Vector2i;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import cz.caver.renderer.RenderStateManager;
import cz.caver.renderer.pipeline.Framebuffer;
import cz.caver.renderer.renderable.Transformable;
import cz.caver.renderer.state.BlendState;
import cz.caver.renderer.state.RenderState;
import cz.caver.renderer.util.RenderingUtils;
import cz.caver.vr.Eye;
import cz.caver.vr.GUI.GUIPanel;
import cz.caver.vr.devices.Device;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.utils.Matrices;
import static cz.caver.vr.utils.Utils.getStringFromBuffer;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VREventData;
import org.lwjgl.openvr.VROverlay;
import static cz.caver.vr.utils.Utils.getBufferFromString;
import cz.caver.vr.GUI.IVRKeyboardCallback;
import cz.caver.vr.IVRComponent;
import cz.caver.vr.GUI.elements.UIElement;

/**
 * Base overlay type for showing buttons. Don't forget to override init function and init it as dashboard or scene overlay.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public abstract class ButtonOverlay extends GUIPanel implements IVROverlay {
    private static final Log LOG = new Log(ButtonOverlay.class);
    
    protected long iconHandle = VR.k_ulOverlayHandleInvalid; //Valid only when overlay is a dashboard
    
    protected OverlayType type = OverlayType.NOT_INITIALIZED;

    public ButtonOverlay(VirtualReality vr) {
        super(vr);
        
        //TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), image, GL.GL_RGBA8, GL.GL_RGB /* Ignored but must not be 0 */, true);
        //renderFbo.createColorAttachment(gl, 0, Framebuffer.AttachmentType.TEXTURE, GL.GL_RGBA8);
        //renderTexture = loadTexture(gl, "/resources/textures/dynamics_next_128.png", TextureIO.PNG);
    }
    
    @Override
    public IVRComponent init(VirtualReality vr) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public final void initAsDashboardOverlay(GL2GL3 gl, String key, String name) {
        dispose(vr);
        this.key = key;
        this.name = name;
        ByteBuffer keyBuffer, nameBuffer;
        LongBuffer dashboardHandle = GLBuffers.newDirectLongBuffer(1);
        LongBuffer icoHandle = GLBuffers.newDirectLongBuffer(1);
        try {
            keyBuffer = getBufferFromString(key);
            nameBuffer = getBufferFromString(name);
            int error = VROverlay.VROverlay_CreateDashboardOverlay(keyBuffer, nameBuffer, dashboardHandle, icoHandle);
            if (error == VR.EVROverlayError_VROverlayError_None) {
                handle = dashboardHandle.get(0);
                iconHandle = icoHandle.get(0);
                if(handle != VR.k_ulOverlayHandleInvalid) {
                    type = OverlayType.DASHBOARD;
                    handleError(VROverlay.VROverlay_SetOverlayWidthInMeters(handle, getVRWidth()));
                    handleError(VROverlay.VROverlay_SetOverlayInputMethod(handle, InputMethod.getRepresentation(getInputMethod())));
                }
            }
        } catch(CharacterCodingException e) {}
    }

    @Override
    public final void initAsSceneOverlay(GL2GL3 gl, String key, String name) {
        dispose(vr);
        this.key = key;
        this.name = name;
        ByteBuffer keyBuffer, nameBuffer;
        LongBuffer overlayHandle = GLBuffers.newDirectLongBuffer(1);
        try {
            keyBuffer = getBufferFromString(key);
            nameBuffer = getBufferFromString(name);
            int error = VROverlay.VROverlay_CreateOverlay(keyBuffer, nameBuffer, overlayHandle);
            if (error == VR.EVROverlayError_VROverlayError_None) {
                handle = overlayHandle.get(0);
                if(handle != VR.k_ulOverlayHandleInvalid) {
                    type = OverlayType.SCENE;
                    handleError(VROverlay.VROverlay_SetOverlayWidthInMeters(handle, getVRWidth()));
                    handleError(VROverlay.VROverlay_SetOverlayInputMethod(handle, InputMethod.getRepresentation(getInputMethod())));
                }
            }
        } catch(CharacterCodingException e) {}
    }

    @Override
    public final boolean isInitialized() {
        return type != OverlayType.NOT_INITIALIZED;
    }
    
    @Override
    public final long getHandle() {
        return handle;
    }
    
    @Override
    public final long getIconHandle() {
        return iconHandle;
    }

    @Override
    public InputMethod getInputMethod() {
        return InputMethod.MOUSE;
    }
    
    @Override
    public boolean isLocked() {
        return isLocked;
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
    public final void setFlag(OverlayFlag flag) {
        VROverlay.VROverlay_SetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), true);
    }
    
    @Override
    public final void unsetFlag(OverlayFlag flag) {
        VROverlay.VROverlay_SetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), false);
    }
    
    @Override
    public final boolean getFlag(OverlayFlag flag) {
        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(1); //1 byte for boolean
        VROverlay.VROverlay_GetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), buffer);
        return buffer.get(0) != 0;
    }
    
    @Override
    public OverlayType getOverlayType() {
        return type;
    }

    @Override
    public final void setOverlayTransformTrackedDeviceRelative(Device device, Vector3f localOffset) {
        //TODO
        //HmdMatrix34 mat = Matrices.matrix4fToHmdMatrix34(transform);
        //VROverlay.VROverlay_SetOverlayTransformTrackedDeviceRelative(handle, VR.ETrackingUniverseOrigin_TrackingUniverseStanding, mat);
    }
    
    @Override
    public final void setOverlayTransformAbsolute(Matrix4f transform) {
        HmdMatrix34 mat = Matrices.matrix4fToHmdMatrix34(transform);
        VROverlay.VROverlay_SetOverlayTransformAbsolute(handle, VR.ETrackingUniverseOrigin_TrackingUniverseStanding, mat);
    }

    @Override
    public final void show() {
        if(isVisible) return;
        isVisible = true;
        if(type == OverlayType.DASHBOARD) {
            try {
                VROverlay.VROverlay_ShowDashboard(getBufferFromString(key));
            } catch(CharacterCodingException e) {}
        } else if(type == OverlayType.SCENE) {
            LOG.debug("Showing overlay " + name + " with result " + VROverlay.VROverlay_ShowOverlay(handle));
        }
    }
    
    @Override
    public final void hide() {
        if(!isVisible) return;
        isVisible = false;
        if(type == OverlayType.SCENE) {
            VROverlay.VROverlay_HideOverlay(handle);
        }
    }

    @Override
    public final boolean isVRVisible() {
        return isVisible;
    }
    
    @Override
    public final void showKeyboard(IVRKeyboardCallback callback, String label, String existingText, int maxCharacters, KeyboardInputMode inputMode, KeyboardLineInputMode lineInputMode, boolean useMinimalMode, long userValue) {
        //Ignore keyboard request from same UIElement
        if(callback != null && keyboardCallback == callback && isKeyboardVisible) return;
        //Show new keyboard for keyborad requested from different UIElement
        if(callback != null && keyboardCallback != callback) onHideKeyboard(false);
        
        keyboardCallback = callback;
        lastKeyboardCharacters = maxCharacters;
        isKeyboardVisible = true;
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
        } catch (CharacterCodingException e) {}
    }
    
    @Override
    public final void onHideKeyboard(boolean success) {
        if(!isKeyboardVisible) return;
        isKeyboardVisible = false;
        if(keyboardCallback != null) {
            try {
                keyboardCallback.keyboardClosed(getKeyboardText(), success);
            } catch(CharacterCodingException e) {}
        }
        //TODO does it create keyboard hide events?
        VROverlay.VROverlay_HideKeyboard();
    }
    
    @Override
    public final String getKeyboardText() throws CharacterCodingException {
        if(!isKeyboardVisible) return null;
        ByteBuffer textBuffer = GLBuffers.newDirectByteBuffer(lastKeyboardCharacters * 8);
        VROverlay.VROverlay_GetKeyboardText(textBuffer);
        return getStringFromBuffer(textBuffer);
    }
    
    @Override
    public final boolean isKeyboardVisible() {
        return isKeyboardVisible;
    }

    @Override
    public final boolean hasChanged() {
        return uiElements.stream().anyMatch(x -> x.hasChanged());
    }

    /*@Override
    public void renderGeneral(VirtualReality vr, Eye eye) {
        if (!hasChanged) return;
        GL2GL3 gl = vr.getGL();
        
        RenderStateManager rsm = RenderStateManager.getInstance();
        RenderState rs = rsm.beginRender(gl);
        
        BlendState blend = rs.setBlendEnabled(gl, true);
        blend.setFunc(gl, GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        int[] viewport = {0, 0, getRenderTextureWidth(), getRenderTextureHeight()};
        
        //Fill background
        vr.getFillRenderer().bindRenderBuffer(renderFbo);
        vr.getFillRenderer().render(getBackgroundColor(), viewport);
        vr.getFillRenderer().unbindRenderBuffer();
        
        vr.getUIElementRenderer().bindRenderBuffer(renderFbo);
        
        //Button
        for(Button button : buttons) {
            if(button.hasChanged()) { button.updateTransform(); }
            if (button.getRenderTexture() != null) {
                vr.getUIElementRenderer().render(button, viewport);
            }
            button.hasChanged(false);
        }

        vr.getUIElementRenderer().unbindRenderBuffer();
        
        rsm.endRender(gl);
        
        int textureHandle = renderFbo.getColorAttachment(0).getAttachmentObject();
        
        //Get texture from fbo and send it to compositor
        org.lwjgl.openvr.Texture overlayTexture = org.lwjgl.openvr.Texture.create();
        overlayTexture.set(textureHandle, VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Auto);
        handleError(VROverlay.VROverlay_SetOverlayTexture(handle, overlayTexture));
        
        //RenderingUtils.debugSaveTexture(gl, "texture.png", textureHandle);
        
        renderFbo.unbind(gl);
        
        hasChanged = false;
    }*/

    @Override
    public void processEvents() {
        VREvent event = VREvent.create();
        while (VROverlay.VROverlay_PollNextOverlayEvent(handle, event, event.sizeof())) {
            processEvent(event);
        }
    }

    @Override
    public void processEvent(VREvent event) {
        Vector2i position = getMousePosition(event.data());
        //int mouseButton = event.data().mouse().button();
        UIElement elem = getElementUnderMouse(position);
        switch (event.eventType()) {
            case VR.EVREventType_VREvent_MouseMove:
                if(elem != null) { elem.onMouseMove(position); }
                break;
            case VR.EVREventType_VREvent_MouseButtonDown:
                if(elem != null) { elem.click(); }
                break;
            case VR.EVREventType_VREvent_MouseButtonUp:
                for( UIElement b : uiElements) {
                    if(b.isClicked() /*&& !b.isToggleable()*/) {
                        b.unclick();
                    }
                }
                break;
            case VR.EVREventType_VREvent_OverlayShown:
                break;
            case VR.EVREventType_VREvent_KeyboardDone:
                try {
                    if(keyboardCallback != null) {
                        keyboardCallback.keyboardClosed(getKeyboardText(), true);
                        keyboardCallback = null;
                    }
                } catch(CharacterCodingException e) {}
                break;
            case VR.EVREventType_VREvent_KeyboardClosed:
                try {
                    if(keyboardCallback != null) {
                        keyboardCallback.keyboardClosed(getKeyboardText(), false);
                        keyboardCallback = null;
                    }
                } catch(CharacterCodingException e) {}
                break;    
        }
    }
    
    private UIElement getElementUnderMouse(Vector2i mousePos) {
        for(UIElement elem : uiElements) {
            if(mousePos.x >= elem.getAnchorOffset().x
                    && mousePos.x <= elem.getAnchorOffset().x + elem.getWidth()
                    && mousePos.y >= elem.getAnchorOffset().y
                    && mousePos.y <= elem.getAnchorOffset().y + elem.getHeight()) {
                return elem;
            }
        }
        return null;
    }

    private Vector2i getMousePosition(VREventData data) {
        Vector2i ret = new Vector2i();
        ret.x = Math.round(data.mouse().x() * getRenderTextureWidth());
        ret.y = Math.round(data.mouse().y() * getRenderTextureHeight());
        return ret;
    }
    
    private void handleError(int error) {
        if (error != VR.EVROverlayError_VROverlayError_None) {
            LOG.error("Overlay error: " + VROverlay.VROverlay_GetOverlayErrorNameFromEnum(error));
        }
    }
    
    @Override
    public void dispose(VirtualReality vr) {
        if(handle != VR.k_ulOverlayHandleInvalid) {
            VROverlay.VROverlay_DestroyOverlay(handle);
        }
        if(iconHandle != VR.k_ulOverlayHandleInvalid) {
            VROverlay.VROverlay_DestroyOverlay(iconHandle);
        }
    }

    @Override
    public void onFrameBegin(Eye eye) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isListeningVREvents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean doesSceneRendering() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void renderScene(VirtualReality vr, Eye eye) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean doesGeneralRendering() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onFrameEnd(Eye eye) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}