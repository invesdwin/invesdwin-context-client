package org.jdesktop.application;

import javax.annotation.concurrent.Immutable;

@Immutable
public final class ApplicationAccessor {

    private ApplicationAccessor() {}

    public static <T extends Application> T create(final Class<T> applicationClass) throws Exception {
        return Application.create(applicationClass);
    }

}
