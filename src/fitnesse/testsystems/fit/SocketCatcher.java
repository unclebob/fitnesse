package fitnesse.testsystems.fit;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.socketservice.SocketServer;

public class SocketCatcher implements SocketServer {
  private static final Logger LOG = Logger.getLogger(SocketCatcher.class.getName());

  private final SocketAccepter accepter;
  private final int ticketNumber;

  public SocketCatcher(SocketAccepter accepter, int ticketNumber) {
    this.accepter = accepter;
    this.ticketNumber = ticketNumber;
  }

  @Override
  public void serve(Socket s) throws IOException {
    InputStream input = s.getInputStream();

    Request request = new Request(input);
    try {
      request.parse();
    } catch (HttpException e) {
      LOG.log(Level.INFO, e.getMessage());
      return;
    }

    if (!"socketCatcher".equals(request.getInput("responder"))) {
      throw new IllegalArgumentException("Not a valid responder: " + request.getInput("responder"));
    }

    if (!Integer.toString(ticketNumber).equals(request.getInput("ticket"))) {
      throw new IllegalArgumentException("Not a valid ticket: " + request.getInput("ticket"));
    }

    try {
      accepter.acceptSocket(s);
    } catch (InterruptedException e) {
      LOG.log(Level.SEVERE, "Fit client interrupted");
      Thread.currentThread().interrupt();
    }
  }
}
