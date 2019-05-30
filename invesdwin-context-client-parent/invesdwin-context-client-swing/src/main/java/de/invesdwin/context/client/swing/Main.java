package de.invesdwin.context.client.swing;

import javax.annotation.concurrent.Immutable;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import de.invesdwin.context.beans.init.AMain;
import de.invesdwin.context.client.swing.impl.app.DelegateRichApplication;

@Immutable
public class Main extends AMain {

    protected Main(final String[] args) {
        super(args, false);
    }

    @Override
    protected void startApplication(final CmdLineParser parser) throws CmdLineException {
        DelegateRichApplication.launch(args);
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