package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.log.Log;
import com.caversoft.structure.Structure;
import com.caversoft.structure.controller.StructureController;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public class DeleteButton extends Button {
    protected static final Log LOG = new Log(DeleteButton.class);
    private final StructureController sc = Lookup.getDefault().lookup(StructureController.class);

    public DeleteButton(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/delete_button.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        for(Structure s : sc.getActiveStructures()) {
            sc.removeStructure(s);
        }
    }
}
