package de.invesdwin.context.client.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.annotation.concurrent.NotThreadSafe;
import javax.imageio.ImageIO;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.boxicons.BoxiconsSolid;
import org.kordamp.ikonli.elusive.Elusive;
import org.kordamp.ikonli.icomoon.Icomoon;
import org.kordamp.ikonli.jam.Jam;
import org.kordamp.ikonli.material2.Material2MZ;
import org.kordamp.ikonli.material2.Material2RoundMZ;
import org.kordamp.ikonli.material2.Material2SharpMZ;
import org.kordamp.ikonli.materialdesign.MaterialDesign;
import org.kordamp.ikonli.materialdesign2.MaterialDesignP;
import org.kordamp.ikonli.remixicon.RemixiconMZ;
import org.kordamp.ikonli.swing.FontIcon;
import org.kordamp.ikonli.typicons.Typicons;
import org.kordamp.ikonli.win10.Win10;

import de.invesdwin.context.ContextProperties;

@NotThreadSafe
public final class ExportIcons {

    private ExportIcons() {}

    //CHECKSTYLE:OFF
    public static void main(final String[] args) throws IOException {
        //CHECKSTYLE:ON
        exportIcon(newIcon(Elusive.PHONE), "phone");
        exportIcon(newIcon(Win10.ANGLE_LEFT), "angle_left");
        exportIcon(newIcon(Win10.ANGLE_RIGHT), "angle_right");
        exportIcon(newIcon(Win10.PIN_3), "win10_pin3");
        exportIcon(newIcon(BoxiconsSolid.PIN), "boxicon_pin");
        exportIcon(newIcon(Icomoon.ICM_PUSHPIN), "icm_pushpin");
        exportIcon(newIcon(Jam.PIN), "jam_pin");
        exportIcon(newIcon(Jam.PIN_F), "jam_pin_f");
        exportIcon(newIcon(Material2MZ.PIN), "material2mz_pin");
        exportIcon(newIcon(Material2MZ.PIN_OFF), "material2mz_pin_off");
        exportIcon(newIcon(Material2MZ.PUSH_PIN), "material2mz_push_pin");
        exportIcon(newIcon(Material2RoundMZ.PIN), "material2roundmz_pin");
        exportIcon(newIcon(Material2RoundMZ.PIN_OFF), "material2roundmz_pin_off");
        exportIcon(newIcon(Material2RoundMZ.PUSH_PIN), "material2roundmz_push_pin");
        exportIcon(newIcon(Material2SharpMZ.PIN), "material2sharpmz_pin");
        exportIcon(newIcon(Material2SharpMZ.PIN_OFF), "material2sharpmz_pin_off");
        exportIcon(newIcon(Material2SharpMZ.PUSH_PIN), "material2sharpmz_push_pin");
        exportIcon(newIcon(MaterialDesign.MDI_PIN), "materialdesign_pin");
        exportIcon(newIcon(MaterialDesign.MDI_PIN_OFF), "materialdesign_pin_off");

        exportIcon(newIcon(MaterialDesignP.PIN), "materialdesignp_pin");
        exportIcon(newIcon(MaterialDesignP.PIN_OFF), "materialdesignp_pin_off");
        exportIcon(newIcon(MaterialDesignP.PIN_OFF_OUTLINE), "materialdesignp_pin_off_outline");
        exportIcon(newIcon(MaterialDesignP.PIN_OUTLINE), "materialdesignp_pin_outline");
        exportIcon(newIcon(MaterialDesignP.PIN_OFF), "materialdesignp_pin_off");

        exportIcon(newIcon(RemixiconMZ.PUSHPIN_2_FILL), "remixiconmz_pushpin_2_fill");
        exportIcon(newIcon(RemixiconMZ.PUSHPIN_2_LINE), "remixiconmz_pushpin_2_line");

        exportIcon(newIcon(Typicons.PIN), "typicons_pin");
        exportIcon(newIcon(Typicons.PIN_OUTLINE), "typicons_pin_outline");
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
