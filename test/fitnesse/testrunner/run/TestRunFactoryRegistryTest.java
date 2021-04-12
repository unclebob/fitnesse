package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class TestRunFactoryRegistryTest {
  private TestRunFactoryRegistry registry = new TestRunFactoryRegistry(null);

  @Test
  public void defaultIsPerTestSystemTestRun() {
    TestRun run = registry.createRun(Collections.emptyList());

    assertEquals(PerTestSystemTestRun.class, run.getClass());

    addFactory(pages -> Optional.empty());
    run = registry.createRun(Collections.emptyList());
    assertEquals(PerTestSystemTestRun.class, run.getClass());
    assertEquals(0, run.getPages().size());
  }

  @Test
  public void canAddFactory() {
    TestRun expectedRun = mock(TestRun.class);
    addFactory(pages -> Optional.empty());
    addFactory(pages -> Optional.of(expectedRun));
    addFactory(pages -> Optional.empty());

    TestRun run = registry.createRun(Collections.emptyList());

    assertSame(expectedRun, run);
  }

  @Test
  public void factoryAddedLastWins() {
    TestRun expectedRun = mock(TestRun.class);
    TestRun notExpectedRun = mock(TestRun.class);
    addFactory(pages -> Optional.empty());
    addFactory(pages -> Optional.of(notExpectedRun));
    addFactory(pages -> Optional.of(expectedRun));
    addFactory(pages -> Optional.empty());

    TestRun run = registry.createRun(Collections.emptyList());

    assertSame(expectedRun, run);
  }

  private void addFactory(Function<List<WikiPage>, Optional<TestRun>> provider) {
    TestRunFactory factory = new TestRunFactory() {
      @Override
      public boolean canRun(List<WikiPage> pages) {
        return provider.apply(pages).isPresent();
      }

      @Override
      public TestRun createRun(List<WikiPage> pages) {
        return provider.apply(pages).get();
      }

      @Override
      public PagePositions findPagePositions(List<WikiPage> pages) {
        return null;
      }
    };
    registry.addFactory(factory);
  }
}
