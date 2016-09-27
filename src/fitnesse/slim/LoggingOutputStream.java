package fitnesse.slim;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * An OutputStream that writes contents to a Logger upon each call to flush()
 */
class LoggingOutputStream extends ByteArrayOutputStream {

  private String lineSeparator;

  private PrintStream logger;
  private String level;

  /**
   * Constructor
   * 
   * @param stdout
   * @param logger
   *          Logger to write to
   * @param level
   *          Level at which to write the log message
   */
  public LoggingOutputStream(PrintStream logger, String level) {
    super();
    this.logger = logger;
    this.level = level;
    this.lineSeparator = System.getProperty("line.separator");
  }

  /**
   * upon flush() write the existing contents of the OutputStream to the logger
   * as a log record.
   * 
   * @throws java.io.IOException
   *           in case of error
   */
  @Override
  public void flush() throws IOException {
    String record;
    synchronized (this) {
      super.flush();
      record = this.toString();
      super.reset();
    }
    if (record.length() == 0 || record.equals(lineSeparator)) {
      // avoid empty records
      return;
    }
    // Prefix each line
    for (String item : record.split(this.lineSeparator)) {
      logger.println(level + ":" + item);
    }
  }

}
