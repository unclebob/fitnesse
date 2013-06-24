package fitnesse.testsystems;

import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

public abstract class ClientBuilder {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp " + fitnesseJar(System.getProperty("java.class.path")) +
      System.getProperty("path.separator") +
      "%p %m";
  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";

  protected final WikiPage page;
  private final ReadOnlyPageData data;
  protected boolean fastTest;
  protected boolean manualStart;
  protected boolean remoteDebug;

  public ClientBuilder(WikiPage page) {
    this.page = page;
    this.data = page.getData();
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

  protected String buildCommand(Descriptor descriptor) {
    return buildCommand(descriptor.getCommandPattern(), descriptor.getTestRunner(), descriptor.getClassPath());
  }

  protected String buildCommand(String commandPattern, String testRunner, String classPath) {
    String command = replace(commandPattern, "%p", classPath);
    command = replace(command, "%m", testRunner);
    return command;
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public void setRemoteDebug(boolean remoteDebug) {
    this.remoteDebug = remoteDebug;
  }

  protected Map<String, String> createClasspathEnvironment(String classPath) {
    String classpathProperty = page.readOnlyData().getVariable("CLASSPATH_PROPERTY");
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }

  public static Descriptor getDescriptor(WikiPage page, boolean isRemoteDebug) {
    return new Descriptor(page, isRemoteDebug);
  }

  protected String getVariable(String name) {
    return data.getVariable(name);
  }

  private String getTestRunnerDebug() {
    String program = getVariable("REMOTE_DEBUG_RUNNER");
    if (program == null) {
      program = getTestRunnerNormal();
      if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
                DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
    }
    return program;
  }

  public String getTestRunnerNormal() {
    String program = getVariable(PageData.TEST_RUNNER);
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
    String testRunner = getVariable("REMOTE_DEBUG_COMMAND");
    if (testRunner == null) {
      testRunner = getVariable(PageData.COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private String getNormalCommandPattern() {
    String testRunner = getVariable(PageData.COMMAND_PATTERN);
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
