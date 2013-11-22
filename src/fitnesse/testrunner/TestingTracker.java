package fitnesse.testrunner;

/**
 *
 */
public interface TestingTracker {

  String addStartedProcess(Stoppable process);

  void removeEndedProcess(String stopId);
}
