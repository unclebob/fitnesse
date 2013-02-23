// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;

import fitnesse.components.ClassPathBuilder;
import fitnesse.responders.PageFactory;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.PageData;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.WikiPage;

public abstract class TestSystem implements TestSystemListener {
  public static final String DEFAULT_COMMAND_PATTERN =
    "java -cp fitnesse.jar" +
      System.getProperties().get("path.separator") +
      "%p %m";

  public static final String DEFAULT_JAVA_DEBUG_COMMAND = "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_FIND = "runner.exe";
  public static final String DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE = "runnerw.exe";
  protected final WikiPage page;
  protected final TestSystemListener testSystemListener;
  protected boolean fastTest;
  protected boolean manualStart;
  private ExecutionLog log;

  public TestSystem(WikiPage page, TestSystemListener testSystemListener) {
    this.page = page;
    this.testSystemListener = testSystemListener;
  }

  protected String buildCommand(TestSystem.Descriptor descriptor) {
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

  public static Descriptor getDescriptor(WikiPage page, PageFactory pageFactory, boolean isRemoteDebug) {
    return new Descriptor(page, pageFactory, isRemoteDebug);
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
    private final WikiPage page;
    private final ReadOnlyPageData data;
    private final PageFactory pageFactory;
    private final boolean remoteDebug;
    private final String classPath;

    public Descriptor(WikiPage page, PageFactory pageFactory,
        boolean remoteDebug) {
       this(page, pageFactory, remoteDebug,
               new ClassPathBuilder().getClasspath(page.getData().getWikiPage()));
    }

    public Descriptor(Descriptor descriptor) {
      this(descriptor.page, descriptor.pageFactory, descriptor.remoteDebug, descriptor.classPath);
    }

    public Descriptor(Descriptor descriptor, String classPath) {
      this(descriptor.page, descriptor.pageFactory, descriptor.remoteDebug, classPath);
    }

    public Descriptor(WikiPage page, PageFactory pageFactory, boolean remoteDebug, String classPath) {
      this.page = page;
      this.data = page.readOnlyData();
      this.pageFactory = pageFactory;
      this.remoteDebug = remoteDebug;
      this.classPath = classPath;
    }

    public String getTestSystem() {
      String testSystemName = data.getVariable("TEST_SYSTEM");
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
      String program = data.getVariable("REMOTE_DEBUG_RUNNER");
      if (program == null) {
        program = getTestRunnerNormal();
        if (program.toLowerCase().contains(DEFAULT_CSHARP_DEBUG_RUNNER_FIND))
          program = program.toLowerCase().replace(DEFAULT_CSHARP_DEBUG_RUNNER_FIND,
            DEFAULT_CSHARP_DEBUG_RUNNER_REPLACE);
      }
      return program;
    }

    public String getTestRunnerNormal() {
      String program = data.getVariable(PageData.TEST_RUNNER);
      if (program == null)
        program = defaultTestRunner();
      return program;
    }

    String defaultTestRunner() {
      String testSystemType = getTestSystemType(getTestSystem());
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
      String testRunner = data.getVariable("REMOTE_DEBUG_COMMAND");
      if (testRunner == null) {
        testRunner = data.getVariable(PageData.COMMAND_PATTERN);
        if (testRunner == null || testRunner.toLowerCase().contains("java")) {
          testRunner = DEFAULT_JAVA_DEBUG_COMMAND;
        }
      }
      return testRunner;
    }

    private String getNormalCommandPattern() {
      String testRunner = data.getVariable(PageData.COMMAND_PATTERN);
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

    public String getClassPath() {
      return classPath;
    }

    protected ReadOnlyPageData getPageData() {
      return data;
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
}
