package com.osiris.betterdesktop;

import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class UI {

    private static int nullTexture = -1;

    public static int getNullTexture() {
        if(nullTexture == -1){
            // Create the nullTexture which is 16x16 with white background
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < 16; x++) { // fill the image with a white background
                for (int y = 0; y < 16; y++) {
                    image.setRGB(x, y, 0xffffffff);
                }
            }

// convert the image to a texture
            nullTexture = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, nullTexture);

// upload the image data to the texture
            ByteBuffer imageBuffer = toBGRAByteBuffer(image);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 16, 16, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);

// set texture parameters
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        }
        return nullTexture;
    }

    public static int toTexture(ImageIcon icon) {
        if (icon == null) return nullTexture;
        BufferedImage imgBuf = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        imgBuf.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        ByteBuffer buffer = toBGRAByteBuffer(imgBuf);

        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, imgBuf.getWidth(), imgBuf.getHeight(),
                0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        return id;
    }

    private static ByteBuffer toBGRAByteBuffer(BufferedImage imgBuf) {
        int[] pixels = new int[imgBuf.getWidth() * imgBuf.getHeight()];
        imgBuf.getRGB(0, 0, imgBuf.getWidth(), imgBuf.getHeight(), pixels, 0, imgBuf.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(imgBuf.getWidth() * imgBuf.getHeight() * 4);

        for (int h = 0; h < imgBuf.getHeight(); h++) {
            for (int w = 0; w < imgBuf.getWidth(); w++) {
                int pixel = pixels[h * imgBuf.getWidth() + w];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();
        return buffer;
    }

    public static Img loadImageFromStream(InputStream stream) throws IOException {
        BufferedImage image = ImageIO.read(stream);
        ByteBuffer buffer = toBGRAByteBuffer(image);
        return new Img(image, buffer);
    }

    public static Img loadImageFromFile(File file) throws IOException {
        BufferedImage image = ImageIO.read(file);
        ByteBuffer buffer = toBGRAByteBuffer(image);
        return new Img(image, buffer);
    }

    public static class Img{
        public BufferedImage bufferedImage;
        public ByteBuffer byteBuffer;

        public Img(BufferedImage bufferedImage, ByteBuffer byteBuffer) {
            this.bufferedImage = bufferedImage;
            this.byteBuffer = byteBuffer;
        }
    }
}
