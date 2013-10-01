package fitnesse.testsystems.slim;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

import fitnesse.socketservice.SocketFactory;

import fitnesse.testsystems.Descriptor;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlimClientBuilderTest {

  @Before
  public void setUp() throws Exception {
    // Enforce the test runner here, to make sure we're talking to the right system
    SlimClientBuilder.clearSlimPortOffset();
  }

  @Test
  public void portRotates() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("SLIM_PORT")).thenReturn(null);
    for (int i = 1; i < 15; i++) {
      SlimClientBuilder clientBuilder = new SlimClientBuilder(descriptor);
      assertEquals(8085 + (i % 10), clientBuilder.getSlimPort());
    }
  }

  @Test
  public void portStartsAtSlimPortVariable() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("SLIM_PORT")).thenReturn("9000");
    for (int i = 1; i < 15; i++) {
      SlimClientBuilder clientBuilder = new SlimClientBuilder(descriptor);
      assertEquals(9000 + (i % 10), clientBuilder.getSlimPort());
    }
  }

  @Test
  public void badSlimPortVariableDefaults() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("SLIM_PORT")).thenReturn("BOB");
    for (int i = 1; i < 15; i++)
      assertEquals(8085 + (i % 10), new SlimClientBuilder(descriptor).getSlimPort());
  }

  @Test
  public void slimHostDefaultsTolocalhost() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    assertEquals("localhost", new SlimClientBuilder(descriptor).determineSlimHost());
  }

  @Test
  public void slimHostVariableSetsTheHost() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("SLIM_HOST")).thenReturn("somehost");
    assertEquals("somehost", new SlimClientBuilder(descriptor).determineSlimHost());
  }

  @Test(expected = IOException.class)
  public void createSlimServiceFailsFastWhenSlimPortIsNotAvailable() throws Exception {
    final int slimServerPort = 10258;
    Descriptor descriptor = mock(Descriptor.class);
    ServerSocket slimSocket = SocketFactory.tryCreateServerSocket(slimServerPort);
    try {
      SlimClientBuilder sys = new SlimClientBuilder(descriptor);
      String slimArguments = String.format("%s %d", "", slimServerPort);
      sys.createSlimService(slimArguments);
    } finally {
      slimSocket.close();
    }
  }


}
