package de.invesdwin.context.client.swing.internal;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;

import org.jdesktop.swingx.error.ErrorInfo;
import org.jdesktop.swingx.plaf.basic.BasicErrorPaneUI;

import de.invesdwin.context.log.error.Err;

@NotThreadSafe
public class DetailedErrorPaneUI extends BasicErrorPaneUI {

    @Override
    protected String getDetailsAsHTML(final ErrorInfo errorInfo) {
        if (errorInfo.getErrorException() != null) {
            //convert the stacktrace into a more pleasent bit of HTML
            final StringBuffer html = new StringBuffer("<html>");
            html.append("<h2>" + escapeXml(errorInfo.getTitle()) + "</h2>");
            html.append("<HR size='1' noshade>");
            html.append("<div></div>");
            html.append("<b>Message:</b>");
            html.append("<pre>");
            html.append("    " + escapeXml(errorInfo.getErrorException().toString()));
            html.append("</pre>");
            html.append("<b>Level:</b>");
            html.append("<pre>");
            html.append("    " + errorInfo.getErrorLevel());
            html.append("</pre>");
            html.append("<b>Stack Trace:</b>");
            html.append("<pre>");
            final Throwable ex = errorInfo.getErrorException();
            html.append(Err.getDetailedStackTrace(ex).replace("\n", "\n    "));
            html.append("</pre>");
            html.append("</html>");
            return html.toString();
        } else {
            return null;
        }
    }

    private static String escapeXml(final String input) {
        String s = input == null ? "" : input.replace("&", "&amp;");
        s = s.replace("<", "&lt;");
        s = s.replace(">", "&gt;");
        return s;
    }

    public static ComponentUI createUI(final JComponent c) {
        return new DetailedErrorPaneUI();
    }

}
