package cz.caver.vr.GUI.overlays.impl;

import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.overlays.ButtonOverlay;
import cz.caver.vr.GUI.buttons.impl.FindStructureButton;
import java.awt.Color;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Dashboard extends ButtonOverlay {

    public Dashboard(VirtualReality vr) {
        super(vr);
        FindStructureButton findButton = new FindStructureButton(this);
        findButton.setLeftTopOffset(new Vector3f(100, 100, 0));
        findButton.setWidth(800);
        uiElements.add(findButton);
        try {
            initAsDashboardOverlay(vr.getGL(), "Dashboard", "Dashboard");
        } catch (Exception e) {
        }
    }

    @Override
    public float getVRWidth() {
        return 2.5f;
    }

    @Override
    public int getRenderTextureWidth() {
        return 1920;
    }
    
    @Override
    public Color getBackgroundColor() {
        return new Color(0, 0, 0, 255);
    }
}
