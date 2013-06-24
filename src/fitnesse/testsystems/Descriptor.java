package fitnesse.testsystems;

import fitnesse.components.ClassPathBuilder;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

@Deprecated
public class Descriptor extends DescriptorBase {
  private final WikiPage page;
  private final ReadOnlyPageData data;
  private final boolean remoteDebug;

  public Descriptor(WikiPage page, boolean remoteDebug) {
    this.page = page;
    this.data = page.readOnlyData();
    this.remoteDebug = remoteDebug;
  }

  public String getTestSystem() {
    String testSystemName = data.getVariable("TEST_SYSTEM");
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  public String getTestSystemName() {
    String testSystemName = getTestSystem();
    String testRunner = getTestRunnerNormal();
    return String.format("%s:%s", testSystemName, testRunner);
  }

  private String getTestRunnerDebug() {
    String program = data.getVariable("REMOTE_DEBUG_RUNNER");
    if (program == null) {
      program = getTestRunnerNormal();
      if (program.toLowerCase().contains(ClientBuilder.DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(ClientBuilder.DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
          ClientBuilder.DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
    }
    return program;
  }

  public String getTestRunnerNormal() {
    String program = data.getVariable(ClientBuilder.TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner();
    return program;
  }

  String defaultTestRunner() {
    String testSystemType = ClientBuilder.getTestSystemType(getTestSystem());
    if ("slim".equalsIgnoreCase(testSystemType))
      return "fitnesse.slim.SlimService";
    else
      return "fit.FitServer";
  }


  public String getTestRunner() {
    if (remoteDebug)
      return getTestRunnerDebug();
    else
      return getTestRunnerNormal();
  }

  private String getRemoteDebugCommandPattern() {
    String testRunner = data.getVariable("REMOTE_DEBUG_COMMAND");
    if (testRunner == null) {
      testRunner = data.getVariable(ClientBuilder.COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = ClientBuilder.DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private String getNormalCommandPattern() {
    String testRunner = data.getVariable(ClientBuilder.COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = ClientBuilder.DEFAULT_COMMAND_PATTERN;
    return testRunner;
  }

  public String getCommandPattern() {
    if (remoteDebug)
      return getRemoteDebugCommandPattern();
    else
      return getNormalCommandPattern();
  }
}
