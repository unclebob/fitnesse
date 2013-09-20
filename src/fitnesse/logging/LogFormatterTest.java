package fitnesse.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LogFormatterTest {

  @Test
  public void logLine() {
    LogRecord logRecord = new LogRecord(Level.INFO, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("MyLogger\tmessage" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }

  @Test
  public void logWarningLine() {
    LogRecord logRecord = new LogRecord(Level.WARNING, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("MyLogger\tWARNING: message" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }

  @Test
  public void logSevereLine() {
    LogRecord logRecord = new LogRecord(Level.SEVERE, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("MyLogger\tSEVERE: message" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }


}
