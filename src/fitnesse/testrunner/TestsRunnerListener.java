package fitnesse.testrunner;

import java.io.IOException;

public interface TestsRunnerListener {

  void announceNumberTestsToRun(int testsToRun);

  void unableToStartTestSystem(String testSystemName, Throwable cause) throws IOException;
}
