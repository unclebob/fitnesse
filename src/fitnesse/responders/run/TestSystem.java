package fitnesse.responders.run;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public abstract class TestSystem implements TestSystemListener {
  public static final String DEFAULT_COMMAND_PATTERN = "java -cp %p %m";
  protected WikiPage page;
  protected boolean fastTest;
  protected static final String emptyPageContent = "OH NO! This page is empty!";
  protected TestSystemListener testSystemListener;
  protected ExecutionLog log;

  public TestSystem(WikiPage page, TestSystemListener testSystemListener) {
    this.page = page;
    this.testSystemListener = testSystemListener;
  }

  public ExecutionLog getExecutionLog(String classPath, String className) throws Exception {
    log = createExecutionLog(classPath, className);
    return log;
  }

  protected abstract ExecutionLog createExecutionLog(String classPath, String className) throws Exception;

  protected String buildCommand(String program, String classPath) throws Exception {
    String testRunner = page.getData().getVariable("COMMAND_PATTERN");
    if (testRunner == null)
      testRunner = DEFAULT_COMMAND_PATTERN;
    String command = replace(testRunner, "%p", classPath);
    command = replace(command, "%m", program);
    return command;
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
}
