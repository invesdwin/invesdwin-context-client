package de.invesdwin.common.client.wicket.generated.markup.internal.bugfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.concurrent.NotThreadSafe;

import org.hibernate.validator.constraints.NotEmpty;

import de.invesdwin.common.client.wicket.generated.markup.internal.SampleModel;
import de.invesdwin.common.client.wicket.generated.markup.internal.TabbedContainerSampleModel;
import de.invesdwin.norva.beanpath.annotation.Tabbed;
import de.invesdwin.nowicket.generated.markup.annotation.GeneratedMarkup;
import de.invesdwin.util.bean.AValueObject;

@GeneratedMarkup
@NotThreadSafe
public class Bugfix extends AValueObject {

    private BugfixComplexValueObject complexItereableAsSelect;
    private String noChoiceSelect = "bla";
    private final TabbedContainerSampleModel tabs;
    private final List<SampleModel> accordionFromList = new ArrayList<SampleModel>();
    private List<String> multiSelect;
    private List<String> multiSelectWithPreselection = Arrays.asList("3", "2");
    @NotEmpty
    private List<String> multiSelectPalette;
    private final List<BugfixComplexValueObject> table = new ArrayList<BugfixComplexValueObject>();

    public Bugfix() {
        tabs = new TabbedContainerSampleModel();
        for (int i = 0; i < 3; i++) {
            accordionFromList.add(new SampleModel(i));
            table.add(new BugfixComplexValueObject("" + i));
        }
    }

    public Iterable<BugfixComplexValueObject> getComplexIterableAsSelectChoice() {
        final List<BugfixComplexValueObject> asList = Arrays.asList(new BugfixComplexValueObject(null),
                //duplicate "null" causes duplicate choice warning
                new BugfixComplexValueObject("null"), new BugfixComplexValueObject("null"),
                new BugfixComplexValueObject("asdf"));
        return new Iterable<BugfixComplexValueObject>() {

            @Override
            public Iterator<BugfixComplexValueObject> iterator() {
                return asList.iterator();
            }

        };
    }

    public BugfixComplexValueObject getComplexIterableAsSelect() {
        return complexItereableAsSelect;
    }

    public void setComplexIterableAsSelect(final BugfixComplexValueObject complexIterableAsSelect) {
        this.complexItereableAsSelect = complexIterableAsSelect;
    }

    public String getNoChoiceSelect() {
        return noChoiceSelect;
    }

    public void setNoChoiceSelect(final String noChoiceSelect) {
        this.noChoiceSelect = noChoiceSelect;
    }

    @Tabbed
    public TabbedContainerSampleModel getTabs() {
        return tabs;
    }

    @Tabbed
    public TabbedContainerSampleModel getTabsAllOpenAllowed() {
        return tabs;
    }

    public List<String> getMultiSelect() {
        return multiSelect;
    }

    public void setMultiSelect(final List<String> multiSelect) {
        this.multiSelect = multiSelect;
    }

    public Iterable<String> getMultiSelectChoice() {
        return Arrays.asList("1", "2", "3");
    }

    public List<String> getMultiSelectWithPreselection() {
        return multiSelectWithPreselection;
    }

    public void setMultiSelectWithPreselection(final List<String> multiSelectWithPreselection) {
        this.multiSelectWithPreselection = multiSelectWithPreselection;
    }

    public List<String> getMultiSelectWithPreselectionChoice() {
        return Arrays.asList("1", "2", "3");
    }

    public List<SampleModel> getAccordionFromList() {
        return accordionFromList;
    }

    public List<String> getMultiSelectPalette() {
        return multiSelectPalette;
    }

    //    @Eager
    public void setMultiSelectPalette(final List<String> multiSelect) {
        this.multiSelectPalette = multiSelect;
    }

    public Iterable<String> getMultiSelectPaletteChoice() {
        return Arrays.asList("1", "2", "3");
    }

    public String validateMultiSelectPalette(final List<String> newValue) {
        if (newValue != null && !newValue.contains("3")) {
            return "should contain \"3\" as this is mandatory here";
        }
        return null;
    }

    public void submit() {}

    public List<BugfixComplexValueObject> getTable() {
        return table;
    }

    public void removeFromTable(final BugfixComplexValueObject value) {
        table.remove(value);
    }

}
