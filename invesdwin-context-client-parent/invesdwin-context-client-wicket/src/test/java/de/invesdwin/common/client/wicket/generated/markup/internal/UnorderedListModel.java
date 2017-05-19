package de.invesdwin.common.client.wicket.generated.markup.internal;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import de.invesdwin.nowicket.generated.binding.annotation.ModalCloser;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@NotThreadSafe
@GeneratedMarkup
public class UnorderedListModel extends AValueObject {

    private final List<SampleModel> unorderedList = new ArrayList<SampleModel>();

    public UnorderedListModel() {
        for (int i = 0; i < 3; i++) {
            unorderedList.add(new SampleModel(i));
        }
    }

    public List<SampleModel> getUnorderedList() {
        return unorderedList;
    }

    @ModalCloser
    public void close() {}

}
