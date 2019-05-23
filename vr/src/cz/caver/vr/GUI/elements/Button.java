package cz.caver.vr.GUI.elements;

import com.jogamp.opengl.util.texture.Texture;
import cz.caver.renderer.RenderManager;
import cz.caver.renderer.StructureRenderingType;
import cz.caver.vr.GUI.IVRGUIPanel;
import cz.caver.vr.VirtualReality;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public abstract class Button extends UIElement {
    protected static final RenderManager rm = Lookup.getDefault().lookup(RenderManager.class);
    
    private boolean textureStretchX = true;
    private boolean textureStretchY = true;
    
    private boolean toggleable = false;
    private boolean isToggled = false;
    protected Texture toggledTexture;
    protected Texture untoggledTexture;
    
    public Button(IVRGUIPanel overlay) {
        super(overlay);
    }

    public final void setToggleable(boolean toggleable) {
        this.toggleable = toggleable;
    }

    public final boolean isToggleable() {
        return toggleable;
    }

    public boolean isTextureStretchX() {
        return textureStretchX;
    }

    public boolean isTextureStretchY() {
        return textureStretchY;
    }

    public void setTextureStretchX(boolean textureStretchX) {
        this.textureStretchX = textureStretchX;
    }

    public void setTextureStretchY(boolean textureStretchY) {
        this.textureStretchY = textureStretchY;
    }

    public boolean isToggled() {
        return isToggled;
    }
    
    @Override
    protected void onClick() {
        super.onClick();
        if(toggleable) {
            isToggled = !isToggled;
            if(isToggled) {
                defaultTexture = toggledTexture;
            } else {
                defaultTexture = untoggledTexture;
            }
        }
    }
    
    protected void deactivateAllRenderingTypes() {
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_LINES, true);
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_POINTS, true);
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_POINTS_NOBONDS, true);
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_DOTS, true);
        rm.deactivateStructureRenderingType(VirtualReality.VRRenderingType.STRUCTURE_VDW_AO, true);
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_STICKS_AO, true);
        rm.deactivateStructureRenderingType(StructureRenderingType.STRUCTURE_CARTOON, true);
    }
}
