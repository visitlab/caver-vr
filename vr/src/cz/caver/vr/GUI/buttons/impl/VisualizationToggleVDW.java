/*

 */

package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.controller.StructureController;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.StructureRenderingType;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.VirtualReality.VRRenderingType;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class VisualizationToggleVDW extends Button {
    private final StructureController sc = Lookup.getDefault().lookup(StructureController.class);

    public VisualizationToggleVDW(IVRGUIPanel panel) {
        super(panel);
        //setToggleable(true);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/structure_vdw_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        if(!rm.getRenderableStructure(sc.getFirstActiveStructure()).isRenderingStrategyActive(VRRenderingType.STRUCTURE_VDW_AO)){
            deactivateAllRenderingTypes();
            rm.activateStructureRenderingType(VRRenderingType.STRUCTURE_VDW_AO, true);
        }
    }
}
