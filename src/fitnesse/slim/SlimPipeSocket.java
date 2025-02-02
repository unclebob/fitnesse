// Copyright (C) 2016 by six42@gmx.net, All rights reserved.

package fitnesse.slim;

import fitnesse.util.MockSocket;
import util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SlimPipeSocket extends ServerSocket {
  public static final String STDOUT_PREFIX = "SOUT";
  public static final String STDERR_PREFIX = "SERR";
  public static final String FIRST_LINE_PREFIX = ".:";
  public static final String FOLLOWING_LINE_PREFIX = " :";

  static {// Better to check this at compile time, but don't know how
    if (FIRST_LINE_PREFIX.length() != FOLLOWING_LINE_PREFIX.length()) {
      System.err
          .println("FIRST_LINE_PREFIX and FOLLOWING_LINE_PREFIX must have the same length!!");
      System.exit(-99);
    }
  }

  private static final Logger LOG = Logger.getLogger(SlimPipeSocket.class
      .getName());

  private PrintStream stdout;
  private PrintStream stderr;
  private InputStream stdin;

  public SlimPipeSocket() throws IOException {

    // preserve original streams
    this.stdout = System.out;
    this.stderr = System.err;
    this.stdin = System.in;

    // bind System.stdout/System.stderr to original stderr
    System.setOut(wrapStream(this.stderr, STDOUT_PREFIX));
    System.setErr(wrapStream(this.stderr, STDERR_PREFIX));

    LOG.log(Level.FINER, "Creating Slim Server with pipe socket.");
  }

  static PrintStream wrapStream(PrintStream original, String prefix) throws UnsupportedEncodingException {
    return new PrintStream(new LoggingOutputStream(original, prefix), true, FileUtil.CHARENCODING);
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
