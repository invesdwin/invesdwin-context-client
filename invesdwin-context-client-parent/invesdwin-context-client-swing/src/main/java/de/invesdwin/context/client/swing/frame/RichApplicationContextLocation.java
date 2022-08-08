package de.invesdwin.context.client.swing.frame;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Named;

import org.springframework.core.io.ClassPathResource;

import de.invesdwin.context.beans.init.locations.AConditionalContextLocation;
import de.invesdwin.context.beans.init.locations.PositionedResource;
import de.invesdwin.context.beans.init.locations.position.ResourcePosition;
import de.invesdwin.util.collections.Arrays;

@ThreadSafe
@Named
public class RichApplicationContextLocation extends AConditionalContextLocation {

    private static volatile boolean activated = true;

    @Override
    protected List<PositionedResource> getContextResourcesIfConditionSatisfied() {
        return Arrays.asList(getContextLocation());
    }

    public static PositionedResource getContextLocation() {
        return PositionedResource.of(new ClassPathResource("/META-INF/ctx.richapplication.xml"),
                ResourcePosition.START);
    }

    @Override
    protected boolean isConditionSatisfied() {
        return activated;
    }

    public static void activate() {
        activated = true;
    }

    public static void deactivate() {
        activated = false;
    }

    public static boolean isActivated() {
        return activated;
    }

}
