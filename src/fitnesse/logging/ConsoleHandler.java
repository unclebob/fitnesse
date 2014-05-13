package fitnesse.logging;

import java.util.logging.ErrorManager;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * This <tt>Handler</tt> publishes log records on <tt>System.err</tt> or
 * <tt>Sytem.out</tt>
 * <p>
 * If level <tt>WARNING</tt> or <tt>SEVERE</tt>, log message is sent to
 * <tt>System.err</tt> otherwise to <tt>System.out</tt>.
 */

public class ConsoleHandler extends Handler {
    /**
     * Initialize the console Handler level and formatter
     */
    public ConsoleHandler() {
	setLevel(Level.INFO);
	setFormatter(new LogFormatter());
    }

    @Override
    public void publish(LogRecord record) {
	if (!isLoggable(record)) {
	    return;
	}

	String msg;
	try {
	    msg = getFormatter().format(record);
	} catch (Exception ex) {
	    // We don't want to throw an exception here, but we report the
	    // exception to any registered ErrorManager.
	    reportError(null, ex, ErrorManager.FORMAT_FAILURE);
	    return;
	}

	if (record.getLevel().intValue() >= Level.WARNING.intValue()) {
	    System.err.print(msg);
	} else {
	    System.out.print(msg);
	}
	// Use PrintStream#print not PrintStream#println because formatter
	// add the new line at the end of the message
    }

    /**
     * Flush but not to close the output streams.
     */
    @Override
    public void close() {
	flush();
    }

    @Override
    public void flush() {
	System.out.flush();
	System.err.flush();
    }
}
