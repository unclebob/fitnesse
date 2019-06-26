package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.slim.TestingInterruptedException;

import java.util.List;

public interface RunCoordinator {
  boolean isNotStopped();

  void announceTotalTestsToRun(int toRun);

  TestSystem startTestSystem(WikiPageIdentity identity, List<TestPage> testPages);

  int announceTestStarted();

  void waitForNoTestsInProgress() throws TestingInterruptedException;

  void reportException(Exception e);
}
