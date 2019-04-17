package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.Scanner;

import javax.annotation.concurrent.NotThreadSafe;

import org.fife.io.DocumentReader;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.expression.tokenizer.IPosition;
import de.invesdwin.util.math.expression.tokenizer.ParseException;

@NotThreadSafe
public class ExpressionValidatingParser extends AbstractParser {

    private final DefaultParseResult result;

    public ExpressionValidatingParser() {
        this.result = new DefaultParseResult(this);
    }

    @Override
    public ParseResult parse(final RSyntaxDocument doc, final String style) {
        result.clearNotices();

        // Always spell check all lines, for now.
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        result.setParsedLines(0, lineCount - 1);

        final StringBuilder expression = new StringBuilder();
        try (DocumentReader r = new DocumentReader(doc)) {
            try (Scanner scanner = new Scanner(r)) {
                while (scanner.hasNextLine()) {
                    expression.append(scanner.nextLine()).append("\n");
                }
            }
        }

        try {
            parseExpression(expression.toString());
        } catch (final ParseException e) {
            final IPosition position = e.getPosition();
            final int line = position.getLine();
            final int offset = position.getPos() - 1;
            final int length = Integers.max(1, position.getLength());
            result.addNotice(new DefaultParserNotice(this, e.getOriginalMessage(), line, offset, length));
        } catch (final Throwable t) {
            result.addNotice(new DefaultParserNotice(this, t.toString(), 1, 0, 1));
        }
        return result;
    }

    protected void parseExpression(final String expression) throws ParseException {
        new de.invesdwin.util.math.expression.ExpressionParser(expression).parse();
    }

}
