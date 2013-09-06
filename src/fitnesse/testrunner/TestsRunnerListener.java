package fitnesse.testrunner;

public interface TestsRunnerListener {

  void setTrackingId(String stopResponderId);

  void announceNumberTestsToRun(int testsToRun);
}
