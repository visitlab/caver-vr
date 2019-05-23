package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.loader.LoaderController;
import com.caversoft.loader.StructureFileFormat;
import com.caversoft.log.Log;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.vr.GUI.ButtonFunctions;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Lookup;

/**
 *
 * @author xkleteck
 */
public class LoadDynamic extends Button {
    protected static final Log LOG = new Log(LoadDynamic.class);
    private static LoaderController loaderController = Lookup.getDefault().lookup(LoaderController.class);

    public LoadDynamic(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/md_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
        String path = "/resources/dynamics/md/";
        String[] names = new String[] {"model.1", "model.2", "model.3", "model.4", "model.5", 
            "model.6", "model.7", "model.8", "model.9", "model.10"};
        File topology = copyResourceIntoTempFile(path, names[0] + ".pdb");
        if (topology == null) {
            return;
        }
        
        List<File> trajectoryFiles = new ArrayList<>();
        for (int i = 1; i < names.length; i++) {
            File file = copyResourceIntoTempFile(path, names[i] + ".pdb");
            if (file == null) {
                return;
            }
            trajectoryFiles.add(file);
        }
        
        loaderController.loadDynamics(topology, trajectoryFiles.toArray(new File[trajectoryFiles.size()]), 
                StructureFileFormat.PDB);
    }
    
    /**
     * Copies resource file into a new temporary file.
     * 
     * @param path Path to the resource
     * @param name Name of the resource
     * @return Temp file
     */
    private static File copyResourceIntoTempFile(String path, String name) {
        String fullPath = path + name;
        
        try {
            InputStream in = ButtonFunctions.class.getResourceAsStream(fullPath);
            if (in == null) {
                LOG.error("Couldnt find file: " + fullPath);
                return null;
            }

            String tempDir = System.getProperty("java.io.tmpdir");
            File tempFile = new File(tempDir, name);
            tempFile.deleteOnExit();

            try (FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile;
        } catch (IOException e) {
            LOG.error("Error while copying resource: " + fullPath, e);
            return null;
        }
    }
}
