package fitnesse.testrunner;

public interface TestsRunnerListener {

  void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log);

  void announceNumberTestsToRun(int testsToRun);
}
