package de.invesdwin.common.client.wicket.generated.markup.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.agilecoders.wicket.core.markup.html.themes.bootstrap.BootstrapCssReference;
import de.invesdwin.norva.beanpath.annotation.Disabled;
import de.invesdwin.norva.beanpath.annotation.Tooltip;
import de.invesdwin.nowicket.generated.guiservice.GuiService;
import de.invesdwin.nowicket.generated.guiservice.OfferDownloadConfig;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;
import de.invesdwin.util.lang.uri.URIs;

@GeneratedMarkup
@NotThreadSafe
public class RedirectedModel extends AValueObject {

    private final AnotherSampleModel parent;
    private SampleModel nested = new SampleModel();
    private File file;
    private List<File> multiFiles;
    private final List<AnchorTableRow> anchorTable;

    public RedirectedModel(final AnotherSampleModel parent) {
        this.parent = parent;
        this.anchorTable = new ArrayList<AnchorTableRow>();
        anchorTable.add(new AnchorTableRow());
    }

    public SampleModel getNested() {
        return nested;
    }

    public void multiFileUpload(final List<File> multiFiles) {
        this.multiFiles = multiFiles;
    }

    public File multiFileDownload() throws IOException {
        final File tempFile = new File(GuiService.get().getSessionFolder(),
                getClass().getSimpleName() + "/multiFileDownload.zip");
        FileUtils.forceMkdir(tempFile.getParentFile());
        final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(tempFile));
        for (final File file : multiFiles) {
            final ZipEntry entry = new ZipEntry(file.getName());
            zip.putNextEntry(entry);
            IOUtils.copy(new FileInputStream(file), zip);
            zip.closeEntry();
        }
        zip.close();
        return tempFile;
    }

    public String disableMultiFileDownload() {
        if (multiFiles == null) {
            return "No multiple files uploaded yet!";
        }
        return null;
    }

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

    public void fileUpload(final File file) {
        this.file = file;
    }

    public File fileDownload() {
        return file;
    }

    public void offerFileDownload() {
        nested.setTwo("File download offered");
        GuiService.get().offerDownload(new OfferDownloadConfig(file));
    }

    public boolean disableOfferFileDownload() {
        return file == null;
    }

    public String disableFileDownload() {
        if (file == null) {
            return "No file uploaded yet!";
        }
        return null;
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

    public void setNested(final SampleModel nested) {
        this.nested = nested;
    }

    public AnotherSampleModel back() {
        return parent;
    }

    public String stringImg() {
        return "https://www.google.de/images/icons/product/chrome-48.png";
    }

    public boolean hideStringImg() {
        return true;
    }

    public URL urlImg() {
        return URIs.asUrl(stringImg());
    }

    public URI uriImg() {
        return URIs.asUri(stringImg());
    }

    @Tooltip("some tooltip on img")
    public String stringImgRelative() {
        return "icon.png";
    }

    public Url wicketUrlImgRelative() {
        return Url.parse(stringImgRelative());
    }

    public URI uriImgRelative() {
        return URIs.asUri(stringImgRelative());
    }

    public IResource resourcImg() {
        return resourceReferenceImg().getResource();
    }

    public ResourceReference resourceReferenceImg() {
        return new PackageResourceReference(getClass(), stringImgRelative());
    }

    public List<AnchorTableRow> getAnchorTable() {
        return anchorTable;
    }

}
