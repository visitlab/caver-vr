package cz.caver.vr.GUI;

import com.caversoft.caverapi.caver3.api.Caver3Params;
import com.caversoft.configuration.Configuration;
import com.caversoft.loader.LoaderController;
import com.caversoft.loader.LoaderTasks;
import com.caversoft.loader.StructureFileFormat;
import com.caversoft.loader.exception.UnknownStructureFormatException;
import com.caversoft.log.Log;
import com.caversoft.sites.Site;
import com.caversoft.sites.SiteController;
import com.caversoft.sites.SiteSource;
import com.caversoft.structure.Structure;
import com.caversoft.structure.controller.StructureController;
import com.caversoft.structure.dynamics.DynamicsController;
import com.caversoft.structure.primary.Atom;
import com.caversoft.tunnel.controller.TunnelController;
import com.caversoft.tunnel.model.Tunnel;
import com.caversoft.tunnel.model.TunnelResult;
import com.caversoft.tunnel.model.TunnelSnapshot;
import cz.caver.controller.selection.GlobalSelection;
import cz.caver.controller.selection.SelectionController;
import cz.caver.gui.functions.Functions;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.RenderingType;
import cz.caver.renderer.filter.StructureFilterManager;
import cz.caver.renderer.renderable.RenderableStructure;
import cz.caver.vr.GUI.elements.UIElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Task;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Provides functions for for buttons to manipulate the scene.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
public class ButtonFunctions {
    
    private static final Log log = new Log(ButtonFunctions.class);
    
    private static RequestProcessor rp = new RequestProcessor("CA-GUI-Tasks", 1, true);
    private static RequestProcessor.Task task = null;
    
    private static RenderManager renderManager = Lookup.getDefault().lookup(RenderManager.class);
    private static StructureFilterManager filterManager = Lookup.getDefault().lookup(StructureFilterManager.class);
    
    private static DynamicsController dynamicsController = Lookup.getDefault().lookup(DynamicsController.class);
    private static LoaderController loaderController = Lookup.getDefault().lookup(LoaderController.class);
    private static StructureController structureController = Lookup.getDefault().lookup(StructureController.class);
    private static SiteController siteController = Lookup.getDefault().lookup(SiteController.class);
    private static TunnelController tunnelController = Lookup.getDefault().lookup(TunnelController.class);
    private static SelectionController selectionController = Lookup.getDefault().lookup(SelectionController.class);
    
    private static Configuration configuration = Lookup.getDefault().lookup(Configuration.class);
    
    /**
     * Sets the next frame as the current frame of the current dynamic structure.
     */
    public static void nextFrameButton() {
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
    
    /**
     * Sets the previous frame as the current frame of the current dynamic structure.
     */
    public static void previousFrameButton() {
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
    
    /**
     * Plays or pauses the animation of the current dynamic structure.
     * 
     * @param button Used to decide whether to play or stop the animation
     */
    public static void playButton(UIElement button) {
        /*if (!button.isClicked()) {
            dynamicsController.play();
        } else {
            dynamicsController.pause();
        }*/
    }
    
    /**
     * Stops the animation of the current dynamic structure
     */
    public static void stopButton() {
        dynamicsController.stop();
    }
    
    /**
     * Loades given files and creates dynamic structure.
     * 
     * @param path Path to files
     * @param names PDB source files
     */
    public static void loadDynamicStructure(String path, String[] names) {
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
                log.error("Couldnt find file: " + fullPath);
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
            log.error("Error while copying resource: " + fullPath, e);
            return null;
        }
    }
    
    /**
     * Loades static structure with given code. If not found, source file is downloaded.
     * 
     * @param code PDB code of structure
     */
    public static void loadStaticStructure(String code) {
        String destination = (String) configuration.getParameter(Configuration.PARAM_CAVER_DOWNLOAD_DIR);
        File f = new File(destination + System.getProperty("file.separator") + code + ".pdb");
        if (!(f.exists() && !f.isDirectory())) {
            LoaderTasks.downloadStructure(code, true, true, new File(destination));
        } else {
            try {
                loaderController.loadStructure(f);
            } catch (UnknownStructureFormatException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
    
    /**
     * Computes hydrogens for all loaded structures.
     */
    public static void computeHydrogens() {
        Functions.computeHydrogens(new HashSet<>(structureController.getStructures()));
    }    
    
    /**
     * Toggles visibility of hydrogen atoms of all structures.
     * 
     * @param button Used to determine the visibility
     */
    public static void toggleHydrogenVisibility(UIElement button) {
        /*
        for (RenderableStructure rs : renderManager.getRenderableStructures()) {
            Structure structure = rs.getStructure();
            filterManager.setFilterEnabled(structure, AtomFilter.HYDROGEN, button.isClicked());
        }*/
    }
    
    /**
     * Computes tunnels of all loaded structures.
     */
    public static void computeTunnels() {
        Caver3Params caverParams = tunnelController.getParams();
        
        for (RenderableStructure rs : renderManager.getRenderableStructures()) {
            try {
                Structure structure = rs.getStructure();     
               
                if (structure.isDynamic()) {
                    siteController.setCurrentSite(new Site(structure, SiteSource.CUSTOM));
                    computeDynamicTunnels(structure, caverParams);
                } else {
                    List<Site> sites = siteController.getOrLoadSites(structure).get();
                    if (sites.size() == 0) {
                        continue;
                    }
                    siteController.setCurrentSite(sites.get(0));
                    Functions.computeStaticTunnels(caverParams, false);
                }
            } catch (InterruptedException | ExecutionException ex) {
                log.error(ex);
            } 
        }
    }
    
    /**
     * Performs computation of tunnels on the dynamic structure.
     *
     * @param structure structure
     * @param params CAVER 3 parameters
     */
    private static void computeDynamicTunnels(Structure structure, Caver3Params params) {
        final String pdbCode = structure.toString();

        int startFrame = params.getStartFrame();
        int endFrame = params.getEndFrame();

        final List<Integer> frames = new ArrayList<>();
        for (int i = startFrame; i <= endFrame; i += params.getSparsity()) {
            frames.add(i - 1);
        }

        final TunnelResult computedTunnels = new TunnelResult(tunnelController.generateTunnelResultId(), siteController.getCurrentSite(), tunnelController.getIncludedResidueTypes());

        // computation task is created
        task = rp.create(() -> {
            /*try {
                TunnelResult tmpResult = tunnelController.computeDynamicTunnels(structure, computedTunnels.getId(), frames, params, pdbCode);
                
                if (tmpResult.getTunnelCount() > 0) {
                    computedTunnels.addTunnels(tmpResult.getTunnels());
                    computedTunnels.setCaver3ConfigData(tmpResult.getCaver3SettingsData());
                    computedTunnels.setComputationDirectory(tmpResult.getComputationDirectory());
                    computedTunnels.setFilenames(tmpResult.getFilenames());
                }
            } catch (IOException ex) {
                Dialogs.fileManagementError();
            } catch (OutOfMemoryError ex) {
                Dialogs.outOfMemoryError();
            } catch (CalculationException ex) {
                TunnelDialogsHelper.channelComputationFailed(ex);
            } catch (StartingPointOutsideStructureException ex) {
                TunnelDialogsHelper.startingPointOutsideTheStructure();
            } catch (Exception ex) {
                Dialogs.genericException(ex);
            }*/
        });

        // on finish task
        task.addTaskListener((Task t) -> {
            WindowManager.getDefault().invokeWhenUIReady(() -> {
                if (computedTunnels.getTunnelCount() > 0) {
                    tunnelController.addTunnelResult(structure, computedTunnels, params);
                    
                    selectBottleneckResidues(computedTunnels.getTunnel(0), 1, 4);
                }
            });
        });

        // start task
        task.schedule(0);
    }

    /**
     * Select bottleneck residues of a tunnel snapshot at specified distance.
     * 
     * @param tunnel tunnel whose bottleneck residues to select
     * @param snapshot snapshot of a tunnel
     * @param distance distance at which atoms are selected
     */
    private static void selectBottleneckResidues(Tunnel tunnel, int snapshot, int distance) {
        TunnelSnapshot tunnelSnapshot = tunnel.getTunnelSnapshotByIndex(snapshot);
        
        // get bottleneck sphere
        Vector4f bottleneck = tunnelSnapshot.getSphere(0);
        for (int i = 1; i < tunnelSnapshot.getSphereCount(); i++) {
            Vector4f sphere = tunnelSnapshot.getSphere(i);
            if (sphere.w < bottleneck.w) {
                bottleneck = sphere;
            }
        }
        
        
        // select residues of all atoms at a specified distnace
        List<Atom> atoms = tunnel.getStructure().getAllAtoms();
        GlobalSelection bottleneckSelection = selectionController.addSelection("MD");
        
        for (Atom atom : atoms) {
            Vector3f delta = new Vector3f(bottleneck.x, bottleneck.y, bottleneck.z);
            delta.sub(atom.getPosition(snapshot));

            if (delta.length() <= distance) {
                bottleneckSelection.select(atom.getResidue());
            }
        }
    }
    
    /**
     * Activates / deactivates given rendering types for all tunnels.
     * 
     * @param renderingTypes Rendering types to activate / deactivate
     * @param button Used to determine activation / deactivation
     */
    public static void setTunnelRenderingType(List<RenderingType> renderingTypes, UIElement button) {
        /*for (RenderingType renderingType : renderingTypes) {
            if (!button.isClicked()) {
                renderManager.activateTunnelRenderingType(renderingType, false);
            } else {
                renderManager.deactivateTunnelRenderingType(renderingType, false);
            }
        }*/
    }
    
    /**
     * Activates / deactivates given rendering types for all structures.
     * 
     * @param renderingTypes Rendering types to activate / deactivate
     * @param button Used to determine activation / deactivation
     */
    public static void setStructureRenderingType(List<RenderingType> renderingTypes, UIElement button) {
        /*
        for (RenderingType renderingType : renderingTypes) {
            if (!button.isClicked()) {
                renderManager.activateStructureRenderingType(renderingType, false);
            } else {
                renderManager.deactivateStructureRenderingType(renderingType, false);
            }
        }
        */
    }
}
