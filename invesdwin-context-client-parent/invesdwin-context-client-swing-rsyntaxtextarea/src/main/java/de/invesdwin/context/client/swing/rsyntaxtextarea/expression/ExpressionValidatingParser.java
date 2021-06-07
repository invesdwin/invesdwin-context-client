package de.invesdwin.context.client.swing.rsyntaxtextarea.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.fife.io.DocumentReader;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.parser.AbstractParser;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParseResult;
import org.fife.ui.rsyntaxtextarea.parser.DefaultParserNotice;
import org.fife.ui.rsyntaxtextarea.parser.ParseResult;

import de.invesdwin.aspects.EventDispatchThreadUtil;
import de.invesdwin.util.concurrent.future.Futures;
import de.invesdwin.util.error.Throwables;
import de.invesdwin.util.lang.Objects;
import de.invesdwin.util.lang.Strings;
import de.invesdwin.util.math.Integers;
import de.invesdwin.util.math.expression.MultipleExpressionParser;
import de.invesdwin.util.math.expression.tokenizer.IPosition;
import de.invesdwin.util.math.expression.tokenizer.ParseException;

@ThreadSafe
public class ExpressionValidatingParser extends AbstractParser {

    private final RSyntaxTextArea parent;
    private final ExecutorService executor;
    @GuardedBy("this")
    private Future<List<DefaultParserNotice>> validateExpressionFutureResult;
    @GuardedBy("this")
    private String validateExpression;
    private Future<?> validateExpressionFutureTask;

    public ExpressionValidatingParser(final RSyntaxTextArea parent, final ExecutorService executor) {
        this.parent = parent;
        this.executor = executor;
    }

    //          * at app//de.invesdwin.util.math.expression.ExpressionParser.parse(ExpressionParser.java:188) *
    //          * at app//de.invesdwin.trading.charts.richclient.swing.chart.expression.AFactoryExpressionSeriesProvider.parseExpression(AFactoryExpressionSeriesProvider.java:43) *
    //          * at app//de.invesdwin.context.client.swing.jfreechart.panel.helper.config.series.expression.IExpressionSeriesProvider$1.parseExpression(IExpressionSeriesProvider.java:34) *
    //          * at app//de.invesdwin.context.client.swing.rsyntaxtextarea.expression.ExpressionValidatingParser.parse(ExpressionValidatingParser.java:55) *
    //            at app//org.fife.ui.rsyntaxtextarea.ParserManager.actionPerformed(ParserManager.java:163)
    //            at java.desktop@13.0.3/javax.swing.Timer.fireActionPerformed(Timer.java:317)
    //            at java.desktop@13.0.3/javax.swing.Timer$DoPostEvent.run(Timer.java:249)
    //            at java.desktop@13.0.3/java.awt.event.InvocationEvent.dispatch(InvocationEvent.java:313)
    //            at java.desktop@13.0.3/java.awt.EventQueue.dispatchEventImpl(EventQueue.java:770)
    @Override
    public ParseResult parse(final RSyntaxDocument doc, final String style) {
        final DefaultParseResult result = new DefaultParseResult(this);

        // Always spell check all lines, for now.
        final int lineCount = doc.getDefaultRootElement().getElementCount();
        result.setParsedLines(0, lineCount - 1);
        boolean firstLine = true;

        final StringBuilder expression = new StringBuilder();
        try (DocumentReader r = new DocumentReader(doc)) {
            try (Scanner scanner = new Scanner(r)) {
                while (scanner.hasNextLine()) {
                    if (!firstLine) {
                        expression.append("\n");
                    }
                    expression.append(scanner.nextLine());
                    firstLine = false;
                }
            }
        }
        if (Strings.isBlank(expression)) {
            return result;
        }

        validateExpressionMaybeAsync(result, expression);

        return result;
    }

    private void validateExpressionMaybeAsync(final DefaultParseResult result, final StringBuilder expression) {
        if (executor != null) {
            final Future<List<DefaultParserNotice>> future = validateExpression(expression.toString());
            if (future.isDone()) {
                try {
                    final List<DefaultParserNotice> notices = Futures.get(future);
                    for (int i = 0; i < notices.size(); i++) {
                        result.addNotice(notices.get(i));
                    }
                } catch (final InterruptedException e) {
                    //ignore
                }
            }
        } else {
            final List<DefaultParserNotice> notices = validateExpressionAsync(expression.toString());
            for (int i = 0; i < notices.size(); i++) {
                result.addNotice(notices.get(i));
            }
        }
    }

    private void processParseException(final List<DefaultParserNotice> notices, final ParseException e) {
        final IPosition position = e.getPosition();
        final int line = position.getLineOffset();
        final int offset = position.getIndexOffset();
        final int length = Integers.max(1, position.getLength());
        notices.add(new DefaultParserNotice(this, e.getOriginalMessage(), line, offset, length));
    }

    protected void parseExpression(final String expression) throws ParseException {
        new MultipleExpressionParser(expression).parse();
    }

    private synchronized Future<List<DefaultParserNotice>> validateExpression(final String expression) {
        if (validateExpressionFutureResult != null) {
            if (!Objects.equals(validateExpression, expression)) {
                //interrupt should not cause any data corruption when initializing instruments
                validateExpressionFutureTask.cancel(true);
                validateExpressionFutureTask = null;
                validateExpressionFutureResult.cancel(true);
                validateExpressionFutureResult = null;
                validateExpression = null;
            } else {
                return validateExpressionFutureResult;
            }
        }

        final CompletableFuture<List<DefaultParserNotice>> futureResult = new CompletableFuture<>();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    final List<DefaultParserNotice> notices = validateExpressionAsync(expression);
                    futureResult.complete(notices);
                } catch (final Throwable t) {
                    futureResult.completeExceptionally(t);
                }
            }
        };
        futureResult.thenAccept(new Consumer<List<DefaultParserNotice>>() {
            @Override
            public void accept(final List<DefaultParserNotice> t) {
                EventDispatchThreadUtil.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        parent.forceReparsing(ExpressionValidatingParser.this);
                    }
                });
            }
        });
        final Future<?> futureTask = executor.submit(task);
        validateExpressionFutureTask = futureTask;
        validateExpressionFutureResult = futureResult;
        validateExpression = expression;
        return futureResult;
    }

    protected List<DefaultParserNotice> validateExpressionAsync(final String expression) {
        final List<DefaultParserNotice> notices = new ArrayList<DefaultParserNotice>();
        try {
            parseExpression(expression.toString());
        } catch (final ParseException e) {
            processParseException(notices, e);
        } catch (final Throwable t) {
            final ParseException parseException = Throwables.getCauseByType(t, ParseException.class);
            if (parseException != null) {
                processParseException(notices, parseException);
            } else {
                notices.add(
                        new DefaultParserNotice(this, Throwables.concatMessagesShort(t), 1, 0, expression.length()));
            }
        }
        return notices;
    }

}
