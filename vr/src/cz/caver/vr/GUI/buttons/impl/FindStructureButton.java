package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.configuration.Configuration;
import com.caversoft.loader.LoaderTasks;
import com.caversoft.structure.Structure;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.renderer.StructureRenderingType;
import cz.caver.vr.GUI.overlays.KeyboardInputMode;
import cz.caver.vr.GUI.overlays.KeyboardLineInputMode;
import cz.caver.vr.GUI.elements.Button;
import cz.caver.vr.GUI.IVRGUIPanel;
import java.io.File;
import net.tascalate.concurrent.Promise;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class FindStructureButton extends Button {
    private Configuration configuration = Lookup.getDefault().lookup(Configuration.class);

    public FindStructureButton(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/find_button.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        //Request keyboard
        panel.showKeyboard((String structureId, boolean success) -> {
            if(success && structureId != null && !structureId.equals("")) {
                try {
                    String directory = (String) configuration.getParameter(Configuration.PARAM_LAST_SOURCE_DIRECTORY);
                    directory = (String) (directory == null ? configuration.getParameter("CAVER_HOME_DIR") : directory);
                    //String filePath = directory + System.getProperty("file.separator") + structureId + ".pdb";
                    File f = new File(directory);
                    ((Promise<Structure>)LoaderTasks.downloadStructure(structureId, true, true, f))
                            .whenComplete((s, t) -> {
                                if(s != null) {
                                    //structController.setActiveStructure(s);
                                    rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_LINES, true);
                                    rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_POINTS, true);
                                    rm.activateStructureRenderingType(StructureRenderingType.STRUCTURE_STICKS_AO);
                                }
                            });
                    
                } catch(Exception e) {
                }
            }
        }, "Find structure", "", 6, KeyboardInputMode.NORMAL, KeyboardLineInputMode.SINGLE_LINE, true, 0);
    }
}
