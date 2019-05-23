package cz.caver.vr.GUI.walls.impl;

import cz.caver.vr.GUI.GUIPanel;
import cz.caver.vr.VirtualReality;
import cz.caver.vr.GUI.buttons.impl.DynamicsNext;
import cz.caver.vr.GUI.buttons.impl.DynamicsPlay;
import cz.caver.vr.GUI.buttons.impl.DynamicsPrevious;
import cz.caver.vr.GUI.buttons.impl.DynamicsStop;
import cz.caver.vr.GUI.buttons.impl.LoadDynamic;
import java.awt.Color;
import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class DynamicsWall extends GUIPanel {
    
    public DynamicsWall(VirtualReality vr) {
        super(vr);
        this.key = DynamicsWall.class.getCanonicalName();
        this.name = DynamicsWall.class.getSimpleName();
        setWidth(vr.getPlayground().getZSize());
        setHeight(vr.getPlayground().getYSize());
        
        //Add buttons
        LoadDynamic dynamicButton = new LoadDynamic(this);
        dynamicButton.setLeftTopOffset(new Vector3f(75, getRenderTextureHeight() / 2, 0));
        dynamicButton.setWidth(100);
        dynamicButton.setHeight(100);
        uiElements.add(dynamicButton);
        
        DynamicsPrevious prevButton = new DynamicsPrevious(this);
        prevButton.setLeftTopOffset(new Vector3f(225, getRenderTextureHeight() / 2, 0));
        prevButton.setWidth(100);
        prevButton.setHeight(100);
        uiElements.add(prevButton);
        
        DynamicsPlay playButton = new DynamicsPlay(this);
        playButton.setLeftTopOffset(new Vector3f(375, getRenderTextureHeight() / 2, 0));
        playButton.setWidth(100);
        playButton.setHeight(100);
        uiElements.add(playButton);
        
        DynamicsStop stopButton = new DynamicsStop(this);
        stopButton.setLeftTopOffset(new Vector3f(525, getRenderTextureHeight() / 2, 0));
        stopButton.setWidth(100);
        stopButton.setHeight(100);
        uiElements.add(stopButton);
        
        DynamicsNext nextButton = new DynamicsNext(this);
        nextButton.setLeftTopOffset(new Vector3f(675, getRenderTextureHeight() / 2, 0));
        nextButton.setWidth(100);
        nextButton.setHeight(100);
        uiElements.add(nextButton);
        
        try {
            init(vr);
        } catch (Exception e) {
        }
        
        //Set transformation
        Matrix3f rot = new Matrix3f();
        rot.setIdentity();
        rot.rotY(3.0f / 2.0f * (float) Math.PI);
        setRotation(rot);
        setTranslation(new Vector3f(vr.getPlayground().getXSize() / 2, vr.getPlayground().getYSize() / 2, 0));
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