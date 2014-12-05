package fitnesse.logging;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Java logging formatter.
 */
public class LogFormatter extends Formatter {

  private static final String ENDL = System.getProperty("line.separator");

  public LogFormatter() {
    super();
  }

  @Override
  public String format(LogRecord logRecord) {
    StringBuilder builder = new StringBuilder(128);
    Throwable thrown = logRecord.getThrown();

    if (atLeastWarningLevel(logRecord)) {
      builder.append(logRecord.getLevel().getName())
              .append(": ");
    }
    builder.append(logRecord.getMessage());
    if (thrown != null) {
      builder.append(" [")
              .append(thrown.getMessage())
              .append("]");
    }
    builder.append(ENDL);

    if (thrown != null && atLeastWarningLevel(logRecord)) {
      StringWriter writer = new StringWriter();
      thrown.printStackTrace(new PrintWriter(writer));
      builder.append(writer.toString());
    }
    return builder.toString();
  }

  private boolean atLeastWarningLevel(LogRecord logRecord) {
    return logRecord.getLevel().intValue() > Level.INFO.intValue();
  }

}
