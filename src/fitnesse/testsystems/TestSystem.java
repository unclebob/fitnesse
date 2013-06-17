// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPage;

public abstract class TestSystem implements TestSystemListener {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp " + fitnesseJar(System.getProperty("java.class.path")) +
      System.getProperty("path.separator") +
      "%p %m";

  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  protected final WikiPage page;
  protected final TestSystemListener testSystemListener;
  protected boolean fastTest;
  protected boolean manualStart;
  private ExecutionLog log;

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

  public TestSystem(WikiPage page, TestSystemListener testSystemListener) {
    this.page = page;
    this.testSystemListener = testSystemListener;
  }

  protected String buildCommand(Descriptor descriptor) {
    String commandPattern = descriptor.getCommandPattern();
    String command = replace(commandPattern, "%p", descriptor.getClassPath());
    command = replace(command, "%m", descriptor.getTestRunner());
    return command;
  }

  protected static String replace(String value, String mark, String replacement) {
    return value.replaceAll(mark, Matcher.quoteReplacement(replacement));
  }

  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public static String getTestSystemType(String testSystemName) {
    String parts[] = testSystemName.split(":");
    return parts[0];
  }

  @Override
  public void testOutputChunk(String output) throws IOException {
    testSystemListener.testOutputChunk(output);
  }

  @Override
  public void testComplete(TestSummary testSummary) throws IOException {
    testSystemListener.testComplete(testSummary);
  }

  @Override
  public void exceptionOccurred(Throwable e) {
    log.addException(e);
    testSystemListener.exceptionOccurred(e);
  }

  @Override
  public void testAssertionVerified(Assertion assertion, TestResult testResult) {
    testSystemListener.testAssertionVerified(assertion, testResult);
  }

  @Override
  public void testExceptionOccurred(Assertion assertion, ExceptionResult exceptionResult) {
    testSystemListener.testExceptionOccurred(assertion, exceptionResult);
  }

  public abstract void start() throws IOException;

  public abstract void bye() throws IOException, InterruptedException;

  public abstract boolean isSuccessfullyStarted();

  public abstract void kill() throws IOException;

  public abstract void runTests(TestPage pageToTest) throws IOException, InterruptedException;

  public final ExecutionLog getExecutionLog() {
    return log;
  }

  protected final void setExecutionLog(final ExecutionLog log) {
    this.log = log;
  }

  public static Descriptor getDescriptor(WikiPage page, boolean isRemoteDebug) {
    return new Descriptor(page, isRemoteDebug);
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
