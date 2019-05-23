/*

 */

package cz.caver.vr.utils;

import com.jogamp.opengl.GL2GL3;
import com.jogamp.opengl.util.GLBuffers;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

/**
 *
 * @author Jiri Kletecka <433728@mail.muni.cz>
 */
public class Utils {
    private static final Charset CHARSET = Charset.forName("UTF-8");
    private static final CharsetEncoder ENCODER = CHARSET.newEncoder();
    private static final CharsetDecoder DECODER = CHARSET.newDecoder();
    
    public static boolean showConfirmBox(String title, String message) {
        return JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
    
    public static void showErrorBox(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    public static void saveImage(GL2GL3 gl, int width, int height) throws IOException {

            BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics graphics = screenshot.getGraphics();

            ByteBuffer buffer = GLBuffers.newDirectByteBuffer(width * height * 4);
            
            gl.glReadPixels(0, 0, width, height, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, buffer);

            for (int h = 0; h < height; h++) {
                for (int w = 0; w < width; w++) {
                    graphics.setColor(new Color((buffer.get() & 0xff), (buffer.get() & 0xff),
                            (buffer.get() & 0xff)));
                    buffer.get();   
                    graphics.drawRect(w, height - h, 1, 1);
                }
            }
            
            File outputfile = new File("C:\\temp\\texture.png");
            ImageIO.write(screenshot, "png", outputfile);
    }
    
    public static ByteBuffer getBufferFromString(String s) throws CharacterCodingException {
        ByteBuffer buffer = GLBuffers.newDirectByteBuffer(s.getBytes().length + 1);
        ENCODER.reset();
        ENCODER.encode(CharBuffer.wrap(s), buffer, false);
        ENCODER.encode(CharBuffer.wrap(""), buffer, true);
        ENCODER.flush(buffer);
        buffer.rewind();
        return buffer;
    }
    
    public static String getStringFromBuffer(ByteBuffer textBuffer) {
        byte[] bytes = new byte[textBuffer.remaining()];
        textBuffer.get(bytes);
        String text = new String(bytes);
        
        //Remove void chars
        text = text.replace(Character.toString('\0'), "");
        
        return text;
    }
}
