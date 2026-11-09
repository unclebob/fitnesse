package fitnesse.responders.run;

import java.util.HashMap;
import java.util.Map;

import fitnesse.testrunner.RunningTestingTracker.RunningProcess;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;

/**
 * Remembers the processes known to the running testing tracker while a test page is executed.
 */
class RunningProcessRecorder implements TestSystemListener {
  private final Map<String, RunningProcess> processes = new HashMap<>();

  @Override
  public void testStarted(TestPage testPage) {
    for (RunningProcess process : SuiteResponder.runningTestingTracker.getRunningProcesses()) {
      processes.put(process.getId(), process);
    }
  }

  RunningProcess getProcess(String id) {
    return processes.get(id);
  }
}
