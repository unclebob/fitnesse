package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public abstract class ClientBuilder<T> {

  private final ReadOnlyPageData data;
  protected boolean fastTest;
  protected boolean manualStart;
  protected boolean remoteDebug;

  public ClientBuilder(ReadOnlyPageData data) {
    this.data = data;
  }

  protected String buildCommand(String commandPattern, String testRunner, String classPath) {
    String command = Descriptor.replace(commandPattern, "%p", classPath);
    command = Descriptor.replace(command, "%m", testRunner);
    return command;
  }

  public ClientBuilder<T> withFastTest(boolean fastTest) {
    this.fastTest = fastTest;
    return this;
  }

  public ClientBuilder<T> withManualStart(boolean manualStart) {
    this.manualStart = manualStart;
    return this;
  }

  public ClientBuilder<T> withRemoteDebug(boolean remoteDebug) {
    this.remoteDebug = remoteDebug;
    return this;
  }

  public abstract T build() throws IOException;

  protected Map<String, String> createClasspathEnvironment(String classPath) {
    String classpathProperty = getVariable(Descriptor.CLASSPATH_PROPERTY);
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }

  public static Descriptor getDescriptor(WikiPage page, boolean remoteDebug) {
    return new Descriptor(page.readOnlyData(), remoteDebug);
  }

  protected String getVariable(String name) {
    return data.getVariable(name);
  }

  private String getTestRunnerDebug() {
    String program = getVariable(Descriptor.REMOTE_DEBUG_RUNNER);
    if (program == null) {
      program = getTestRunnerNormal();
      if (program.toLowerCase().contains(Descriptor.DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(Descriptor.DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
                Descriptor.DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
    }
    return program;
  }

  public String getTestRunnerNormal() {
    String program = getVariable(Descriptor.TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner();
    return program;
  }

  protected abstract String defaultTestRunner();

  public String getTestRunner() {
    if (remoteDebug)
      return getTestRunnerDebug();
    else
      return getTestRunnerNormal();
  }

  private String getRemoteDebugCommandPattern() {
    String testRunner = getVariable(Descriptor.REMOTE_DEBUG_COMMAND);
    if (testRunner == null) {
      testRunner = getVariable(Descriptor.COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = Descriptor.DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private String getNormalCommandPattern() {
    String testRunner = getVariable(Descriptor.COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = Descriptor.DEFAULT_COMMAND_PATTERN;
    return testRunner;
  }

  public String getCommandPattern() {
    if (remoteDebug)
      return getRemoteDebugCommandPattern();
    else
      return getNormalCommandPattern();
  }
}
