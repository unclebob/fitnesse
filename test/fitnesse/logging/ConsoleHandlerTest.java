package fitnesse.logging;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConsoleHandlerTest {

  private static final ConsoleHandler handler = new ConsoleHandler();
  private static final ByteArrayOutputStream baOut = new ByteArrayOutputStream();
  private static final ByteArrayOutputStream baErr = new ByteArrayOutputStream();

  @BeforeClass
  public static void setUpSuite() {
    // Override defaults stream out and err to validate logs sent
    handler.setOut(new PrintStream(baOut));
    handler.setErr(new PrintStream(baErr));
    // Mock the formatter
    handler.setFormatter(new Formatter() {
      @Override
      public String format(LogRecord record) {
        return record.getMessage();
      }
    });
  }

  @Before
  public void setUp() {
    baOut.reset();
    baErr.reset();
  }

  @Test
  public void logFineLine() {
    publishLogRecord(Level.FINE);

    assertEquals(
        "Message with level less than INFO must NOT be logged on stdout", 0,
        baOut.size());
    assertEquals(
        "Message with level less than INFO must NOT be logged on stderr", 0,
        baErr.size());
  }

  @Test
  public void logInfoLine() {
    publishLogRecord(Level.INFO);

    assertEquals("Message with level equals to INFO must be logged on stdout",
        MESSAGE, baOut.toString());
    assertEquals(
        "Message with level equals to INFO must NOT be logged on stderr", 0,
        baErr.size());
  }

  @Test
  public void logWarningLine() {
    publishLogRecord(Level.WARNING);

    assertEquals(
        "Message with level equals or greater than WARNING must NOT be logged on stdout",
        0,
        baOut.size());
    assertEquals(
        "Message with level equals or greater than WARNING must be logged on stderr",
        MESSAGE, baErr.toString());
  }

  @Test
  public void logSevereLine() {
    publishLogRecord(Level.SEVERE);

    assertEquals(
        "Message with level equals to SEVERE must NOT be logged on stdout", 0,
        baOut.size());
    assertEquals("Message with level equals to SEVER must be logged on stderr",
        MESSAGE, baErr.toString());
  }

  @Test
  // Test that close doesn't close the stream
  public void close() {
    publishLogRecord(Level.INFO);
    handler.close();
    publishLogRecord(Level.INFO);

    assertEquals("Close method call should not close the stream",
        MESSAGE.length() * 2,
        baOut.toString().length());
  }

  private static final String MESSAGE = "message";

  private void publishLogRecord(Level level) {
    LogRecord logRecord = new LogRecord(level, MESSAGE);
    logRecord.setLoggerName("MyLogger");
    handler.publish(logRecord);
  }
}
