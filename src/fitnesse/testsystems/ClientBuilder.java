package fitnesse.testsystems;

import fitnesse.wiki.WikiPage;

import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Created with IntelliJ IDEA.
 * User: arjan
 * Date: 6/22/13
 * Time: 8:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientBuilder {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp " + fitnesseJar(System.getProperty("java.class.path")) +
      System.getProperty("path.separator") +
      "%p %m";
  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  protected final WikiPage page;
  protected boolean fastTest;
  protected boolean manualStart;
  protected ExecutionLog log;

  public ClientBuilder(WikiPage page) {
    this.page = page;
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
    String commandPattern = descriptor.getCommandPattern();
    String command = replace(commandPattern, "%p", descriptor.getClassPath());
    command = replace(command, "%m", descriptor.getTestRunner());
    return command;
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public final ExecutionLog getExecutionLog() {
    return log;
  }

  protected final void setExecutionLog(final ExecutionLog log) {
    this.log = log;
  }

  protected Map<String, String> createClasspathEnvironment(String classPath) {
    String classpathProperty = page.readOnlyData().getVariable("CLASSPATH_PROPERTY");
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }
}
