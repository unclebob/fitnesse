package fitnesse.testsystems;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ClientBuilder<T> {
  static final String COMMAND_PATTERN = "COMMAND_PATTERN";
  static final String[] DEFAULT_COMMAND_PATTERN = {
          javaExecutable(),
          "-cp",
          fitnesseJar(System.getProperty("java.class.path")) + File.pathSeparator + "%p",
          "%m" };
  static final String[] DEFAULT_JAVA_DEBUG_COMMAND = {
          javaExecutable(),
          "-Xdebug",
          "-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000",
          "-cp",
          "%p",
          "%m"};
  static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  static final String REMOTE_DEBUG_COMMAND = "REMOTE_DEBUG_COMMAND";
  static final String TEST_RUNNER = "TEST_RUNNER";
  static final String REMOTE_DEBUG_RUNNER = "REMOTE_DEBUG_RUNNER";
  static final String CLASSPATH_PROPERTY = "CLASSPATH_PROPERTY";

  private final Descriptor descriptor;

  public ClientBuilder(Descriptor descriptor) {
    this.descriptor = descriptor;
  }

  protected String[] buildCommand(String[] commandPattern, String testRunner, String classPath) {
    String[] command = new String[commandPattern.length];
    for (int i = 0; i < commandPattern.length; i++) {
      command[i] = replace(commandPattern[i], "%p", classPath);
      command[i] = replace(command[i], "%m", testRunner);
    }
    return command;
  }


  protected static String replace(String value, String mark, String replacement) {
    return value.replaceAll(mark, Matcher.quoteReplacement(replacement));
  }

  public abstract T build() throws IOException;

  protected abstract String defaultTestRunner();

  public String getTestSystemName() {
    String testSystemName = descriptor.getTestSystem();
    String testRunner = getTestRunnerNormal();
    return String.format("%s:%s", testSystemName, testRunner);
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


  public String getTestRunner() {
    if (isDebug())
      return getTestRunnerDebug();
    else
      return getTestRunnerNormal();
  }

  private String[] getRemoteDebugCommandPattern() {
    String testRunner = getVariable(REMOTE_DEBUG_COMMAND);
    if (testRunner != null)
      return parseCommandLine(testRunner);
    testRunner = getVariable(COMMAND_PATTERN);
    if (testRunner == null || testRunner.toLowerCase().contains("java")) {
      return DEFAULT_JAVA_DEBUG_COMMAND;
    }
    return parseCommandLine(testRunner);
  }


  public String[] getCommandPattern() {
    if (isDebug())
      return getRemoteDebugCommandPattern();
    else
      return getNormalCommandPattern();
  }

  private String[] getNormalCommandPattern() {
    String testRunner = getVariable(COMMAND_PATTERN);
    if (testRunner != null)
      return parseCommandLine(testRunner);
    return DEFAULT_COMMAND_PATTERN;
  }

  protected String[] parseCommandLine(String commandLine) {
		Collection<String> result = new ArrayList<String>();
		Pattern p = Pattern.compile("\"([^\"]*)\"|[\\S]+");
		Matcher m = p.matcher(commandLine);
		while(m.find())
		{
		  String token = (m.group(1)==null) ? m.group(0) : m.group(1);   
		  result.add(token);
		}
		return result.toArray(new String[result.size()]); 
  }

  public Map<String, String> createClasspathEnvironment(String classPath) {
    String classpathProperty = getVariable(CLASSPATH_PROPERTY);
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }

  public String getClassPath() {
    return descriptor.getClassPath().toString();
  }

  public boolean isDebug() {
    return descriptor.isDebug();
  }

  public String getVariable(String name) {
    return descriptor.getVariable(name);
  }

  public ExecutionLogListener getExecutionLogListener() {
    return new DecoratingExecutionLogListener(getTestSystemName(), descriptor.getExecutionLogListener());
  }

  private String getTestRunnerNormal() {
    String program = getVariable(TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner();
    return program;
  }

  protected static String fitnesseJar(String classpath) {
    for (String pathEntry: classpath.split(File.pathSeparator)) {
      String[] paths = pathEntry.split(java.util.regex.Pattern.quote(File.separator));
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
      if (jarFile.matches("fitnesse-\\d\\d\\d\\d\\d\\d\\d\\d-standalone.jar")) {
        return pathEntry;
      }
    }

    return "fitnesse.jar";
  }

  protected static String javaExecutable() {
    String javaHome = System.getenv("JAVA_HOME");
    String result = "java";
    if (javaHome != null) {
      boolean wrapInQuotes = javaHome.contains(" "); 
      String separator = File.separator;
      result = javaHome + separator + "bin" + separator + "java"; 
      if (wrapInQuotes) {
    	  result = "\"" + result + "\"";
      }
    }
    return result;
  }

  private static class DecoratingExecutionLogListener implements ExecutionLogListener {
    private final String testSystemName;
    private final ExecutionLogListener executionLogListener;

    private DecoratingExecutionLogListener(String testSystemName, ExecutionLogListener executionLogListener) {
      this.testSystemName = testSystemName;
      this.executionLogListener = executionLogListener;
    }

    @Override
    public void commandStarted(final ExecutionContext context) {
      executionLogListener.commandStarted(new ExecutionContext() {

        @Override
        public String getCommand() {
          return context.getCommand();
        }

        @Override
        public String getTestSystemName() {
          return testSystemName;
        }
      });
    }

    @Override
    public void stdOut(String output) {
      executionLogListener.stdOut(output);
    }

    @Override
    public void stdErr(String output) {
      executionLogListener.stdErr(output);
    }

    @Override
    public void exitCode(int exitCode) {
      executionLogListener.exitCode(exitCode);
    }

    @Override
    public void exceptionOccurred(Throwable e) {
      executionLogListener.exceptionOccurred(e);
    }
  }
}
