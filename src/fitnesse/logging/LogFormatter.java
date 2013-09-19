package fitnesse.logging;

import java.util.logging.Formatter;
import java.util.logging.Handler;
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
    if (logRecord.getLevel().intValue() > Level.INFO.intValue()) {
      return logRecord.getLevel().getName() + " " + logRecord.getLoggerName() + "\t" + logRecord.getMessage() + ENDL;
    }
    return logRecord.getLoggerName() + "\t" + logRecord.getMessage() + ENDL;
  }
}
