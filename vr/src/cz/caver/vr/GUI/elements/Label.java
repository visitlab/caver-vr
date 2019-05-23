package cz.caver.vr.GUI.elements;

import cz.caver.vr.GUI.IVRGUIPanel;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Label extends UIElement {
    private Color color = Color.black;
    private Font font;
    private String text;

    public Label(IVRGUIPanel panel) {
        super(panel);
    }
    
    public Label(IVRGUIPanel panel, String text, Font font) {
        super(panel);
        setText(text);
        setFont(font);
    }
    
    public Label(IVRGUIPanel panel, String text, Font font, Color color) {
        super(panel);
        setText(text);
        setFont(font);
        setColor(color);
    }
    
    public Color getColor() {
        return color;
    }

    public final void setColor(Color color) {
        hasChanged = true;
        this.color = color;
    }

    public Font getFont() {
        return font;
    }

    public final void setFont(Font font) {
        hasChanged = true;
        this.font = font;
    }

    public String getText() {
        return text;
    }

    public final void setText(String text) {
        hasChanged = true;
        this.text = text;
    }
}
