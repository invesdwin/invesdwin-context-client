package de.invesdwin.context.client.swing.layout;

import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.context.client.swing.api.annotation.DefaultCloseOperation;
import de.invesdwin.context.client.swing.api.guiservice.ContentPane;
import de.invesdwin.context.client.swing.api.view.AModel;
import de.invesdwin.context.client.swing.frame.content.WorkingAreaLocation;
import de.invesdwin.norva.beanpath.annotation.Forced;
import de.invesdwin.norva.beanpath.annotation.ModalCloser;

@ThreadSafe
public class AddView extends AModel {

    private final ContentPane contentPane;
    private String viewId;
    private WorkingAreaLocation workingAreaLocation;

    public AddView(final ContentPane contentPane) {
        this.contentPane = contentPane;
        this.workingAreaLocation = WorkingAreaLocation.East;
    }

    public String getViewId() {
        return viewId;
    }

    public void setViewId(final String viewId) {
        this.viewId = viewId;
    }

    public WorkingAreaLocation getWorkingAreaLocation() {
        return workingAreaLocation;
    }

    public void setWorkingAreaLocation(final WorkingAreaLocation workingAreaLocation) {
        this.workingAreaLocation = workingAreaLocation;
    }

    @ModalCloser
    public void ok() {
        this.contentPane.showView(new TestSaveRestoreLayoutModelView(new TestSaveRestoreLayoutModel(viewId)),
                workingAreaLocation);
    }

    @Forced
    @DefaultCloseOperation
    public void cancel() {}
}
