package de.invesdwin.context.client.wicket.examples.guestbook.persistence;

import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import jakarta.inject.Named;

import com.querydsl.jpa.impl.JPAQuery;

import de.invesdwin.context.persistence.jpa.api.dao.ADao;

@Named
@ThreadSafe
public class GuestbookEntryDao extends ADao<GuestbookEntryEntity> {
    public List<GuestbookEntryEntity> getEntriesOrderedByDate() {
        final QGuestbookEntryEntity qg = QGuestbookEntryEntity.guestbookEntryEntity;
        return new JPAQuery<GuestbookEntryEntity>(getEntityManager()).from(qg).orderBy(qg.created.desc()).fetch();
    }
}
