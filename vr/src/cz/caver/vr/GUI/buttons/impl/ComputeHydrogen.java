package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.controller.StructureController;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.gui.functions.Functions;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import java.util.HashSet;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public class ComputeHydrogen extends Button {
    private static final StructureController structureController = Lookup.getDefault().lookup(StructureController.class);

    public ComputeHydrogen(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/hydrogen_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        Functions.computeHydrogens(new HashSet<>(structureController.getStructures()));
    }
}