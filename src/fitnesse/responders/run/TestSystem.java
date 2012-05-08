// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.responders.PageFactory;
import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.net.SocketException;
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

  public ExecutionLog getExecutionLog(String classPath, TestSystem.Descriptor descriptor) throws SocketException {
    log = createExecutionLog(classPath, descriptor);
    return log;
  }

  protected abstract ExecutionLog createExecutionLog(String classPath, Descriptor descriptor) throws SocketException;

  protected String buildCommand(TestSystem.Descriptor descriptor, String classPath) {
    String commandPattern = descriptor.commandPattern;
    String command = replace(commandPattern, "%p", classPath);
    command = replace(command, "%m", descriptor.testRunner);
    return command;
  }

  private static String getRemoteDebugCommandPattern(ReadOnlyPageData pageData) {
    String testRunner = pageData.getVariable("REMOTE_DEBUG_COMMAND");
    if (testRunner == null) {
      testRunner = pageData.getVariable(PageData.COMMAND_PATTERN);
      if (testRunner == null || testRunner.toLowerCase().contains("java")) {
        testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
      }
    }
    return testRunner;
  }

  private static String getNormalCommandPattern(ReadOnlyPageData pageData) {
    String testRunner = pageData.getVariable(PageData.COMMAND_PATTERN);
    if (testRunner == null)
      testRunner = DEFAULT_COMMAND_PATTERN;
    return testRunner;
  }

  private static String getCommandPattern(ReadOnlyPageData pageData, boolean isRemoteDebug) {
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

  public static String getTestSystemName(PageData data) {
    String testSystemName = getTestSystem(data);
    String testRunner = getTestRunnerNormal(data);
    return String.format("%s:%s", testSystemName, testRunner);
  }

  private static String getTestSystem(ReadOnlyPageData data) {
    String testSystemName = data.getVariable("TEST_SYSTEM");
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  public static String getPathSeparator(ReadOnlyPageData pageData) {
    String separator = pageData.getVariable(PageData.PATH_SEPARATOR);
    if (separator == null)
      separator = (String) System.getProperties().get("path.separator");
    return separator;
  }

  public static String getTestSystemType(String testSystemName) {
    String parts[] = testSystemName.split(":");
    return parts[0];
  }

  public void acceptOutputFirst(String output) throws IOException {
    testSystemListener.acceptOutputFirst(output);
  }

  public void testComplete(TestSummary testSummary) throws IOException {
    testSystemListener.testComplete(testSummary);
  }

  public void exceptionOccurred(Throwable e) {
    log.addException(e);
    log.addReason("Test execution aborted abnormally with error code " + log.getExitCode());
    testSystemListener.exceptionOccurred(e);
  }

  public abstract void start() throws IOException;

  private static String getTestRunner(ReadOnlyPageData pageData, boolean isRemoteDebug) {
    if (isRemoteDebug)
      return getTestRunnerDebug(pageData);
    else
      return getTestRunnerNormal(pageData);
  }

  
  private static String getTestRunnerDebug(ReadOnlyPageData data) {
    String program = data.getVariable("REMOTE_DEBUG_RUNNER");
    if (program == null) {
      program = getTestRunnerNormal(data);
      if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
        program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND, 
                                                DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE); 
    }
    return program;
  }

  public static String getTestRunnerNormal(ReadOnlyPageData data) {
    String program = data.getVariable(PageData.TEST_RUNNER);
    if (program == null)
      program = defaultTestRunner(data);
    return program;
  }

  static String defaultTestRunner(ReadOnlyPageData data) {
    String testSystemType = getTestSystemType(getTestSystem(data));
    if ("slim".equalsIgnoreCase(testSystemType))
      return "fitnesse.slim.SlimService";
    else
      return "fit.FitServer";
  }

  public abstract void bye() throws IOException, InterruptedException;

  public abstract boolean isSuccessfullyStarted();

  public abstract void kill() throws IOException;

  public abstract String runTestsAndGenerateHtml(ReadOnlyPageData pageData) throws IOException, InterruptedException;

  public static Descriptor getDescriptor(ReadOnlyPageData data, PageFactory pageFactory, boolean isRemoteDebug) {
    String testSystemName = getTestSystem(data);
    String testRunner = getTestRunner(data, isRemoteDebug);
    String commandPattern = getCommandPattern(data, isRemoteDebug);
    String pathSeparator = getPathSeparator(data);
    return new Descriptor(testSystemName, testRunner, commandPattern, pathSeparator, pageFactory);
  }

  protected Map<String, String> createClasspathEnvironment(String classPath) {
    String classpathProperty = page.readOnlyData().getVariable("CLASSPATH_PROPERTY");
    Map<String, String> environmentVariables = null;
    if (classpathProperty != null) {
      environmentVariables = Collections.singletonMap(classpathProperty, classPath);
    }
    return environmentVariables;
  }

  public static class Descriptor {
    public final String testSystemName;
    public final String testRunner;
    public final String commandPattern;
    public final String pathSeparator;
    public final PageFactory pageFactory;

    Descriptor(String testSystemName, String testRunner, String commandPattern, String pathSeparator, PageFactory pageFactory) {
      this.testSystemName = testSystemName;
      this.testRunner = testRunner;
      this.commandPattern = commandPattern;
      this.pathSeparator = pathSeparator;
      this.pageFactory = pageFactory;
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
