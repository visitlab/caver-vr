package cz.caver.vr.GUI.buttons.impl;

import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.GUI.IVRGUIPanel;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class LockButton extends Button {

    public LockButton(IVRGUIPanel panel) {
        super(panel);
        setLockable(false);
        setToggleable(true);
        
        untoggledTexture = loadTexture(panel.getVR().getGL(), "/resources/textures/lock_unlocked_128.png", TextureIO.PNG);
        toggledTexture = loadTexture(panel.getVR().getGL(), "/resources/textures/lock_locked_128.png", TextureIO.PNG);
        defaultTexture = untoggledTexture;
    }
    
    @Override
    protected void onClick() {
        super.onClick();
        if(panel.isLocked()) panel.unlock();
        else panel.lock();
    }
}
