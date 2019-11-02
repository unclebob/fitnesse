package fitnesse.testsystems;

import org.apache.commons.lang3.SystemUtils;

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
          "%p",
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

  protected String[] buildCommand(String[] commandPattern, String testRunner, ClassPath classPath) {

    ClassPath completeClassPath;
    if (isJava(commandPattern[0])) {
      completeClassPath = classPath.withLocationForClass(testRunner);
    } else {
      completeClassPath = classPath;
    }

    String[] command = new String[commandPattern.length];
    for (int i = 0; i < commandPattern.length; i++) {
      command[i] = replace(commandPattern[i], "%p", completeClassPath.toString());
      command[i] = replace(command[i], "%m", testRunner);
      if (SystemUtils.IS_OS_WINDOWS && command[i].contains(" ")) {
        command[i] = "\"" + command[i] + "\"";
      }
    }
    return command;
  }

  private boolean isJava(String command) {
    return command.toLowerCase().contains("java");
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
    if (testRunner == null || isJava(testRunner)) {
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
		Collection<String> result = new ArrayList<>();
		Pattern p = Pattern.compile("\"([^\"]*)\"|[\\S]+");
		Matcher m = p.matcher(commandLine);
		while(m.find())
		{
		  String token = (m.group(1)==null) ? m.group(0) : m.group(1);
		  result.add(token);
		}
		return result.toArray(new String[result.size()]);
  }

  public Map<String, String> createClasspathEnvironment(ClassPath classPath) {
    String classpathProperty = getVariable(CLASSPATH_PROPERTY);
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath.toString());
    }
    return environmentVariables;
  }

  public ClassPath getClassPath() {
    return descriptor.getClassPath();
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

  protected static String javaExecutable() {
    String javaHome = System.getenv("JAVA_HOME");
    String result;
    if (javaHome != null) {
      result = javaHome + File.separator + "bin" + File.separator + "java";
    } else {
      result = "java";
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
