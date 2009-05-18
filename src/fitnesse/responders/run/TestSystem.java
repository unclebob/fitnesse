// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public abstract class TestSystem implements TestSystemListener {
  public static final String DEFAULT_COMMAND_PATTERN = "java -cp %p %m";
  public static final String DEFAULT_DEBUG_PATTERN = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  protected WikiPage page;
  protected boolean fastTest;
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
    if (testRunner == null)
      testRunner = DEFAULT_DEBUG_PATTERN;
    return testRunner;
  }

  private static String getNormalCommandPattern(PageData pageData) throws Exception {
    String testRunner = pageData.getVariable("COMMAND_PATTERN");
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

  public static String getTestSystemName(PageData data) throws Exception {
    String testSystemName = getTestSystem(data);
    String testRunner = getTestRunner(data);
    return String.format("%s:%s", testSystemName, testRunner);
  }

  private static String getTestSystem(PageData data) throws Exception {
    String testSystemName = data.getVariable("TEST_SYSTEM");
    if (testSystemName == null)
      return "fit";
    return testSystemName;
  }

  public static String getPathSeparator(PageData pageData) throws Exception {
    String separator = pageData.getVariable("PATH_SEPARATOR");
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

  public void acceptResultsLast(TestSummary testSummary) throws Exception {
    testSystemListener.acceptResultsLast(testSummary);
  }

  public void exceptionOccurred(Throwable e) {
    log.addException(e);
    log.addReason("Test execution aborted abnormally with error code " + log.getExitCode());
    testSystemListener.exceptionOccurred(e);
  }

  public abstract void start() throws Exception;

  public static String getTestRunner(PageData data) throws Exception {
    String program = data.getVariable("TEST_RUNNER");
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
    String testRunner = getTestRunner(data);
    String commandPattern = getCommandPattern(data, isRemoteDebug);
    String pathSeparator = getPathSeparator(data);
    return new Descriptor(testSystemName, testRunner, commandPattern, pathSeparator);
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
