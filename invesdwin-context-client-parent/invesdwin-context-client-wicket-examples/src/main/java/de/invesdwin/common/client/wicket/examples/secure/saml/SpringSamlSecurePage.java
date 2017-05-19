package de.invesdwin.common.client.wicket.examples.secure.saml;

import javax.annotation.concurrent.NotThreadSafe;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.wicketstuff.annotation.mount.MountPath;

import de.invesdwin.common.client.wicket.examples.AExampleWebPage;
import de.invesdwin.nowicket.generated.binding.GeneratedBinding;

@NotThreadSafe
@MountPath("springsamlsecure")
//@AuthorizeInstantiation(IExampleRoles.ADMIN) instead of auth-roles we use declarative security in the spring xml
public class SpringSamlSecurePage extends AExampleWebPage {

    public SpringSamlSecurePage() {
        this(Model.of(new SpringSamlSecure()));
    }

    public SpringSamlSecurePage(final IModel<SpringSamlSecure> model) {
        super(model);
        new GeneratedBinding(this).bind();
    }

}
