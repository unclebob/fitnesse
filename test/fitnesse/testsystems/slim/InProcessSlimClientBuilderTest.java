package fitnesse.testsystems.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;
import fitnesse.slim.fixtureInteraction.InteractionDemo;
import fitnesse.testsystems.Descriptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

public class InProcessSlimClientBuilderTest {
  private ClassLoader classLoader;

  @Before
  public void setUp() throws Exception {
    // Enforce the test runner here, to make sure we're talking to the right system
    SlimClientBuilder.clearSlimPortOffset();
    classLoader = ClassLoader.getSystemClassLoader();
  }

  @Test
  public void parseInteractionFlags() {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("slim.flags")).thenReturn("-i " + InteractionDemo.class.getName());

    InProcessSlimClientBuilder slimClientBuilder = new InProcessSlimClientBuilder(descriptor, classLoader);

    // Check that the arguments were processed correctly and are in the client
    // builder's slim flags
    String[] found = slimClientBuilder.getSlimFlags();
    assertNotNull(found);
    assertEquals(2, found.length);
    assertEquals("-i", found[0]);
    assertEquals(InteractionDemo.class.getName(), found[1]);
  }

  @Test
  public void createSlimClientWithFixtureInteraction() {
    Descriptor descriptor = mock(Descriptor.class);
    when(descriptor.getVariable("slim.flags")).thenReturn("-i " + InteractionDemo.class.getName());

    FixtureInteraction interaction = captureInteraction(descriptor);

    assertNotNull(interaction);
    assertNotEquals(DefaultInteraction.class, interaction.getClass());
    assertTrue(interaction instanceof InteractionDemo);
  }

  @Test
  public void createSlimClientWithoutFixtureInteraction() throws Exception {
    Descriptor descriptor = spy(Descriptor.class);
    when(descriptor.getVariable("slim.flags")).thenReturn(null);

    FixtureInteraction interaction = captureInteraction(descriptor);

    assertNotNull(interaction);
    assertEquals(DefaultInteraction.class, interaction.getClass());
  }

  private FixtureInteraction captureInteraction(Descriptor descriptor) {
    InProcessSlimClientBuilder slimClientBuilder = spy(new InProcessSlimClientBuilder(descriptor, classLoader));

    ArgumentCaptor<FixtureInteraction> interactionCaptor = ArgumentCaptor.forClass(FixtureInteraction.class);

    slimClientBuilder.build();
    verify(slimClientBuilder).createSlimServer(interactionCaptor.capture(), isNull(), anyBoolean());
    return interactionCaptor.getValue();
  }
}
