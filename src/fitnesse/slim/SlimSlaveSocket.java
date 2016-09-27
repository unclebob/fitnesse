// Copyright (C) 2016 by six42@gmx.net, All rights reserved.

package fitnesse.slim;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import util.FileUtil;
import fitnesse.util.MockSocket;

public class SlimSlaveSocket extends ServerSocket {
  private static final Logger LOG = Logger.getLogger(SlimSlaveSocket.class
      .getName());

  private PrintStream stdout;
  private PrintStream stderr;
  private InputStream stdin;

  public SlimSlaveSocket() throws IOException {

    // preserve original streams
    this.stdout = System.out;
    this.stderr = System.err;
    this.stdin = System.in;

    // bind System.stdout/System.stderr to original stderr
    System.setOut(new PrintStream(new LoggingOutputStream(this.stderr, "SOUT"),
        true));
    System.setErr(new PrintStream(new LoggingOutputStream(this.stderr, "SERR"),
        true));

    LOG.log(Level.FINER, "Creating port free Slim Slave.");

  }

  @Override
  public int getLocalPort() {
    return 1;
  }

  @Override
  public void close() {
    FileUtil.close(stdin);
    System.out.flush();
    System.err.flush();
    stdout.flush();
    FileUtil.close(stdout);
    FileUtil.close(stderr);
  }

  @Override
  public Socket accept() {
    return new MockSocket(this.stdin, this.stdout);
  }
}
