package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.structure.Structure;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.filter.AtomFilter;
import cz.caver.renderer.filter.StructureFilterManager;
import cz.caver.renderer.renderable.RenderableStructure;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public class VisualizationToggleHydrogen extends Button {
    private static final StructureFilterManager filterManager = Lookup.getDefault().lookup(StructureFilterManager.class);

    public VisualizationToggleHydrogen(IVRGUIPanel panel) {
        super(panel);
        //setToggleable(true);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/hydrogen_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        for (RenderableStructure rs : rm.getRenderableStructures()) {
            Structure structure = rs.getStructure();
            filterManager.setFilterEnabled(structure, AtomFilter.HYDROGEN, isClicked());
        }
    }
}