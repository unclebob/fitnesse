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
}
