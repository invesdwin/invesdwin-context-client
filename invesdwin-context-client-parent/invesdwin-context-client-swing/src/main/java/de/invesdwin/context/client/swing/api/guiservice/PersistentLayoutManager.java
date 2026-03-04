package de.invesdwin.context.client.swing.api.guiservice;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.annotation.concurrent.ThreadSafe;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import bibliothek.util.Path;
import de.invesdwin.context.client.swing.api.view.PlaceholderView;
import de.invesdwin.context.client.swing.frame.content.ContentPaneView;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.lang.string.Strings;
import jakarta.inject.Inject;

@ThreadSafe
public class PersistentLayoutManager {

    public static final String LAYOUT_FILE_NAME = "layout.xml";

    @Inject
    private ContentPane contentPane;
    @Inject
    private ContentPaneView contentPaneView;

    public void saveLayout(final File layoutFile) {
        //not yet
    }

    public void restoreLayout(final File layoutFile) {
        final Set<String> placeholders = extractPlaceholders(layoutFile);

        for (final String placeholder : placeholders) {
            final PlaceholderView placeholderView = new PlaceholderView(placeholder);
            contentPane.showView(placeholderView, WorkingAreaLocation.Center, false);
        }

        try {
            contentPaneView.getControl().readXML(layoutFile);
        } catch (final IOException e1) {
            //TODO: Log only in console or LogViewer ?
        }
    }

    private Set<String> extractPlaceholders(final File layoutFile) {
        final Set<String> result = ILockCollectionFactory.getInstance(false).newLinkedSet();

        try {
            final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            final Document doc = dBuilder.parse(layoutFile);
            doc.getDocumentElement().normalize();

            final NodeList nList = doc.getElementsByTagName("placeholder");
            for (int i = 0; i < nList.getLength(); i++) {
                final Node item = nList.item(i);
                final Node firstChild = item.getFirstChild();
                final String nodeValue = firstChild.getNodeValue();
                final String encodedViewId = Strings.substringAfterLast(nodeValue, '.');
                final String viewId = Path.decodeSegment(encodedViewId);
                result.add(viewId);
            }
        } catch (final Exception e) {
            //TODO: Log only in console or LogViewer ?
        }

        //We never replace the ContentPaneView
        result.remove(ContentPaneView.class.getSimpleName());
        return result;
    }

}
