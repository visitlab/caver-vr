package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.dynamics.DynamicsController;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class DynamicsStop extends Button {
    private static DynamicsController dynamicsController = Lookup.getDefault().lookup(DynamicsController.class);

    public DynamicsStop(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/dynamics_stop_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        dynamicsController.stop();
    }
}
