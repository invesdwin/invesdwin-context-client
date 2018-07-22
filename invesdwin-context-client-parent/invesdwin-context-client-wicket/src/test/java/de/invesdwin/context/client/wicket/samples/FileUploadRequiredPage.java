package de.invesdwin.context.client.wicket.samples;

import java.util.Optional;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;

import de.invesdwin.nowicket.application.AWebPage;

// @NotThreadSafe
public class FileUploadRequiredPage extends AWebPage {

    public FileUploadRequiredPage() {
        super(null);
        final Form<Object> form = new Form<Object>("form");
        form.setOutputMarkupId(true);
        add(form);
        final FeedbackPanel feedback = new FeedbackPanel("feedback");
        form.add(feedback);
        final TextField<String> textField = new TextField<String>("text", Model.of("asdf")) {
            @Override
            public boolean isRequired() {
                return true;
            }
        };
        form.add(textField);
        final FileUploadField file = new FileUploadField("file");
        form.add(file);
        final AjaxFallbackButton submit = new AjaxFallbackButton("submit", form) {
            @Override
            protected void onSubmit(final Optional<AjaxRequestTarget> target) {
                if (target.isPresent()) {
                    target.get().add(getForm());
                }
            }

            @Override
            protected void onError(final Optional<AjaxRequestTarget> target) {
                onSubmit(target);
            }

        };
        form.add(submit);
    }
}
