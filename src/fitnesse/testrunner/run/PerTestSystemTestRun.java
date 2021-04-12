package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.WikiPage;

import java.util.List;

public class PerTestSystemTestRun implements TestRun {
  private final PagesByTestSystem pagesByTestSystem;
  private TestSystem testSystem;
  private RunCoordinator coordinator;

  public PerTestSystemTestRun(List<WikiPage> pages) {
    this(new PagesByTestSystem(pages));
  }

  public PerTestSystemTestRun(PagesByTestSystem pagesByTestSystem) {
    this.pagesByTestSystem = pagesByTestSystem;
  }

  @Override
  public void executeTestPages(RunCoordinator coordinator) throws TestExecutionException {
    this.coordinator = coordinator;
    coordinator.announceTotalTestsToRun(pagesByTestSystem.totalTestsToRun());

    for (WikiPageIdentity identity : pagesByTestSystem.identities()) {
      startTestSystemAndExecutePages(identity, pagesByTestSystem.testPagesForIdentity(identity));
    }
  }

  @Override
  public List<WikiPage> getPages() {
    return pagesByTestSystem.testsToRun();
  }

  private void startTestSystemAndExecutePages(WikiPageIdentity identity, List<TestPage> testSystemPages) throws TestExecutionException {
    testSystem = null;
    try {
      if (coordinator.isNotStopped()) {
        testSystem = coordinator.startTestSystem(identity, testSystemPages);
      }

      if (testSystem != null && testSystem.isSuccessfullyStarted()) {
        executeTestSystemPages(testSystemPages, testSystem);
        coordinator.waitForNoTestsInProgress();
      }
    } finally {
      if (coordinator.isNotStopped() && testSystem != null) {
        try {
          testSystem.bye();
        } catch (Exception e) {
          coordinator.reportException(e);
        }
      }
    }
  }

  private void executeTestSystemPages(List<TestPage> pagesInTestSystem, TestSystem testSystem) throws TestExecutionException {
    for (TestPage testPage : pagesInTestSystem) {
      coordinator.announceTestStarted();
      testSystem.runTests(testPage);
    }
  }

  @Override
  public void stop() {
    if (testSystem != null) {
      testSystem.kill();
    }
  }
}
