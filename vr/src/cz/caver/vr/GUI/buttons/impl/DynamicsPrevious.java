/*

 */

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
public class DynamicsPrevious  extends Button {
    private static DynamicsController dynamicsController = Lookup.getDefault().lookup(DynamicsController.class);

    public DynamicsPrevious(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/dynamics_previous_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        Structure structure = dynamicsController.getStructure();
        if (structure == null) {
            return;
        }
        
        if ((structure.getCurrentFrame() - dynamicsController.getStep()) >= 1) {
            dynamicsController.setCurentFrame(structure.getCurrentFrame() - dynamicsController.getStep());
        } else {
            dynamicsController.setCurentFrame(0);
        }
    }
}