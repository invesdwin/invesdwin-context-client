package de.invesdwin.context.client.wicket.examples.guestbook.persistence;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.context.persistence.jpa.api.dao.entity.AEntity;
import de.invesdwin.norva.beanpath.annotation.Disabled;
import de.invesdwin.norva.beanpath.annotation.Format;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.time.date.FDate;
import jakarta.persistence.Entity;

@Entity
@NotThreadSafe
@GeneratedMarkup
public class GuestbookEntryEntity extends AEntity {

    private String message;
    private String email;

    @Disabled
    public String getMessage() {
        return message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    @Disabled
    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    @Disabled
    @Format(FDate.FORMAT_ISO_DATE_TIME)
    public FDate getDate() {
        return super.getCreated();
    }
}
