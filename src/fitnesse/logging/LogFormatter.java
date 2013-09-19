package fitnesse.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
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
    return logRecord.getLoggerName() + "\t" + logRecord.getMessage() + ENDL;
  }
}
