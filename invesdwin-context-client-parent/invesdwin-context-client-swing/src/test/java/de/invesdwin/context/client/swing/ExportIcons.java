package de.invesdwin.context.client.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;
import javax.imageio.ImageIO;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.ionicons4.Ionicons4IOS;
import org.kordamp.ikonli.swing.FontIcon;

import de.invesdwin.context.ContextProperties;

@NotThreadSafe
public final class ExportIcons {

    private ExportIcons() {}

    //CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
        //CHECKSTYLE:ON
        exportIcon(newIcon(Ionicons4IOS.SETTINGS), "configure");

    }

    private static FontIcon newIcon(final Ikon iconType) {
        return FontIcon.of(iconType, 256, new Color(20, 20, 20));
    }

    private static void exportIcon(final FontIcon icon, final String name) throws IOException {
        final BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                BufferedImage.TYPE_4BYTE_ABGR);

        final Graphics2D g2 = bi.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        ImageIO.write(bi, "png", new File(ContextProperties.getCacheDirectory(), name + ".png"));
    }

}
