package de.invesdwin.context.client.wicket.examples.secure.cas;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.mount.MountPath;

import de.invesdwin.context.client.wicket.examples.AExampleWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
@MountPath(SpringCasSecurePage.MOUNT_PATH)
//@AuthorizeInstantiation(IExampleRoles.ADMIN) instead of auth-roles we use declarative security in the spring xml
public class SpringCasSecurePage extends AExampleWebPage {

    public static final String MOUNT_PATH = "springcassecure";

    public SpringCasSecurePage() {
        this(Model.of(new SpringCasSecure()));
    }

    public SpringCasSecurePage(final IModel<SpringCasSecure> model) {
        super(model);
        new GeneratedBinding(this).bind();
    }

}
