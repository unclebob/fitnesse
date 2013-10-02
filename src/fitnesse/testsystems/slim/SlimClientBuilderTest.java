package fitnesse.testsystems.slim;

import java.net.ServerSocket;
import java.net.SocketException;

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
  public void portStartsAtSlimPortEnvironmentVariable() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("slim.port")).thenReturn("9000");
    when(descriptor.getVariable("SLIM_PORT")).thenReturn("1313");
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
  public void slimPortPoolSizeCanBeModified() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("slim.pool.size")).thenReturn("20");
    for (int i = 1; i < 25; i++)
      assertEquals(8085 + (i % 20), new SlimClientBuilder(descriptor).getSlimPort());
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

  @Test
  public void slimHostVariableSetsTheHostEnvironmentVariable() throws Exception {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("slim.host")).thenReturn("somehost");
    when(descriptor.getVariable("SLIM_HOST")).thenReturn("notThisHost");
    assertEquals("somehost", new SlimClientBuilder(descriptor).determineSlimHost());
  }

  @Test(expected = SocketException.class)
  public void createSlimServiceFailsFastWhenSlimPortIsNotAvailable() throws Exception {
    final int slimServerPort = 10258;
    Descriptor descriptor = mock(Descriptor.class);
    ServerSocket slimSocket = new ServerSocket(slimServerPort);
    try {
      SlimClientBuilder sys = new SlimClientBuilder(descriptor);
      String slimArguments = String.format("%s %d", "", slimServerPort);
      sys.createSlimService(slimArguments);
    } finally {
      slimSocket.close();
    }
  }


}
