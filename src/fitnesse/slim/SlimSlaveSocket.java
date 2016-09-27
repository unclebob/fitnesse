package fitnesse.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.FileUtil;
import fitnesse.socketservice.PlainServerSocketFactory;
import fitnesse.util.MockSocket;

public class SlimSlaveSocket extends ServerSocket {
  private static final Logger LOG = Logger.getLogger(SlimSlaveSocket.class
      .getName());

  private PrintStream stdout;
  private PrintStream stderr;
  private InputStream stdin;

  public SlimSlaveSocket() throws IOException {

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

    LOG.log(Level.FINER, "Creating port free Slim Slave.");

  }

  @Override
  public int getLocalPort() {
    return 1;
  }

  @Override
  public void close() {
    FileUtil.close(stdin);
    FileUtil.close(stdout);
    FileUtil.close(stderr);
  }

  @Override
  public Socket accept() {
    return new MockSocket(this.stdin, this.stdout);
  }
}
