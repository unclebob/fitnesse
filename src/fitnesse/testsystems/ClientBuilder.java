package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class ClientBuilder<T> {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp " + fitnesseJar(System.getProperty("java.class.path")) +
      System.getProperty("path.separator") +
      "%p %m";
  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";

  public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  public static final String REMOTE_DEBUG_COMMAND = "REMOTE_DEBUG_COMMAND";
  public static final String TEST_RUNNER = "TEST_RUNNER";
  public static final String REMOTE_DEBUG_RUNNER = "REMOTE_DEBUG_RUNNER";
  public static final String CLASSPATH_PROPERTY = "CLASSPATH_PROPERTY";
  public static final String TEST_SYSTEM = "TEST_SYSTEM";

  private final ReadOnlyPageData data;
  protected boolean fastTest;
  protected boolean manualStart;
  protected boolean remoteDebug;

  public ClientBuilder(ReadOnlyPageData data) {
    this.data = data;
  }

  protected static String fitnesseJar(String classpath) {
    for (String pathEntry: classpath.split(System.getProperty("path.separator"))) {
      String[] paths = pathEntry.split(java.util.regex.Pattern.quote(System.getProperty("file.separator")));
      String jarFile = paths[paths.length-1];
      if ("fitnesse-standalone.jar".equals(jarFile)) {
        return pathEntry;
      }
      if (jarFile.matches("fitnesse-\\d\\d\\d\\d\\d\\d\\d\\d.jar")) {
        return pathEntry;
      }
      if (jarFile.matches("fitnesse-standalone-\\d\\d\\d\\d\\d\\d\\d\\d.jar")) {
        return pathEntry;
      }
    }

    return "fitnesse.jar";
  }

  protected static String replace(String value, String mark, String replacement) {
    return value.replaceAll(mark, Matcher.quoteReplacement(replacement));
  }

  public static String getTestSystemType(String testSystemName) {
    String parts[] = testSystemName.split(":");
    return parts[0];
  }

  protected String buildCommand(String commandPattern, String testRunner, String classPath) {
    String command = replace(commandPattern, "%p", classPath);
    command = replace(command, "%m", testRunner);
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
    String classpathProperty = getVariable(CLASSPATH_PROPERTY);
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
    String program = getVariable(REMOTE_DEBUG_RUNNER);
    if (program == null) {
      program = getTestRunnerNormal();
      if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
                DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
    }
    return program;
  }

  public String getTestRunnerNormal() {
    String program = getVariable(TEST_RUNNER);
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
    String testRunner = getVariable(REMOTE_DEBUG_COMMAND);
    if (testRunner == null) {
      testRunner = getVariable(COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private String getNormalCommandPattern() {
    String testRunner = getVariable(COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = DEFAULT_COMMAND_PATTERN;
    return testRunner;
  }

  public String getCommandPattern() {
    if (remoteDebug)
      return getRemoteDebugCommandPattern();
    else
      return getNormalCommandPattern();
  }
}
