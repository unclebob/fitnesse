package fitnesse.slim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class SlimSlaveSocket implements SlimSocket {

  private PrintStream stdout;
  private PrintStream stderr;
  private InputStream stdin;

  public SlimSlaveSocket() {

    // preserve old stdout/stderr streams in case they might be useful
    this.stdout = System.out;
    this.stderr = System.err;
    this.stdin = System.in;

    // now rebind stdout/stderr to original stderr
    LoggingOutputStream los;

    los = new LoggingOutputStream(this.stderr, "SOUT");
    System.setOut(new PrintStream(los, true));

    los = new LoggingOutputStream(this.stderr, "SERR");
    System.setErr(new PrintStream(los, true));

  }

  @Override
  public int getLocalPort() {

    return -1;
  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  @Override
  public SlimSocket accept() {
    return this;
  }

  @Override
  public SlimStreamReader getReader() {
    return new SlimStreamReader(new BufferedInputStream(this.stdin));
  }

  @Override
  public OutputStream getByteWriter() {
    return new BufferedOutputStream(stdout);
  }

}
