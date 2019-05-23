package cz.caver.vr.GUI.buttons.impl;

import com.caversoft.caverapi.caver3.api.Caver3Params;
import com.caversoft.log.Log;
import com.caversoft.sites.Site;
import com.caversoft.sites.SiteController;
import com.caversoft.sites.SiteSource;
import com.caversoft.structure.Structure;
import com.caversoft.structure.primary.Atom;
import com.caversoft.tunnel.controller.TunnelController;
import com.caversoft.tunnel.model.Tunnel;
import com.caversoft.tunnel.model.TunnelResult;
import com.caversoft.tunnel.model.TunnelSnapshot;
import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.texture.TextureIO;
import cz.caver.controller.selection.GlobalSelection;
import cz.caver.controller.selection.SelectionController;
import cz.caver.gui.functions.Functions;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.renderable.RenderableStructure;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.GUI.elements.Button;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class ComputeTunnels extends Button {
    private static final Log LOG = new Log(ComputeTunnels.class);
    private static RequestProcessor rp = new RequestProcessor("CA-GUI-Tasks", 1, true);
    private static RequestProcessor.Task task = null;
    
    private static RenderManager renderManager = Lookup.getDefault().lookup(RenderManager.class);
    private static SiteController siteController = Lookup.getDefault().lookup(SiteController.class);
    private static TunnelController tunnelController = Lookup.getDefault().lookup(TunnelController.class);
    private static SelectionController selectionController = Lookup.getDefault().lookup(SelectionController.class);

    public ComputeTunnels(IVRGUIPanel panel) {
        super(panel);
        
        GL2GL3 gl = panel.getVR().getGL();
        defaultTexture = loadTexture(gl, "/resources/textures/tunnel_computation_128.png", TextureIO.PNG);
    }

    @Override
    protected void onClick() {
        super.onClick();
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
                LOG.error(ex);
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
}