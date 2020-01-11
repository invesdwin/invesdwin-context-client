package de.invesdwin.context.client.swing.frame.content;

import java.awt.Dimension;

import javax.annotation.concurrent.NotThreadSafe;

import bibliothek.extension.gui.dock.theme.EclipseTheme;
import bibliothek.extension.gui.dock.theme.eclipse.EclipseColorScheme;
import bibliothek.extension.gui.dock.theme.eclipse.rex.RexSystemColor;
import bibliothek.extension.gui.dock.theme.eclipse.stack.tab.RectGradientPainter;
import bibliothek.extension.gui.dock.theme.flat.FlatStationPaint;
import bibliothek.gui.DockController;
import bibliothek.gui.DockUI;
import bibliothek.gui.dock.dockable.ScreencaptureMovingImageFactory;
import bibliothek.gui.dock.util.Priority;
import bibliothek.gui.dock.util.laf.LookAndFeelColors;
import bibliothek.util.Colors;

@NotThreadSafe
public class CustomTheme extends EclipseTheme {

    public CustomTheme() {
        setPaint(new FlatStationPaint(), Priority.DEFAULT);
        setMovingImageFactory(new ScreencaptureMovingImageFactory(new Dimension(300, 200)), Priority.DEFAULT);
    }

    @Override
    public void install(final DockController controller) {
        super.install(controller);
        controller.getProperties().set(TAB_PAINTER, RectGradientPainter.FACTORY);
        controller.getProperties().set(ECLIPSE_COLOR_SCHEME, new EclipseColorScheme() {
            @Override
            protected void updateUI() {
                super.updateUI();
                setColor("stack.tab.border", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.border.selected", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.border.selected.focused", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.border.selected.focuslost", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.border.disabled",
                        Colors.brighter(DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND)));

                setColor("stack.tab.top", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.top.disabled", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.top.selected", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.top.selected.focused",
                        Colors.darker(DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND), 0.15D));
                setColor("stack.tab.top.selected.focuslost", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.top.disabled",
                        Colors.brighter(DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND)));

                setColor("stack.tab.bottom", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.bottom.disabled", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.bottom.selected", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.bottom.selected.focused", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.bottom.selected.focuslost", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));
                setColor("stack.tab.bottom.disabled",
                        Colors.brighter(DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND)));

                setColor("stack.tab.text", DockUI.getColor(LookAndFeelColors.PANEL_FOREGROUND));
                setColor("stack.tab.text.selected", DockUI.getColor(LookAndFeelColors.PANEL_FOREGROUND));
                setColor("stack.tab.text.selected.focused", DockUI.getColor(LookAndFeelColors.PANEL_FOREGROUND));
                setColor("stack.tab.text.selected.focuslost", DockUI.getColor(LookAndFeelColors.PANEL_FOREGROUND));
                setColor("stack.tab.text.disabled",
                        Colors.brighter(DockUI.getColor(LookAndFeelColors.PANEL_FOREGROUND)));

                setColor("stack.border", RexSystemColor.getBorderColor());
                setColor("stack.border.edges", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));

                setColor("flap.button.border.inner", Colors.brighter(RexSystemColor.getBorderColor(), 0.7));
                setColor("flap.button.border.outer", RexSystemColor.getBorderColor());
                setColor("flap.button.border.edge", DockUI.getColor(LookAndFeelColors.PANEL_BACKGROUND));

                setColor("selection.border", RexSystemColor.getBorderColor());
            }
        });
    }

}
