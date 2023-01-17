package com.osiris.betterdesktop;

import org.lwjgl.opengl.GL11;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

public class UI {

    public static final int nullTexture;

    static {
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
        ByteBuffer imageBuffer = toByteBuffer(image);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, 16, 16, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, imageBuffer);

// set texture parameters
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
    }

    public static ByteBuffer toByteBuffer(BufferedImage image) {
        byte[] data;
        DataBuffer buffer = image.getRaster().getDataBuffer();
        if (buffer instanceof DataBufferByte) {
            data = ((DataBufferByte) buffer).getData();
        } else if (buffer instanceof DataBufferInt) {
            int[] intData = ((DataBufferInt) buffer).getData();
            data = new byte[intData.length * 4];
            for (int i = 0; i < intData.length; i++) {
                data[i * 4] = (byte) (intData[i] >> 24);
                data[i * 4 + 1] = (byte) (intData[i] >> 16);
                data[i * 4 + 2] = (byte) (intData[i] >> 8);
                data[i * 4 + 3] = (byte) intData[i];
            }
        } else {
            throw new IllegalArgumentException("Unsupported image type");
        }
        ByteBuffer bytebuffer = ByteBuffer.allocateDirect(data.length);
        bytebuffer.order(ByteOrder.nativeOrder());
        bytebuffer.put(data, 0, data.length);
        bytebuffer.flip();
        return bytebuffer;
    }

    public static int toTexture(ImageIcon icon) {
        if (icon == null) return nullTexture;
        BufferedImage image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        image.getGraphics().drawImage(icon.getImage(), 0, 0, null);
        int[] pixels = new int[image.getWidth() * image.getHeight()];
        image.getRGB(0, 0, image.getWidth(), image.getHeight(), pixels, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(image.getWidth() * image.getHeight() * 4);

        for (int h = 0; h < image.getHeight(); h++) {
            for (int w = 0; w < image.getWidth(); w++) {
                int pixel = pixels[h * image.getWidth() + w];

                buffer.put((byte) ((pixel >> 16) & 0xFF));
                buffer.put((byte) ((pixel >> 8) & 0xFF));
                buffer.put((byte) (pixel & 0xFF));
                buffer.put((byte) ((pixel >> 24) & 0xFF));
            }
        }

        buffer.flip();

        int id = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, id);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(),
                0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        return id;
    }
}
