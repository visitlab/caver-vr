package cz.caver.vr.GUI.overlays;

/*package cz.caver.vr.overlays;

import cz.caver.vr.GUI.IVROverlay;
import com.caversoft.core.vecmath.Vector2i;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import cz.caver.renderer.util.RenderingUtils;
import cz.caver.vr.Eye;
import cz.caver.vr.devices.Device;
import cz.caver.vr.VirtualReality;
import static cz.caver.vr.overlays.ButtonOverlay.vr;
import cz.caver.vr.overlays.buttons.UIElement;
import static cz.caver.vr.utils.Matrices.matrix4fToHmdMatrix34;
import static cz.caver.vr.utils.Utils.getStringFromBuffer;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.charset.CharacterCodingException;
import javax.imageio.ImageIO;
import javax.swing.CellRendererPane;
import javax.swing.JPanel;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VREvent;
import org.lwjgl.openvr.VREventData;
import org.lwjgl.openvr.VROverlay;
import org.openide.windows.WindowManager;
import static cz.caver.vr.utils.Utils.getBufferFromString;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
/*public abstract class JPanelOverlay extends JPanel implements IVROverlay {
    private static final Log LOG = new Log(JPanelOverlay.class);
    
    protected static VirtualReality vr;
    protected long handle = VR.k_ulOverlayHandleInvalid;
    protected long iconHandle = VR.k_ulOverlayHandleInvalid; //Valid only when overlay is a dashboard
    
    protected String key;
    protected String name;
    protected OverlayType type = OverlayType.NOT_INITIALIZED;
    
    protected boolean visible = false;
    protected boolean keyboardVisible = false;
    protected boolean locked = false;
    protected boolean hasChanged = true;
    
    protected UIElement keyboardCallback = null;
    protected int maxKeyboardCharacters = 1;
    
    public JPanelOverlay(VirtualReality vr) {
        this.vr = vr;
    }
    
    @Override
    public final void initAsDashboardOverlay(GL2GL3 gl, String key, String name) {
        dispose(vr);
        ByteBuffer keyBuffer, nameBuffer;
        LongBuffer dashboardHandle = GLBuffers.newDirectLongBuffer(1);
        LongBuffer icoHandle = GLBuffers.newDirectLongBuffer(1);
        try {
            keyBuffer = getBufferFromString(key);
            nameBuffer = getBufferFromString(name);
            int error = VROverlay.VROverlay_CreateDashboardOverlay(keyBuffer, nameBuffer, dashboardHandle, icoHandle);
            this.handle = dashboardHandle.get(0);
            this.iconHandle = icoHandle.get(0);
            if (error != VR.EVROverlayError_VROverlayError_None) {
                if(handle != VR.k_ulOverlayHandleInvalid) {
                    type = OverlayType.SCENE;
                    initOverlayInternal();
                }
            }
        } catch(CharacterCodingException e) {}
    }
    
    @Override
    public final void initAsSceneOverlay(GL2GL3 gl, String key, String name) {
        dispose(vr);
        ByteBuffer keyBuffer, nameBuffer;
        LongBuffer overlayHandle = GLBuffers.newDirectLongBuffer(1);
        try {
            keyBuffer = getBufferFromString(key);
            nameBuffer = getBufferFromString(name);
            int error = VROverlay.VROverlay_CreateOverlay(keyBuffer, nameBuffer, overlayHandle);
            handle = overlayHandle.get(0);
            if (error != VR.EVROverlayError_VROverlayError_None) {
                if(handle != VR.k_ulOverlayHandleInvalid) {
                    type = OverlayType.SCENE;
                    initOverlayInternal();
                }
            }
        } catch(CharacterCodingException e) {}
    }
    
    private void initOverlayInternal() {
        handleError(VROverlay.VROverlay_SetOverlayWidthInMeters(handle, getVRWidth()));
        handleError(VROverlay.VROverlay_SetOverlayInputMethod(handle, InputMethod.getRepresentation(getInputMethod())));
    }
    
    @Override
    public boolean isInitialized() {
        return type != OverlayType.NOT_INITIALIZED;
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
    public final VirtualReality getVR() {
        return vr;
    }
    
    @Override
    public long getHandle() {
        return handle;
    }
    
    @Override
    public long getIconHandle() {
        return iconHandle;
    }
    
    @Override
    public void setFlag(OverlayFlag flag) {
        VROverlay.VROverlay_SetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), true);
    }
    
    @Override
    public void unsetFlag(OverlayFlag flag) {
        VROverlay.VROverlay_SetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), false);
    }
    
    @Override
    public boolean getFlag(OverlayFlag flag) {
        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(1); //1 byte for boolean
        VROverlay.VROverlay_GetOverlayFlag(handle, OverlayFlag.getRepresentation(flag), buffer);
        return buffer.get(0) != 0;
    }
    
    @Override
    public void setOverlayTransformTrackedDeviceRelative(Device device, Vector3f localOffset) {
        //TODO apply offset
        //Vector3f worldOffset = d.getPose().getRotation(). * localOffset;
        //VROverlay.VROverlay_SetOverlayTransformTrackedDeviceRelative(handle, 0, pmatTrackedDeviceToOverlayTransform)
    }
    
    @Override
    public void setOverlayTransformAbsolute(Matrix4f transform) {
        HmdMatrix34 hmdTransform = matrix4fToHmdMatrix34(transform);
        //TODO
        VROverlay.VROverlay_SetOverlayTransformAbsolute(handle, VR.ETrackingUniverseOrigin_TrackingUniverseStanding, hmdTransform);
    }
    
    @Override
    public void show() {
        if(visible) return;
        visible = true;
        if(type == OverlayType.DASHBOARD) {
            try {
                VROverlay.VROverlay_ShowDashboard(getBufferFromString(key));
            } catch(CharacterCodingException e) {}
        } else if(type == OverlayType.SCENE) {
            VROverlay.VROverlay_ShowOverlay(handle);
        }
    }
    
    @Override
    public void hide() {
        if(!visible) return;
        visible = false;
        if(type == OverlayType.SCENE) {
            VROverlay.VROverlay_HideOverlay(handle);
        }
    }
    
    @Override
    public boolean isVisible() {
        //Force true
        return true;
    }
    
    @Override
    public boolean isVRVisible() {
        return visible;
    }
    
    @Override
    public boolean isLocked() {
        return locked;
    }
    
    @Override
    public void lock() {
        locked = true;
        hasChanged = true;
    }
    
    @Override
    public void unlock() {
        locked = false;
        hasChanged = true;
    }
    
    //TODO
    /*@Override
    public void showKeyboard(UIElement callback, String description, String existingText, int maxCharacters, KeyboardInputMode inputMode, KeyboardLineInputMode lineInputMode, boolean useMinimalMode, long userValue) throws CharacterCodingException {
        //Ignore keyboard request from same UIElement
        if(callback != null && keyboardCallback == callback && isKeyboardVisible) return;
        //Show new keyboard for keyborad requested from different UIElement
        if(callback != null && keyboardCallback != callback) hideKeyboard(false);
        
        keyboardCallback = callback;
        maxKeyboardCharacters = maxCharacters;
        isKeyboardVisible = true;
        ByteBuffer descBuffer = getBuffer(description);
        ByteBuffer textBuffer = getBuffer(existingText);
        //TODO
        VROverlay.VROverlay_ShowKeyboardForOverlay(handle,
                KeyboardInputMode.getRepresentation(inputMode),
                KeyboardLineInputMode.getRepresentation(lineInputMode),
                descBuffer,
                maxCharacters,
                textBuffer,
                useMinimalMode,
                userValue);
    }
    
    @Override
    public void hideKeyboard(boolean success) {
        if(!isKeyboardVisible) return;
        isKeyboardVisible = false;
        
        if(keyboardCallback != null) {
            keyboardCallback.onKeyboardClose(getKeyboardText(), success);
        }
        VROverlay.VROverlay_HideKeyboard();
    }*/
    /*
    @Override
    public String getKeyboardText() {
        if(!keyboardVisible) return null;
        ByteBuffer textBuffer = GLBuffers.newDirectByteBuffer(maxKeyboardCharacters * 8);
        VROverlay.VROverlay_GetKeyboardText(textBuffer);
        return getStringFromBuffer(textBuffer);
    }       
    
    @Override
    public boolean isKeyboardVisible() {
        return keyboardVisible;
    }
    
    @Override
    public boolean hasChanged() {
        return hasChanged;
    }
    
    @Override
    public void renderGeneral(VirtualReality vr, Eye eye) {
        LOG.debug("Rendering to texture: " + key);
        GL2GL3 gl = vr.getGL();
        
        setLocation(0, 0); // CellRendererPane sets location to (-w,-h)
        setSize(getPreferredSize());
        layoutComponent(this);
        
        GraphicsConfiguration gc = WindowManager.getDefault().getMainWindow().getGraphicsConfiguration();
        BufferedImage image = gc.createCompatibleImage(this.getWidth(), this.getHeight());
        Graphics g = image.getGraphics();
        
        CellRendererPane crp = new CellRendererPane();
        crp.paintComponent(g, this, crp, getBounds());
        
        // TODO remove, image manipulation check only code
        // image manipulation breaks Texture creation, I suggest to left JOGL texture classes
        int width = image.getWidth();
        int[] data = new int[width];
        IntBuffer ib = GLBuffers.newDirectIntBuffer(image.getHeight() * image.getWidth());
        for (int y = image.getHeight() - 1; y >= 0; y--) {
            image.getRGB(0, y, width, 1, data, 0, width);
            for (int x = 0; x < width; x++) {
                data[x] |= 0x00FF0000;
            }
            image.setRGB(0, y, width, 1, data, 0, width);
            ib.put(data);
        }
        ib.rewind();
        
        try {
            ImageIO.write(image, "png", new File("ui.png"));
        } catch (IOException ex) {
            LOG.error(ex);
        }
        
        // This is problematic, probably due to internal use of non-direct (Java heap) NIO buffer
        TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), image, GL.GL_RGBA8, GL.GL_RGB, true);
        
        com.jogamp.opengl.util.texture.Texture texture = AWTTextureIO.newTexture(textureData);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_EDGE);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        
        texture.bind(gl);
        // This makes it work since ib is direct NIO buffer
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL.GL_BGRA, GL2.GL_UNSIGNED_INT_8_8_8_8_REV, ib);
        
        RenderingUtils.debugSaveTexture(gl, "texture.png", texture.getTextureObject(gl));
        
        Texture t = Texture.create();
        t.set(texture.getTextureObject(gl), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Auto);
        handleError(VROverlay.VROverlay_SetOverlayTexture(handle, t));

        g.dispose();
        
        hasChanged = false;
    }
    
    @Override
    public void processEvents() {
        if(javax.swing.SwingUtilities.isEventDispatchThread()) {
            setLocation(0, 0); // CellRendererPane sets location to (-w,-h)
            setSize(getPreferredSize());
            layoutComponent(this);
            this.repaint();

            VREvent event = VREvent.create();
            while (VROverlay.VROverlay_PollNextOverlayEvent(handle, event, event.sizeof())) {
                processEvent(event);
            }
        }
    }
    
    //TODO add keyboard callbacks
    @Override
    public void processEvent(VREvent event) {
        Vector2i position = getMousePosition(event.data());
        int button = (event.data().mouse().button() == VR.EVRMouseButton_VRMouseButton_Right) ? MouseEvent.BUTTON1 : MouseEvent.BUTTON2;
        button = MouseEvent.getMaskForButton(button);
        switch (event.eventType()) {
            case VR.EVREventType_VREvent_MouseMove:
                dispatchEvent(new MouseEvent(this, 0, 0, 0, position.x, position.y, 1, false, MouseEvent.MOUSE_MOVED));
                hasChanged = true;
                break;
            case VR.EVREventType_VREvent_MouseButtonDown:
                dispatchEvent(new MouseEvent(this, 0, 0, 0, position.x, position.y, 1, false, button | MouseEvent.MOUSE_PRESSED));
                hasChanged = true;
                break;
            case VR.EVREventType_VREvent_MouseButtonUp:
                dispatchEvent(new MouseEvent(this, 0, 0, 0, position.x, position.y, 1, false, button | MouseEvent.MOUSE_RELEASED));
                hasChanged = true;
                break;
            case VR.EVREventType_VREvent_OverlayShown:
                repaint();
                break;
        }
    }
    
    private static void layoutComponent(Component c) {
        synchronized (c.getTreeLock()) {
            c.doLayout();
            if (c instanceof Container) {
                for (Component child : ((Container) c).getComponents()) {
                    layoutComponent(child);
                }
            }
        }
    }
    
    private Vector2i getMousePosition(VREventData data) {
        Vector2i ret = new Vector2i();
        ret.x = Math.round(data.mouse().x() * getWidth());
        ret.y = Math.round(data.mouse().y() * getHeight());
        return ret;
    }
    
    private void handleError(int error) {
        if (error != VR.EVROverlayError_VROverlayError_None) {
            LOG.error("Overlay error: " + VROverlay.VROverlay_GetOverlayErrorNameFromEnum(error));
        }
    }
}
*/