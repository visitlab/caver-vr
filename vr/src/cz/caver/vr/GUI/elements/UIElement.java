package cz.caver.vr.GUI.elements;

import com.caversoft.core.vecmath.Vector2i;
import com.caversoft.core.vecmath.Vector3i;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;
import java.io.IOException;
import java.io.InputStream;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.Transform2D;
import org.openide.util.RequestProcessor;

/**
 * Represents abstract button element.
 *
 * @author Peter Hutta <433395@mail.muni.cz>
 */
public abstract class UIElement extends Transform2D {

    private static final Log LOG = new Log(UIElement.class);
    private static final RequestProcessor RP = new RequestProcessor("CA-GUI-Tasks", 1, true);
    private static RequestProcessor.Task task = null;

    protected final IVRGUIPanel panel;

    // Pixel width of the element
    protected int width = 128;
    // Pixel height of the element
    protected int height = 128;

    // UI element left-top corner offset
    protected final Vector3f leftTopOffset = new Vector3f(0, 0, 0);

    private final Vector3i pixelPosition = new Vector3i();

    protected float alpha = 1f;

    protected boolean receivingInput = false;
    // Dismises input if panel is locked
    protected boolean lockable = true;
    
    //protected Texture downTexture;
    //protected Texture clickTexture;
    //protected Texture upTexture;
    protected Texture defaultTexture;

    //Texture to be rendered
    private Texture renderTexture;

    // Element state
    protected boolean mouseDown = false;
    protected boolean mouseClicked = false;
    protected boolean mouseUp = false;
    
    protected boolean hasChanged = true;

    public UIElement(IVRGUIPanel overlay) {
        this.panel = overlay;
    }

    public final void refresh() {
        if (mouseDown) {
            onMouseDown();
            mouseClicked = true;
        } else if (mouseDown && mouseClicked) {
            onClick();
            mouseDown = false;
        } else if (mouseUp) {
            mouseDown = false;
            mouseClicked = false;
            mouseUp = false;
            onMouseUp();
        } else {
            renderTexture = defaultTexture;
        }
    }

    /**
     * Returns true if button should have been clicked
     *
     * @param pixelClickPosition Click position with 0,0 in bottom left corner
     * of panel container
     * @return
     */
    public boolean isClicked(Vector2i pixelClickPosition) {
        int yOffset = Math.round(panel.getRenderTextureHeight() - height - leftTopOffset.y);
        return pixelClickPosition.x >= leftTopOffset.x && pixelClickPosition.x <= leftTopOffset.x + width && pixelClickPosition.y >= yOffset && pixelClickPosition.y <= yOffset + height;
    }

    public final void click() {
        mouseDown = true;
        onClick();
    }

    public final void unclick() {
        mouseUp = true;
    }

    /**
     * Receives mouse position when mouse is over this UIElement
     *
     * @param position Mouse position relative to panel bottom-left position
     */
    public void onMouseMove(Vector2i position) {
        hasChanged = true;
    }

    protected void onMouseDown() {
        hasChanged = true;
        //renderTexture = downTexture;
    }

    protected void onClick() {
        hasChanged = true;
        //renderTexture = clickTexture;
    }

    protected void onMouseUp() {
        hasChanged = true;
        //renderTexture = upTexture;
    }

    public final boolean isClicked() {
        return mouseClicked;
    }

    public final Texture getRenderTexture() {
        if (renderTexture == null) {
            return defaultTexture;
        }
        return renderTexture;
    }

    public Vector3i getPixelPosition() {
        return pixelPosition;
    }

    private void updateTranslation() {
        //Anchorpoint transformation
        Vector3f t = new Vector3f();
        // set position of an anchor point to screen center
        t.set(getWidth() / 2.0f, -getHeight() / 2.0f, 0);
        // translate element from screen center to upper left corner
        t.add(new Vector3f(-panel.getRenderTextureWidth() / 2.0f, panel.getRenderTextureHeight() / 2.0f, 0));
        // apply element leftTopOffset (minus in y coord is due to desired top-down behaviour - OpenGL has y coord increasing in bottom-up direction)
        t.add(new Vector3f(leftTopOffset.x, -leftTopOffset.y, 0));
        // normalize
        t.x *= 2.0f / panel.getRenderTextureWidth();
        t.y *= 2.0f / panel.getRenderTextureHeight();
        
        super.setTranslation(t.x, t.y);
        
        Vector2f p = this.getTranslation();
        pixelPosition.x = Math.round(p.x * panel.getRenderTextureWidth());
        pixelPosition.y = Math.round(p.y * panel.getRenderTextureHeight());
    }

    @Override
    public void setTranslation(float x, float y) {
        //TODO set hasChanged to whole panel to force rerender
        UIElement.this.setLeftTopOffset(leftTopOffset);
    }

    /**
     * Returns top-left leftTopOffset of an anchorpoint.
     *
     * @return
     */
    public final Vector3f getAnchorOffset() {
        return leftTopOffset;
    }
    
    public final Vector3f getLeftTopCornerOffset() {
        return leftTopOffset;
    }

    public final int getWidth() {
        return width;
    }

    public final int getHeight() {
        return height;
    }

    /**
     * Returns global transparency of this button.
     *
     * @return
     */
    public final float getAlpha() {
        return alpha;
    }

    public boolean hasChanged() {
        return hasChanged;
    }
    
    public void setChanged(boolean changedState) {
        hasChanged = changedState;
    }

    public final void setLeftTopOffset(Vector3f offset) {
        setLeftTopOffset(offset.x, offset.y);
    }
    
    public final void setLeftTopOffset(float x, float y) {
        this.leftTopOffset.x = x;
        this.leftTopOffset.y = y;
        updateTranslation();
    }

    public final void setWidth(int pixelWidth) {
        setScale(pixelWidth, height);
    }

    public final void setHeight(int pixelHeight) {
        setScale(width, pixelHeight);
    }
    
    public void setScale(int pixelWidth, int pixelHeight) {
        this.width = pixelWidth;
        this.height = pixelHeight;
        super.setScale((float) pixelWidth / panel.getRenderTextureWidth(), (float) pixelHeight / panel.getRenderTextureHeight());
        updateTranslation();
    }

    /**
     * Sets global transparency of this UI element.
     * @param alpha Alpha transparency value
     */
    public final void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public final boolean isLockable() {
        return lockable;
    }

    /**
     * Sets lockable property of this ui element
     * @param lockable If true ui element does not receive input when its container is locked
     */
    public final void setLockable(boolean lockable) {
        this.lockable = lockable;
    }

    /**
     * Loads and returns texture with given name.
     *
     * @param gl
     * @param filename Path to the texture
     * @param suffix Image format of the texture
     * @return Loaded texture
     */
    protected static final Texture loadTexture(GL2GL3 gl, String filename, String suffix) {
        try (InputStream is = UIElement.class.getResourceAsStream(filename)) {
            Texture texture = TextureIO.newTexture(is, true, suffix);
            texture.bind(gl);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
            //texture.setTexParameteri(gl, GL2GL3.GL_TEXTURE_BASE_LEVEL, 0);
            //texture.setTexParameteri(gl, GL2GL3.GL_TEXTURE_MAX_LEVEL, 0);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_S, GL.GL_REPEAT);
            texture.setTexParameteri(gl, GL.GL_TEXTURE_WRAP_T, GL.GL_REPEAT);
            gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
            return texture;
        } catch (IOException ex) {
            LOG.error("Failed loading texture: " + filename, ex);
            return null;
        }
    }
}
