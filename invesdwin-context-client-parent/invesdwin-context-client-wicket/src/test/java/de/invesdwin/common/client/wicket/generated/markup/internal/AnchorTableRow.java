package de.invesdwin.common.client.wicket.generated.markup.internal;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.core.markup.html.themes.bootstrap.BootstrapCssReference;
import de.invesdwin.norva.beanpath.annotation.Disabled;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.lang.uri.URIs;

@NotThreadSafe
public class AnchorTableRow extends AValueObject {

    public String stringUrlLink() {
        return "http://google.com";
    }

    public String getGetterStringUrlLink() {
        return "http://google.com";
    }

    public Url wicketUrlLink() {
        return Url.parse(stringUrlLink());
    }

    @Disabled("some link disabled reason")
    public URL urlLink() {
        return URIs.asUrl(stringUrlLink());
    }

    public URI uriLink() {
        return URIs.asUri(stringUrlLink());
    }

    public String stringUrlLinkRelative() {
        return "relative.html";
    }

    public Url wicketUrlLinkRelative() {
        return Url.parse(stringUrlLinkRelative());
    }

    public URI uriLinkRelative() {
        return URIs.asUri(stringUrlLinkRelative());
    }

    public File fileDownload() throws IOException {
        final File file = new File(GuiService.get().getSessionFolder(), getClass().getSimpleName() + ".txt");
        FileUtils.deleteQuietly(file);
        FileUtils.writeStringToFile(file, "asdf");
        return file;
    }

    public IResource resourceDownload() {
        return resourceReferenceDownload().getResource();
    }

    public ResourceReference resourceReferenceDownload() {
        return BootstrapCssReference.instance();
    }

    public String resourceDownloadTitle() {
        return "Download \"bootstrap.css\" Resource";
    }

}
