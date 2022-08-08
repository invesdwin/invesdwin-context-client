package de.invesdwin.context.client.swing.jfreechart.panel.helper.config.dialog.expression;

import java.awt.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.concurrent.Immutable;
import javax.swing.Icon;
import javax.swing.JLabel;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.AddSeriesPanel;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.AddSeriesPanelLayout;
import de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider;
import de.invesdwin.util.collections.Arrays;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.expression.IExpression;
import de.invesdwin.util.swing.Components;
import de.invesdwin.util.swing.icon.ChangeColorImageFilter;
import de.invesdwin.util.swing.text.ToolTipFormatter;

@Immutable
public final class ValidatingExpressionData {

    public static final Color COLOR_EXPRESSION_PENDING_INVALID = Color.RED;
    public static final Color COLOR_EXPRESSION_PENDING_VALIDATING = Color.ORANGE;
    public static final Color COLOR_EXPRESSION_PENDING_VALID = Color.GREEN.darker();

    public static final Icon ICON_EXPRESSION = AddSeriesPanelLayout.ICON_EXPRESSION;
    public static final Icon ICON_EXPRESSION_PENDING_INVALID = ChangeColorImageFilter
            .apply(AddSeriesPanelLayout.ICON_EXPRESSION, COLOR_EXPRESSION_PENDING_INVALID);
    public static final Icon ICON_EXPRESSION_PENDING_VALIDATING = ChangeColorImageFilter
            .apply(AddSeriesPanelLayout.ICON_EXPRESSION, COLOR_EXPRESSION_PENDING_VALIDATING);
    public static final Icon ICON_EXPRESSION_PENDING_VALID = ChangeColorImageFilter
            .apply(AddSeriesPanelLayout.ICON_EXPRESSION, COLOR_EXPRESSION_PENDING_VALID);
    public static final int TOOLTIP_WORD_WRAP_LIMIT = 120;
    public static final ToolTipFormatter SPACED_TOOLTIP_FORMATTER = Components.getDefaultToolTipFormatter()
            .clone()
            .setLineBreaks(
                    Arrays.addAll(new String[] { "<br>  " }, Components.getDefaultToolTipFormatter().getLineBreaks()));
    private static final String CLIENT_PROP = ValidatingExpressionData.class.getSimpleName();

    private final Future<?> future;
    private final String expression;

    private ValidatingExpressionData(final Future<?> future, final String expression) {
        this.future = future;
        this.expression = expression;
    }

    public static void validate(final IExpressionSeriesProvider provider, final JLabel lbl_expression,
            final String newExpression) {
        final ExecutorService executor = provider.getValidatingExecutor();
        if (executor != null) {
            synchronized (lbl_expression) {
                ValidatingExpressionData data = (ValidatingExpressionData) lbl_expression
                        .getClientProperty(CLIENT_PROP);
                if (data != null) {
                    if (!Objects.equals(data.expression, newExpression)) {
                        data.future.cancel(true);
                        data = null;
                        lbl_expression.putClientProperty(CLIENT_PROP, null);
                    }
                }
                if (data == null) {
                    data = validateAsync(provider, lbl_expression, newExpression, executor);
                    lbl_expression.putClientProperty(CLIENT_PROP, data);
                }
            }
        } else {
            validateSync(provider, lbl_expression, newExpression);
        }
    }

    private static void validateSync(final IExpressionSeriesProvider provider, final JLabel lbl_expression,
            final String newExpression) {
        try {
            final IExpression parsedExpression = provider.parseExpression(newExpression);
            if (parsedExpression == null) {
                lbl_expression.setIcon(ICON_EXPRESSION);
                Components.setToolTipText(lbl_expression, null, false);
                return;
            }
            lbl_expression.setIcon(ICON_EXPRESSION_PENDING_VALID);
            Components.setToolTipText(lbl_expression,
                    "<html><b>Valid:</b><br><pre>  "
                            + Strings.escapeHtml4(parsedExpression.toString().replace("\n", "\n  ")) + "</pre>",
                    false, SPACED_TOOLTIP_FORMATTER);
        } catch (final Throwable t) {
            lbl_expression.setIcon(ICON_EXPRESSION_PENDING_INVALID);
            Components.setToolTipText(lbl_expression,
                    "<html><b>Error:</b><br><pre>  " + AddSeriesPanel.prepareErrorMessageForTooltip(t) + "</pre>",
                    false, SPACED_TOOLTIP_FORMATTER);
        }
    }

    private static ValidatingExpressionData validateAsync(final IExpressionSeriesProvider provider,
            final JLabel lbl_expression, final String newExpression, final ExecutorService executor) {
        //set pending
        lbl_expression.setIcon(ICON_EXPRESSION_PENDING_VALIDATING);
        Components.setToolTipText(lbl_expression, null, false);
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    final IExpression parsedExpression = provider.parseExpression(newExpression);
                    if (parsedExpression == null) {
                        EventDispatchThreadUtil.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                lbl_expression.setIcon(ICON_EXPRESSION);
                                Components.setToolTipText(lbl_expression, null, false);
                            }
                        });
                        return;
                    }
                    EventDispatchThreadUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            lbl_expression.setIcon(ICON_EXPRESSION_PENDING_VALID);
                            Components.setToolTipText(lbl_expression, "<html><b>Valid:</b><br><pre>  "
                                    + Strings.escapeHtml4(parsedExpression.toString().replace("\n", "\n  ")) + "</pre>",
                                    false, SPACED_TOOLTIP_FORMATTER);
                        }
                    });
                } catch (final Throwable t) {
                    EventDispatchThreadUtil.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            lbl_expression.setIcon(ICON_EXPRESSION_PENDING_INVALID);
                            Components.setToolTipText(
                                    lbl_expression, "<html><b>Error:</b><br><pre>  "
                                            + AddSeriesPanel.prepareErrorMessageForTooltip(t) + "</pre>",
                                    false, SPACED_TOOLTIP_FORMATTER);
                        }
                    });
                }
            }
        };
        final Future<?> future = executor.submit(task);
        return new ValidatingExpressionData(future, newExpression);
    }

}