package de.invesdwin.context.client.javafx.component.swing;

import java.awt.BorderLayout;

import javax.annotation.concurrent.NotThreadSafe;
import javax.swing.JPanel;

import de.invesdwin.context.client.javafx.util.annotation.FxApplicationThread;
import de.invesdwin.context.client.javafx.util.annotation.FxApplicationThread.InvocationType;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

// https://gist.github.com/anjackson/1640654
// https://stackoverflow.com/questions/33823670/how-can-i-set-a-javafx-webview-as-big-as-the-scene
@NotThreadSafe
public class SwingWebViewPanel extends JPanel {

    private Stage stage;
    private WebView webView;
    private JFXPanel jfxPanel;
    private WebEngine webEngine;

    public SwingWebViewPanel() {
        initComponents();
    }

    private void initComponents() {

        jfxPanel = new JFXPanel();
        createScene();

        setLayout(new BorderLayout());
        add(jfxPanel, BorderLayout.CENTER);
    }

    public WebView getWebView() {
        return webView;
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    @FxApplicationThread(InvocationType.RUN_AND_WAIT)
    private void createScene() {
        stage = new Stage();

        stage.setResizable(true);

        final StackPane root = new StackPane();
        final Scene scene = new Scene(root);
        stage.setScene(scene);

        webView = new WebView();
        webView.setContextMenuEnabled(true);
        webEngine = webView.getEngine();
        root.getChildren().add(webView);

        jfxPanel.setScene(scene);
    }
}