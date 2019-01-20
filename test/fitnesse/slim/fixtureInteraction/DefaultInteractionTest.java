package fitnesse.slim.fixtureInteraction;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class DefaultInteractionTest {

  @Test
  public void checkDefaultIsCached() {
    DefaultInteraction d = new DefaultInteraction();
    assertTrue(CachedInteraction.class.isAssignableFrom(d.getClass()));
  }
}
