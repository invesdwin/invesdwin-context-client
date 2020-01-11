package de.invesdwin.context.client.swing.frame.splash;

import java.util.Locale;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.richclient.application.splash.AbstractSplashScreen;

import de.invesdwin.context.client.swing.frame.RichApplicationProperties;

/**
 * So that Spring-RCP is happy and the splash title can be set comfortably.
 * 
 */
@Immutable
public class ApplicationMessageSource implements MessageSource {

    private final ResourceMap resourceMap;

    public ApplicationMessageSource() {
        resourceMap = RichApplicationProperties.getDesignTimeApplication().getContext().getResourceMap();
    }

    @Override
    public String getMessage(final String code, final Object[] args, final String defaultMessage, final Locale locale) {
        return getFrameTitle(code);
    }

    @Override
    public String getMessage(final String code, final Object[] args, final Locale locale) {
        return getFrameTitle(code);
    }

    @Override
    public String getMessage(final MessageSourceResolvable resolvable, final Locale locale) {
        throw new UnsupportedOperationException("No clear code to get!");
    }

    private String getFrameTitle(final String code) {
        if (AbstractSplashScreen.SPLASH_TITLE_KEY.equals(code)) {
            //Relay to correct property
            return resourceMap.getString(Application.KEY_APPLICATION_TITLE);
        } else if (Application.KEY_APPLICATION_ICON.equals(code)) {
            return resourceMap.getString(code);
        } else {
            return resourceMap.getString(code);
        }
    }

}
