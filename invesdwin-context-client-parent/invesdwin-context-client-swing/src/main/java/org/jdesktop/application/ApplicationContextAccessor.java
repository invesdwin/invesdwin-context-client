package org.jdesktop.application;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ApplicationContextAccessor {

    private ApplicationContextAccessor() {}

    public static void setResourceManager(final ApplicationContext context, final ResourceManager resourceManager) {
        context.setResourceManager(resourceManager);
    }

}
