package fitnesse.testsystems.fit;

import java.io.IOException;

import fitnesse.socketservice.SocketServer;
import fitnesse.util.MockSocket;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Socket catcher is used to
 */
public class SocketCatcherTest {

  @Test
  public void shouldPassIncomingConnectionToSocketAccepter() throws IOException, InterruptedException {
    SocketAccepter accepter = mock(SocketAccepter.class);
    SocketServer server = new SocketCatcher(accepter, 1);
    MockSocket socket = new MockSocket("GET /?responder=socketCatcher&ticket=1 HTTP/1.1\r\n\r\n");

    server.serve(socket);

    verify(accepter).acceptSocket(socket);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAcceptAnythingButSocketCatcherResponder() throws IOException, InterruptedException {
    SocketAccepter accepter = mock(SocketAccepter.class);
    SocketServer server = new SocketCatcher(accepter, 1);
    MockSocket socket = new MockSocket("GET /?responder=fake&ticket=1 HTTP/1.1\r\n\r\n");

    server.serve(socket);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAcceptAnythingButValidTicketNumber() throws IOException, InterruptedException {
    SocketAccepter accepter = mock(SocketAccepter.class);
    SocketServer server = new SocketCatcher(accepter, 1234);
    MockSocket socket = new MockSocket("GET /?responder=fake&ticket=1234 HTTP/1.1\r\n\r\n");

    server.serve(socket);
  }

}
