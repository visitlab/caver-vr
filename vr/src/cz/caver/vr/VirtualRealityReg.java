package cz.caver.vr;

import cz.caver.renderer.RenderManager;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;

/**
 * Registers {@link VirtualReality} as rendering pre- and post- processor.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
@OnStart
public class VirtualRealityReg implements Runnable {

    @Override
    public void run() {
        RenderManager rm = Lookup.getDefault().lookup(RenderManager.class);
        VirtualReality vr = Lookup.getDefault().lookup(VirtualReality.class);
        
        rm.registerPreProcessor(vr);
        rm.registerPostProcessor(vr);
    }
}
