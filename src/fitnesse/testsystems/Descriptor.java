package fitnesse.testsystems;

import fitnesse.wiki.ReadOnlyPageData;

import java.util.regex.Matcher;

public class Descriptor extends DescriptorBase {
  public static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp " + fitnesseJar(System.getProperty("java.class.path")) +
      System.getProperty("path.separator") +
      "%p %m";
  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  public static final String REMOTE_DEBUG_COMMAND = "REMOTE_DEBUG_COMMAND";
  public static final String TEST_RUNNER = "TEST_RUNNER";
  public static final String REMOTE_DEBUG_RUNNER = "REMOTE_DEBUG_RUNNER";
  public static final String CLASSPATH_PROPERTY = "CLASSPATH_PROPERTY";
  public static final String TEST_SYSTEM = "TEST_SYSTEM";
  private final ReadOnlyPageData data;
  private final boolean remoteDebug;

  public Descriptor(ReadOnlyPageData data, boolean remoteDebug) {
    this.data = data;
    this.remoteDebug = remoteDebug;
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

  public String getTestSystem() {
    String testSystemName = data.getVariable(TEST_SYSTEM);
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
    String program = data.getVariable(REMOTE_DEBUG_RUNNER);
    if (program == null) {
      program = getTestRunnerNormal();
      if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
          DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
    }
    return program;
  }

  public String getTestRunnerNormal() {
    String program = data.getVariable(TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner();
    return program;
  }

  String defaultTestRunner() {
    String testSystemType = getTestSystem();
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
    String testRunner = data.getVariable(REMOTE_DEBUG_COMMAND);
    if (testRunner == null) {
      testRunner = data.getVariable(COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private String getNormalCommandPattern() {
    String testRunner = data.getVariable(COMMAND_PATTERN);
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

  @Override
  public int hashCode() {
    return getTestSystemName().hashCode() ^ getTestRunner().hashCode() ^ getCommandPattern().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    Descriptor descriptor = (Descriptor) obj;
    return descriptor.getTestSystemName().equals(getTestSystemName()) &&
            descriptor.getTestRunner().equals(getTestRunner()) &&
            descriptor.getCommandPattern().equals(getCommandPattern());
  }

}
