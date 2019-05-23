package cz.caver.vr.GUI.walls.impl;

import cz.caver.vr.GUI.GUIPanel;
import cz.caver.vr.GUI.buttons.impl.DeleteButton;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.buttons.impl.FindStructureButton;
import cz.caver.vr.GUI.buttons.impl.VisualizationToggleCartoon;
import cz.caver.vr.GUI.buttons.impl.VisualizationToggleSticks;
import cz.caver.vr.GUI.buttons.impl.VisualizationToggleVDW;
import cz.caver.vr.GUI.buttons.impl.VisualizationToggleWireframe;
import cz.caver.vr.GUI.elements.Label;
import java.awt.Color;
import java.awt.Font;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public final class StructureWall extends GUIPanel {
    private Label findLabel;
    public StructureWall(VirtualReality vr) {
        super(vr);
        this.key = StructureWall.class.getCanonicalName();
        this.name = StructureWall.class.getSimpleName();
        setWidth(vr.getPlayground().getXSize());
        setHeight(vr.getPlayground().getYSize());
        
        //Add buttons
        FindStructureButton findButton = new FindStructureButton(this);
        findButton.setLeftTopOffset(100, 100);
        findButton.setWidth(650);
        findButton.setHeight(100);
        uiElements.add(findButton);
        
        findLabel = new Label(this, "Download structure", new Font("SansSerif", Font.BOLD, 24));
        findLabel.setLeftTopOffset(120, 140);
        uiElements.add(findLabel);
        
        DeleteButton deleteButton = new DeleteButton(this);
        deleteButton.setLeftTopOffset(800, 100);
        deleteButton.setWidth(100);
        deleteButton.setHeight(100);
        uiElements.add(deleteButton);
        
        VisualizationToggleWireframe wireframeToggleButton = new VisualizationToggleWireframe(this);
        wireframeToggleButton.setLeftTopOffset(100, 250);
        wireframeToggleButton.setWidth(100);
        wireframeToggleButton.setHeight(100);
        uiElements.add(wireframeToggleButton);
        
        VisualizationToggleSticks sticksToggleButton = new VisualizationToggleSticks(this);
        sticksToggleButton.setLeftTopOffset(250, 250);
        sticksToggleButton.setWidth(100);
        sticksToggleButton.setHeight(100);
        uiElements.add(sticksToggleButton);
        
        VisualizationToggleVDW vdwToggleButton = new VisualizationToggleVDW(this);
        vdwToggleButton.setLeftTopOffset(400, 250);
        vdwToggleButton.setWidth(100);
        vdwToggleButton.setHeight(100);
        uiElements.add(vdwToggleButton);
        
        VisualizationToggleCartoon cartoonToggleButton = new VisualizationToggleCartoon(this);
        cartoonToggleButton.setLeftTopOffset(550, 250);
        cartoonToggleButton.setWidth(100);
        cartoonToggleButton.setHeight(100);
        uiElements.add(cartoonToggleButton);
        
        try {
            init(vr);
        } catch (Exception e) {
        }
        
        //Set transformation
        Matrix3f rot = new Matrix3f();
        rot.rotY((float) Math.PI);
        setRotation(rot);
        setTranslation(new Vector3f(0, vr.getPlayground().getYSize() / 2, vr.getPlayground().getZSize() / 2));
    }

    @Override
    public void onKeyboardInput(String currentInput) {
        findLabel.setText(currentInput);
    }
    
    @Override
    public void onHideKeyboard(boolean success) {
        super.onHideKeyboard(success);
        findLabel.setText("Download structure");
    }
    
    @Override
    public int getRenderTextureWidth() {
        return 1000;
    }

    @Override
    public Color getBackgroundColor() {
        return new Color(255, 255, 255, 255);
    }
}