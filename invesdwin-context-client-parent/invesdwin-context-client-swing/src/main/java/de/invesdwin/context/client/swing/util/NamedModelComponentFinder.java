package de.invesdwin.context.client.swing.util;

import java.awt.Component;

import javax.annotation.concurrent.Immutable;

import org.apache.commons.lang3.CharUtils;

import de.invesdwin.util.lang.string.Strings;
import de.invesdwin.util.swing.AComponentFinder;

@Immutable
public class NamedModelComponentFinder extends AComponentFinder {

    public static final NamedModelComponentFinder INSTANCE = new NamedModelComponentFinder();

    @Override
    public boolean matches(final Component component) {
        //our bean paths start with lowercase letters, swing might use uppercase names which we want to ignore
        return Strings.isNotBlank(component.getName()) && !CharUtils.isAsciiAlphaUpper(component.getName().charAt(0));
    }

}