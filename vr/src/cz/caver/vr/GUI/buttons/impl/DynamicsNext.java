package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.Structure;
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
public class DynamicsNext extends Button {
    private static DynamicsController dynamicsController = Lookup.getDefault().lookup(DynamicsController.class);

    public DynamicsNext(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/dynamics_next_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        Structure structure = dynamicsController.getStructure();
        if (structure == null) {
            return;
        }
        
        if ((structure.getCurrentFrame() + dynamicsController.getStep()) < structure.getLastFrame()) {
            dynamicsController.setCurentFrame(structure.getCurrentFrame() + dynamicsController.getStep());
        } else if (structure.getCurrentFrame() < structure.getLastFrame()) {
            dynamicsController.setCurentFrame(structure.getLastFrame());
        } else if (dynamicsController.isLoopEnabled()) {
            dynamicsController.setCurentFrame(0);
        }
    }
}