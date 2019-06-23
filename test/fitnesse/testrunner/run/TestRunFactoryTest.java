package fitnesse.testrunner.run;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class TestRunFactoryTest {
  private TestRunFactory factory = TestRunFactory.getInstance();

  @Before
  public void setUp() {
    factory.resetProviders();
  }

  @After
  public void tearDown() {
    factory.resetProviders();
  }

  @Test
  public void defaultIsPerTestSystemTestRun() {
    TestRun run = factory.createRun(Collections.emptyList());

    assertEquals(PerTestSystemTestRun.class, run.getClass());

    factory.addProvider(pages -> Optional.empty());
    run = factory.createRun(Collections.emptyList());
    assertEquals(PerTestSystemTestRun.class, run.getClass());
  }

  @Test
  public void canAddProvider() {
    TestRun expectedRun = mock(TestRun.class);
    factory.addProvider(pages -> Optional.empty());
    factory.addProvider(pages -> Optional.of(expectedRun));
    factory.addProvider(pages -> Optional.empty());

    TestRun run = factory.createRun(Collections.emptyList());

    assertSame(expectedRun, run);
  }

  @Test
  public void providerAddedLastWins() {
    TestRun expectedRun = mock(TestRun.class);
    TestRun notExpectedRun = mock(TestRun.class);
    factory.addProvider(pages -> Optional.empty());
    factory.addProvider(pages -> Optional.of(notExpectedRun));
    factory.addProvider(pages -> Optional.of(expectedRun));
    factory.addProvider(pages -> Optional.empty());

    TestRun run = factory.createRun(Collections.emptyList());

    assertSame(expectedRun, run);
  }
}
