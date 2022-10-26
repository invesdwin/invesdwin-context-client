package de.invesdwin.context.client.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public final class AdvancedBrowser {

    private static final String AT_REST = " ";

    private AdvancedBrowser(final String location) {
        final Display display = new Display();
        final Shell shell = new Shell(display);
        shell.setText("Advanced Browser");

        shell.setLayout(new FormLayout());

        final Composite controls = new Composite(shell, SWT.NONE);
        FormData data = new FormData();
        data.top = new FormAttachment(0, 0);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        controls.setLayoutData(data);

        final Label status = new Label(shell, SWT.NONE);
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        data.bottom = new FormAttachment(100, 0);
        status.setLayoutData(data);

        final Browser browser = new Browser(shell, SWT.WEBKIT);
        data = new FormData();
        data.top = new FormAttachment(controls);
        data.bottom = new FormAttachment(status);
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(100, 0);
        browser.setLayoutData(data);

        controls.setLayout(new GridLayout(7, false));

        Button button = new Button(controls, SWT.PUSH);
        button.setText("Back");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                browser.back();
            }
        });

        button = new Button(controls, SWT.PUSH);
        button.setText("Forward");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                browser.forward();
            }
        });

        button = new Button(controls, SWT.PUSH);
        button.setText("Refresh");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                browser.refresh();
            }
        });

        button = new Button(controls, SWT.PUSH);
        button.setText("Stop");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                browser.stop();
            }
        });

        final Text url = new Text(controls, SWT.BORDER);
        url.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        url.setFocus();

        button = new Button(controls, SWT.PUSH);
        button.setText("Go");
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(final SelectionEvent event) {
                browser.setUrl(url.getText());
            }
        });

        final Label throbber = new Label(controls, SWT.NONE);
        throbber.setText(AT_REST);

        shell.setDefaultButton(button);

        browser.addCloseWindowListener(new AdvancedCloseWindowListener());
        //        browser.addLocationListener(new AdvancedLocationListener(url));
        browser.addProgressListener(new AdvancedProgressListener(throbber));
        browser.addStatusTextListener(new AdvancedStatusTextListener(status));

        // Go to the initial URL
        if (location != null) {
            browser.setUrl(location);
            url.setText(location);
        }

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        display.dispose();
    }

    class AdvancedCloseWindowListener implements CloseWindowListener {
        @Override
        public void close(final WindowEvent event) {
            ((Browser) event.widget).getShell().close();
        }
    }

    class AdvancedLocationListener implements LocationListener {
        private final Text location;

        AdvancedLocationListener(final Text text) {
            location = text;
        }

        @Override
        public void changing(final LocationEvent event) {
            location.setText("Loading " + event.location + "...");
        }

        @Override
        public void changed(final LocationEvent event) {
            location.setText(event.location);
        }
    }

    class AdvancedProgressListener implements ProgressListener {
        private final Label progress;

        AdvancedProgressListener(final Label label) {
            progress = label;
        }

        @Override
        public void changed(final ProgressEvent event) {
            if (event.total != 0) {
                final int percent = event.current / event.total;
                progress.setText(percent + "%");
            } else {
                progress.setText("?");
            }
        }

        @Override
        public void completed(final ProgressEvent event) {
            progress.setText(AT_REST);
        }
    }

    class AdvancedStatusTextListener implements StatusTextListener {
        private final Label status;

        AdvancedStatusTextListener(final Label label) {
            status = label;
        }

        @Override
        public void changed(final StatusTextEvent event) {
            status.setText(event.text);
        }
    }

    public static void main(final String[] args) {
        new AdvancedBrowser("http://tradingview.com");
    }

}