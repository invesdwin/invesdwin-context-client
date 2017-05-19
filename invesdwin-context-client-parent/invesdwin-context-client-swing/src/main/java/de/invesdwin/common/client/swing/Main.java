package de.invesdwin.common.client.swing;

import javax.annotation.concurrent.Immutable;

import org.jdesktop.application.Application;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import de.invesdwin.common.client.swing.internal.app.DelegateRichApplication;
import de.invesdwin.context.beans.init.AMain;

@Immutable
public class Main extends AMain {

    @Argument(metaVar = "<arg1> <arg2>")
    private String[] arguments;

    protected Main(final String[] args) {
        super(args, false);
    }

    @Override
    protected void startApplication(final CmdLineParser parser) throws CmdLineException {
        final String[] args;
        if (arguments == null) {
            args = new String[0];
        } else {
            args = arguments;
        }
        Application.launch(DelegateRichApplication.class, args);
    }

    @Override
    protected CmdLineParser newCmdLineParser() {
        return new CmdLineParser(this) {
            @Override
            protected boolean isOption(final String arg) {
                //don't parse any options, everything is an argument and is passed along that way
                return false;
            }
        };
    }

    public static void main(final String[] args) {
        new Main(args);
    }

}