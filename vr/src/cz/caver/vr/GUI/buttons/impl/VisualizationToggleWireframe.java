package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.controller.StructureController;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.renderer.StructureRenderingType;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public class VisualizationToggleWireframe extends Button {
    private final StructureController sc = Lookup.getDefault().lookup(StructureController.class);

    public VisualizationToggleWireframe(IVRGUIPanel panel) {
        super(panel);
        //setToggleable(true);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/structure_wireframe_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        if(!rm.getRenderableStructure(sc.getFirstActiveStructure()).isRenderingStrategyActive(StructureRenderingType.STRUCTURE_LINES) || !rm.getRenderableStructure(sc.getFirstActiveStructure()).isRenderingStrategyActive(StructureRenderingType.STRUCTURE_LINES)) {
            deactivateAllRenderingTypes();
            rm.activateStructureRenderingType(StructureRenderingType.STRUCTURE_LINES, true);
            rm.activateStructureRenderingType(StructureRenderingType.STRUCTURE_POINTS, true);
        }
    }
}