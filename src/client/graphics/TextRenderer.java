package client.graphics;

import client.ClientSettings;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;


public class TextRenderer {

    //Constants
    private final Map<Integer,String> CHARS = new HashMap<Integer,String>() {{
        put(0, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        put(1, "abcdefghijklmnopqrstuvwxyz");
        put(2, "0123456789:,.()");
        put(3, "abcdefghijklmnopqrstuvwxyz");
        put(4, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        //put(4, "-*/=%\"'#@&_(),.;:?!\\|<>[]§`^~");
    }};

    //Variables
    private java.awt.Font font;
    private FontMetrics fontMetrics;
    private BufferedImage bufferedImage;
    private int fontTextureId;
    private java.awt.Color colour;
    public enum Alignment{
        LEFT, RIGHT, CENTRE
    };


    public TextRenderer(int size)  {
        this.font = new java.awt.Font("Agency FB", java.awt.Font.BOLD, size);

        //Generate buffered image
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        Graphics2D graphics = gc.createCompatibleImage(1, 1, Transparency.TRANSLUCENT).createGraphics();
        graphics.setFont(font);
        graphics.setColor(Color.black);

        fontMetrics = graphics.getFontMetrics();
        bufferedImage = graphics.getDeviceConfiguration().createCompatibleImage((int) getFontImageWidth(),(int) getFontImageHeight(),Transparency.TRANSLUCENT);

        //Generate texture
        fontTextureId = glGenTextures();
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, fontTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA,(int) getFontImageWidth(),(int) getFontImageHeight(),0, GL_RGBA, GL_UNSIGNED_BYTE, asByteBuffer());

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glBindTexture(GL_TEXTURE_2D, 0);
    }


    void drawText(String text, float x, float y) {
        drawText(text, x, y, Alignment.LEFT);
    }

    void drawText(String text, float x, float y, Alignment alignment) {
        glBindTexture(GL_TEXTURE_2D, this.fontTextureId);
        glBegin(GL_QUADS);
        y = ClientSettings.SCREEN_HEIGHT - y - getCharHeight();

        if (alignment == Alignment.RIGHT || alignment == Alignment.CENTRE) {
            float xShift = -getStringWidth(text);

            if (alignment == Alignment.CENTRE) {
                xShift /= 2;
                y -= getCharHeight()/2;
            }
            x += xShift;
        }

        float xTmp = x;
        for (char c : text.toCharArray()) {
            float width = getCharWidth(c);
            float height = getCharHeight();
            float cw = 1f / getFontImageWidth() * width;
            float ch = 1f / getFontImageHeight() * height;
            float cx = 1f / getFontImageWidth() * getCharX(c);
            float cy = 1f / getFontImageHeight() * getCharY(c);

            glTexCoord2f(cx, cy);
            glVertex3f(xTmp, y, 0);

            glTexCoord2f(cx + cw, cy);
            glVertex3f(xTmp + width, y, 0);

            glTexCoord2f(cx + cw, cy + ch);
            glVertex3f(xTmp + width, y + height, 0);

            glTexCoord2f(cx, cy + ch);
            glVertex3f(xTmp, y + height, 0);

            xTmp += width;
        }

        glEnd();
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    //Getters
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private float getFontImageWidth() {
        return (float) CHARS.values().stream().mapToDouble(e -> fontMetrics.getStringBounds(e, null).getWidth()).max().getAsDouble();
    }

    private float getFontImageHeight() {
        return (float) CHARS.keySet().size() * (this.getCharHeight());
    }

    private float getCharX(char c) {
        String originStr = CHARS.values().stream().filter(e -> e.contains("" + c)).findFirst().orElse("" + c);
        return (float) fontMetrics.getStringBounds(originStr.substring(0, originStr.indexOf(c)), null).getWidth();
    }

    private float getCharY(char c) {
        float lineId = (float) CHARS.keySet().stream().filter(i -> CHARS.get(i).contains("" + c)).findFirst().orElse(0);
        return this.getCharHeight() * lineId;
    }
    private float getCharWidth(char c) {
        return fontMetrics.charWidth(c);
    }

    float getCharHeight() {
        return (float) (fontMetrics.getMaxAscent() + fontMetrics.getMaxDescent());
    }

    float getStringWidth(String string) {
        float width = 0;
        for (char c : string.toCharArray()) {
            width += getCharWidth(c);
        }
        return width;
    }

    public Color getColour()
    {
        return colour;
    }

    //setter
    public void setColour(Color col)
    {
        this.colour = col;
    }
    //Conversions
    private ByteBuffer asByteBuffer() {

        ByteBuffer byteBuffer;

        //Draw the characters on our image
        Graphics2D imageGraphics = (Graphics2D) bufferedImage.getGraphics();
        imageGraphics.setFont(font);
        imageGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        imageGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // draw every CHAR by line...
        imageGraphics.setColor(colour);
        CHARS.keySet().forEach(i -> imageGraphics.drawString(CHARS.get(i), 0, fontMetrics.getMaxAscent() + (this.getCharHeight() * i)));

        //Generate texture data
        int[] pixels = new int[bufferedImage.getWidth() * bufferedImage.getHeight()];
        bufferedImage.getRGB(0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), pixels, 0, bufferedImage.getWidth());
        byteBuffer = ByteBuffer.allocateDirect((bufferedImage.getWidth() * bufferedImage.getHeight() * 4));
        
       for (int y=bufferedImage.getHeight()-1;y>=0;y--) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                int pixel = pixels[y * bufferedImage.getWidth() + x];
                byteBuffer.put((byte) ((pixel >> 16) & 0xFF));   // Red component
                byteBuffer.put((byte) ((pixel >> 8) & 0xFF));    // Green component
                byteBuffer.put((byte) (pixel & 0xFF));           // Blue component
                byteBuffer.put((byte) ((pixel >> 24) & 0xFF));   // Alpha component. Only for RGBA
            }
        }

        byteBuffer.flip();

        return byteBuffer;
    }
}