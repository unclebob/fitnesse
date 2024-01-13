package fitnesse.testrunner.run;

import fitnesse.testrunner.Stoppable;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.wiki.WikiPage;

import java.util.List;

public interface TestRun extends Stoppable {
  void executeTestPages(RunCoordinator coordinator) throws TestExecutionException;

  List<WikiPage> getPages();
}
