package fitnesse.slim.fixtureInteraction;

import fitnesse.slim.MethodExecutionResult;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Micro benchmark to see how interactions compare.
 * A benchmark like this proves almost nothing about real usage, but gives an indication
 * about relative speed.
 */
public class InteractionBenchmark {

  @Test
  public void timeSimple1_10() throws Throwable {
    callOften(new SimpleInteraction(), 1, 10);
  }

  @Test
  public void timeCached1_10() throws Throwable {
    callOften(new CachedInteraction(), 1, 10);
  }

  @Test
  public void timeSimple1() throws Throwable {
    callOften(new SimpleInteraction(), 1, 100);
  }

  @Test
  public void timeCached1() throws Throwable {
    callOften(new CachedInteraction(), 1, 100);
  }

  @Test
  public void timeSimple100() throws Throwable {
    callOften(new SimpleInteraction(), 100, 100);
  }

  @Test
  public void timeCached100() throws Throwable {
    callOften(new CachedInteraction(), 100, 100);
  }

  @Test
  public void timeSimple1000() throws Throwable {
    callOften(new SimpleInteraction(), 1_000, 100);
  }

  @Test
  public void timeCached1000() throws Throwable {
    callOften(new CachedInteraction(), 1_000, 100);
  }

  @Test
  public void timeSimple10000() throws Throwable {
    callOften(new SimpleInteraction(), 10_000, 100);
  }

  @Test
  public void timeCached10000() throws Throwable {
    callOften(new CachedInteraction(), 10_000, 100);
  }

  protected void callOften(FixtureInteraction interaction, int loops, int calls) throws Throwable {
    for (int i = 0; i < loops; i++) {
      Object fixture = interaction.createInstance(Collections.singletonList("fitnesse.fixtures"), "EchoFixture", new Object[0]);
      for (int j = 0; j < calls; j++) {
        MethodExecutionResult result = interaction.findAndInvoke("echo", fixture,"Hello");
        assertEquals("Hello", result.returnValue());
      }
    }
  }
}
