// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

import java.util.Collections;
import java.util.Map;

public abstract class TestSystem implements TestSystemListener {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp fitnesse.jar" +
    System.getProperties().get("path.separator") +
    "%p %m";
  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  protected WikiPage page;
  protected boolean fastTest;
  protected boolean manualStart;
  protected static final String emptyPageContent = "OH NO! This page is empty!";
  protected TestSystemListener testSystemListener;
  protected ExecutionLog log;

  public TestSystem(WikiPage page, TestSystemListener testSystemListener) {
    this.page = page;
    this.testSystemListener = testSystemListener;
  }

  public ExecutionLog getExecutionLog(String classPath, TestSystem.Descriptor descriptor) throws Exception {
    log = createExecutionLog(classPath, descriptor);
    return log;
  }

  protected abstract ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws Exception;

  protected String buildCommand(TestSystem.Descriptor descriptor, String classPath) throws Exception {
    String commandPattern = descriptor.commandPattern;
    String command = replace(commandPattern, "%p", classPath);
    command = replace(command, "%m", descriptor.testRunner);
    return command;
  }

  private static String getRemoteDebugCommandPattern(PageData pageData) throws Exception {
    String testRunner = pageData.getVariable("REMOTE_DEBUG_COMMAND");
    if (testRunner == null) {
      testRunner = pageData.getVariable(PageData.COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private static String getNormalCommandPattern(PageData pageData) throws Exception {
    String testRunner = pageData.getVariable(PageData.COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = DEFAULT_COMMAND_PATTERN;
    return testRunner;
  }

  private static String getCommandPattern(PageData pageData, boolean isRemoteDebug) throws Exception {
    if (isRemoteDebug)
      return getRemoteDebugCommandPattern(pageData);
    else
      return getNormalCommandPattern(pageData);
  }

  // String.replaceAll(...) is not trustworthy because it seems to remove all '\' characters.
  protected static String replace(String value, String mark, String replacement) {
    int index = value.indexOf(mark);
    if (index == -1)
      return value;

    return value.substring(0, index) + replacement + value.substring(index + mark.length());
  }
  public void setFastTest(boolean fastTest) {
    this.fastTest = fastTest;
  }

  public void setManualStart(boolean manualStart) {
    this.manualStart = manualStart;
  }

  public static String getTestSystemName(PageData data) throws Exception {
    String testSystemName = getTestSystem(data);
    String testRunner = getTestRunnerNormal(data);
    return String.format("%s:%s", testSystemName, testRunner);
  }

  private static String getTestSystem(PageData data) throws Exception {
    String testSystemName = data.getVariable("TEST_SYSTEM");
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  public static String getPathSeparator(PageData pageData) throws Exception {
    String separator = pageData.getVariable(PageData.PATH_SEPARATOR);
    if (separator == null)
      separator = (String) System.getProperties().get("path.separator");
    return separator;
  }

  public static String getTestSystemType(String testSystemName) throws Exception {
    String parts[] = testSystemName.split(":");
    return parts[0];
  }

  public void acceptOutputFirst(String output) throws Exception {
    testSystemListener.acceptOutputFirst(output);
  }

  public void testComplete(TestSummary testSummary) throws Exception {
    testSystemListener.testComplete(testSummary);
  }

  public void exceptionOccurred(Throwable e) {
    log.addException(e);
    log.addReason("Test execution aborted abnormally with error code " + log.getExitCode());
    testSystemListener.exceptionOccurred(e);
  }

  public abstract void start() throws Exception;

  private static String getTestRunner(PageData pageData, boolean isRemoteDebug) throws Exception {
    if (isRemoteDebug)
      return getTestRunnerDebug(pageData);
    else
      return getTestRunnerNormal(pageData);
  }

  
  private static String getTestRunnerDebug(PageData data) throws Exception {
    String program = data.getVariable("REMOTE_DEBUG_RUNNER");
    if (program == null) {
      program = getTestRunnerNormal(data);
      if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND, 
                                                DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE); 
    }
    return program;
  }

  public static String getTestRunnerNormal(PageData data) throws Exception {
    String program = data.getVariable(PageData.TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner(data);
    return program;
  }

  static String defaultTestRunner(PageData data) throws Exception {
    String testSystemType = getTestSystemType(getTestSystem(data));
    if ("slim".equalsIgnoreCase(testSystemType))
      return "fitnesse.slim.SlimService";
    else
      return "fit.FitServer";
  }

  public abstract void bye() throws Exception;

  public abstract boolean isSuccessfullyStarted();

  public abstract void kill() throws Exception;

  public abstract String runTestsAndGenerateHtml(PageData pageData) throws Exception;

  public static Descriptor getDescriptor(PageData data, boolean isRemoteDebug) throws Exception {
    String testSystemName = getTestSystem(data);
    String testRunner = getTestRunner(data, isRemoteDebug);
    String commandPattern = getCommandPattern(data, isRemoteDebug);
    String pathSeparator = getPathSeparator(data);
    return new Descriptor(testSystemName, testRunner, commandPattern, pathSeparator);
  }

  protected Map<String, String> createClasspathEnvironment(String classPath) throws Exception {
    String classpathProperty = page.getData().getVariable("CLASSPATH_PROPERTY");
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }

  public static class Descriptor {
    public String testSystemName;
    public String testRunner;
    public String commandPattern;
    public String pathSeparator;

    public Descriptor(String testSystemName, String testRunner, String commandPattern, String pathSeparator) {
      this.testSystemName = testSystemName;
      this.testRunner = testRunner;
      this.commandPattern = commandPattern;
      this.pathSeparator = pathSeparator;
    }

    public int hashCode() {
      return testSystemName.hashCode() ^ testRunner.hashCode() ^ commandPattern.hashCode() ^ pathSeparator.hashCode();
    }

    public boolean equals(Object obj) {
      Descriptor d = (Descriptor) obj;
      return d.testSystemName.equals(testSystemName) &&
        d.testRunner.equals(testRunner) &&
        d.commandPattern.equals(commandPattern) &&
        d.pathSeparator.equals(pathSeparator);
    }
  }
}
