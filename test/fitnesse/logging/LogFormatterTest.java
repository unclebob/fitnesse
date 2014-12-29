package fitnesse.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Test;

import static org.junit.Assert.*;

public class LogFormatterTest {

  @Test
  public void logLine() {
    LogRecord logRecord = new LogRecord(Level.INFO, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("message" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }

  @Test
  public void logWarningLine() {
    LogRecord logRecord = new LogRecord(Level.WARNING, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("WARNING: message" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }

  @Test
  public void logSevereLine() {
    LogRecord logRecord = new LogRecord(Level.SEVERE, "message");
    logRecord.setLoggerName("MyLogger");

    assertEquals("SEVERE: message" + System.getProperty("line.separator"), new LogFormatter().format(logRecord));
  }

  @Test
  public void logShouldLogExceptions() {
    LogRecord logRecord = new LogRecord(Level.WARNING, "message");
    logRecord.setLoggerName("MyLogger");
    logRecord.setThrown(new RuntimeException(new IllegalArgumentException("Something went wrong here")));

    String logOutput = new LogFormatter().format(logRecord);
    assertTrue(logOutput, logOutput.contains("WARNING: message [java.lang.IllegalArgumentException: Something went wrong here]" + System.getProperty("line.separator")));
    assertTrue(logOutput, logOutput.contains("at fitnesse.logging.LogFormatterTest.logShouldLogExceptions"));

  }

  @Test
  public void logShouldNotLogExceptionsAtInfoLevel() {
    LogRecord logRecord = new LogRecord(Level.WARNING, "message");
    logRecord.setLoggerName("MyLogger");
    logRecord.setThrown(new RuntimeException(new IllegalArgumentException("Something went wrong here")));

    String logOutput = new LogFormatter().format(logRecord);
    assertTrue(logOutput, logOutput.contains("WARNING: message [java.lang.IllegalArgumentException: Something went wrong here]" + System.getProperty("line.separator")));
    assertFalse(logOutput, logOutput.contains("at fitnesse.logging.LogFormatterTest.logShouldLogExceptions"));

  }

}
