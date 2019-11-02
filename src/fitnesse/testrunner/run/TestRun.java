package fitnesse.testrunner.run;

import fitnesse.testrunner.Stoppable;
import fitnesse.testsystems.TestExecutionException;

public interface TestRun extends Stoppable {
  void executeTestPages(RunCoordinator coordinator) throws TestExecutionException;
}
