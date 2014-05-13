package fitnesse.logging;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConsoleHandlerTest {

  private static PrintStream outBackup, errBackup;

  private static ByteArrayOutputStream baOut = new ByteArrayOutputStream();
  private static ByteArrayOutputStream baErr = new ByteArrayOutputStream();
  private static PrintStream out = new PrintStream(baOut);
  private static PrintStream err = new PrintStream(baErr);

  private ConsoleHandler handler = new ConsoleHandler();

  @BeforeClass
  public static void setUpSuite() {
    outBackup = System.out;
    errBackup = System.err;
    System.setOut(out);
    System.setErr(err);
  }

  @AfterClass
  public static void tearDownSuite() {
    System.setOut(outBackup);
    System.setErr(errBackup);
  }

  @Before
  public void setUp() {
    baOut.reset();
    baErr.reset();
  }

  @Test
  public void logFineLine() {
    publishLogRecord(Level.FINE);

    assertEquals(0, baOut.size());
    assertEquals(0, baErr.size());
  }

  @Test
  public void logInfoLine() {
    publishLogRecord(Level.INFO);

    assertTrue(baOut.toString().contains(MESSAGE));
    assertEquals(0, baErr.size());
  }

  @Test
  public void logWarningLine() {
    publishLogRecord(Level.WARNING);

    assertEquals(0, baOut.size());
    assertTrue(baErr.toString().contains(MESSAGE));
  }

  @Test
  public void logSevereLine() {
    publishLogRecord(Level.SEVERE);

    assertEquals(0, baOut.size());
    assertTrue(baErr.toString().contains(MESSAGE));
  }

  @Test
  // Test that close doesn't close the stream
  public void close() {
    publishLogRecord(Level.INFO);
    handler.close();
    publishLogRecord(Level.INFO);
  }

  private static final String MESSAGE = "message";

  private void publishLogRecord(Level level) {
    LogRecord logRecord = new LogRecord(level, MESSAGE);
    logRecord.setLoggerName("MyLogger");
    handler.publish(logRecord);
  }

}
