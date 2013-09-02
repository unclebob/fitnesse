package fitnesse.testrunner;

import fitnesse.reporting.CompositeExecutionLog;

public interface TestsRunnerListener {

  void setTrackingId(String stopResponderId);

  void announceNumberTestsToRun(int testsToRun);
}
