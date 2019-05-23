package cz.caver.vr;

import org.openide.modules.OnStop;
import org.openide.util.Lookup;

/**
 * Disposes of all resources which {@link VirtualReality} allocated.
 * 
 * @author Peter Hutta <433395@mail.muni.cz>
 */
@OnStop
public class VirtualRealityDispose implements Runnable {

    @Override
    public void run() {
        Lookup.getDefault().lookup(VirtualReality.class).dispose();
    }
}