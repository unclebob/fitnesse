package fitnesse.testsystems.slim;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

import fitnesse.socketservice.ClientSocketFactory;
import fitnesse.testsystems.CommandRunner;

public class PipeBasedSocketFactory implements ClientSocketFactory {

  private final CommandRunner commandRunner;

  public PipeBasedSocketFactory(CommandRunner commandRunner) {
    this.commandRunner = commandRunner;
  }

  @Override
  public Socket createSocket(final String hostName, final int port) throws IOException {
    return new PipeBasedSocket();
  }

  /**
   * A socket based on a pipe (stdin/stdout).
   */
  private class PipeBasedSocket extends Socket {
    @Override
    public InputStream getInputStream() throws IOException {
      return commandRunner.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
      return commandRunner.getOutputStream();
    }

    @Override
    public synchronized void setSoTimeout(final int timeout) throws SocketException {
    }

    @Override
    public synchronized int getSoTimeout() throws SocketException {
      return 0;
    }
  }
}
