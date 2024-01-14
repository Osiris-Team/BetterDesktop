package com.author.core.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

public class AWT {
    public static RenderedImage convertToRenderedImage(ImageIcon imageIcon) {
        Image image = imageIcon.getImage();
        if (image instanceof RenderedImage) {
            return (RenderedImage) image;
        } else {
            BufferedImage bufferedImage = new BufferedImage(
                imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
            bufferedImage.getGraphics().drawImage(image, 0, 0, null);
            return bufferedImage;
        }
    }
}
