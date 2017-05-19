package de.invesdwin.context.client.wicket.examples.secure.kerberos;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.mount.MountPath;

import de.invesdwin.context.client.wicket.examples.AExampleWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
@MountPath(SpringKerberosSecurePage.MOUNT_PATH)
//@AuthorizeInstantiation(IExampleRoles.ADMIN) instead of auth-roles we use declarative security in the spring xml
public class SpringKerberosSecurePage extends AExampleWebPage {

    public static final String MOUNT_PATH = "springkerberossecure";

    public SpringKerberosSecurePage() {
        this(Model.of(new SpringKerberosSecure()));
    }

    public SpringKerberosSecurePage(final IModel<SpringKerberosSecure> model) {
        super(model);
        new GeneratedBinding(this).bind();
    }

}
