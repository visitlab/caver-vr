package cz.caver.vr.GUI;

import cz.caver.vr.IVRComponent;
import cz.caver.vr.GUI.overlays.KeyboardInputMode;
import cz.caver.vr.GUI.overlays.KeyboardLineInputMode;
import cz.caver.vr.GUI.elements.UIElement;
import java.awt.Color;
import java.nio.charset.CharacterCodingException;
import java.util.Collection;
import java.util.Iterator;
import javax.vecmath.Vector2f;

/**
 * Common interface for all VR panels.
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public interface IVRGUIPanel extends IVRComponent {
    
    /**
     * Returns name of this panel.
     * @return 
     */
    public String getName();
    
    /**
     * Returns width of panel texture.
     * @return Texture width
     */
    public int getRenderTextureWidth();
    
    /**
     * Returns height of panel texture.
     * @return Texture height
     */
    public int getRenderTextureHeight();
    
    /**
     * Sets scene width of panel
     * @param width 
     */
    public void setWidth(float width);
    
    /**
     * Sets scene height of panel
     * @param width 
     */
    public void setHeight(float height);
    
    /**
     * Returns background color for this panel
     * @return Background color
     */
    public Color getBackgroundColor();
    
    /**
     * Returns all UI elements that this panel contains
     * @return 
     */
    public Collection<UIElement> getUIElements();
    
    /**
     * Shows panel.
     */
    public void show();
    
    /**
     * Hides panel.
     */
    public void hide();
    
    /**
     * True if panel is locked to input events
     * @return 
     */
    public boolean isLocked();
    
    /**
     * Locks panel for input events
     */
    public void lock();
    
    /**
     * Unlocks panel for input events
     */
    public void unlock();
    
    /**
     * Called when controller pointer enters the panel
     * @param position 
     */
    public void onPointerEnter(Vector2f position);
    
    /**
     * Called every frame when controller pointer moves over the panel
     * @param position [0,1] normalized position
     */
    public void onPointerMove(Vector2f position);
    
    /**
     * Called when controller performs click on the panel
     * @param position [0,1] normalized position
     */
    public void onPointerClick(Vector2f position);
    
    /**
     * Called when controller pointer leaves the panel
     * @param position 
     */
    public void onPointerLeave(Vector2f position);
    
    /**
     * Checks if panel is visible in VR.
     * @return True if panel is visible in VR.
     */
    public boolean isVRVisible();
    
    /**
     * Checks if panel has changed since last rendering and needs redrawal.
     * @return True if panel has changed since last rendering.
     */
    public boolean hasChanged();
    
    /**
     * Processes all queued overlay events.
     */
    public void processEvents();
    
    /**
     * Shows keyboard for this UI.
     * @param callback UIElement callback which is supposed to handle returned text
     * @param label Label for keyboard
     * @param existingText Prefilled text
     * @param maxCharacters Maximum number of characters
     * @param inputMode Keyboard input mode
     * @param lineInputMode Keyboard line input mode
     * @param useMinimalMode Use of minimal mode
     * @param userValue User value
     * @throws CharacterCodingException 
     */
    public void showKeyboard(IVRKeyboardCallback callback, String label, String existingText, int maxCharacters, KeyboardInputMode inputMode, KeyboardLineInputMode lineInputMode, boolean useMinimalMode, long userValue);
    
    /**
     * Called when keyboard receives input
     */
    public void onKeyboardInput(String currentInput);
    
    /**
     * Called when keyboard is closed
     */
    public void onHideKeyboard(boolean success);
    
    /**
     * Gets current text from keyboard.
     * @return Current keyboard text. Empty string if keyboard is not visible.
     */
    public String getKeyboardText() throws CharacterCodingException;
    
    /**
     * Checks if keyboard is visible in VR.
     * @return True if keyboard is visible in VR.
     */
    public boolean isKeyboardVisible();
    
    /**
     * Called when panel is hit by a raycast done by device d
     * @param hit Hit object
     * @param d Device which caused raycasting
     */
    //public void onRaycastHit(Device d, RaycastHit hit);
}
