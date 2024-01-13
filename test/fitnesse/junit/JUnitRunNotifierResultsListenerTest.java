package fitnesse.junit;

import fitnesse.slim.SlimServer;
import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.results.SlimExceptionResult;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.fs.WikiPageProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JUnitRunNotifierResultsListenerTest {
  private final RunNotifier notifier = mock(RunNotifier.class);
  private final DescriptionFactory descriptionFactory = mock(DescriptionFactory.class);
  private Description description;
  private final JUnitRunNotifierResultsListener listener = new JUnitRunNotifierResultsListener(notifier, getClass(), descriptionFactory);
  private final ArgumentCaptor<Failure> arguments = ArgumentCaptor.forClass(Failure.class);

  @Before
  public void setUp() {
    description = Description.createTestDescription("myTest", "bla");
    when(descriptionFactory.createDescription(eq(getClass()), any(TestPage.class))).thenReturn(description);
  }

  @Test
  public void shouldFinishSuccessfully() {
    TestResult testResult = SlimTestResult.pass("-");

    listener.announceNumberTestsToRun(1);
    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("R"));
    listener.close();

    verify(notifier).fireTestFinished(description);
  }

  @Test
  public void shouldFinishSuccessfullyWithTooManyTests() {
    TestResult testResult = SlimTestResult.pass();

    listener.announceNumberTestsToRun(0);
    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("R"));
    listener.close();

    verify(notifier).fireTestFinished(description);
  }

  @Test
  public void shouldFailOnTooFewTests() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.announceNumberTestsToRun(2);
    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("R"));
    listener.close();

    verify(notifier).fireTestFailure(any(Failure.class));
    verify(notifier).fireTestFinished(description);
  }

  @Test
  public void shouldStillFinishOnFailures() {
    TestResult testResult = SlimTestResult.fail("-", "-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("F"));

    verify(notifier).fireTestFailure(any(Failure.class));
    verify(notifier).fireTestFinished(description);
  }

  @Test
  public void shouldStillFinishWhenResultsOkButSummaryReportsFailures() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("W"));

    verify(notifier).fireTestFailure(arguments.capture());
    verify(notifier).fireTestFinished(description);

    Failure failure = arguments.getValue();
    assertThat(failure.getMessage(), is("Test failures occurred on page WikiPage"));
  }

  @Test
  public void shouldStillFinishOnErrors() {
    TestResult testResult = SlimTestResult.error("-", "-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("E"));

    verify(notifier).fireTestFailure(any(Failure.class));
    verify(notifier).fireTestFinished(description);
  }

  @Test
  public void shouldReportAsIgnored() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.announceNumberTestsToRun(1);
    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));
    listener.close();

    verify(notifier).fireTestIgnored(description);
  }

  @Test
  public void shouldStillFinishWhenResultsOkButSummaryReportsExceptions() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("E"));

    verify(notifier).fireTestFailure(arguments.capture());
    verify(notifier).fireTestFinished(description);

    Failure failure = arguments.getValue();
    assertThat(failure.getMessage(), is("Exception occurred on page WikiPage"));
  }

  @Test
  public void shouldProvideFirstFailure() {
    TestResult testResult = SlimTestResult.fail("Actual", "Expected");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(AssertionError.class));
    assertThat(failure.getMessage(), is("[Actual] expected [Expected]"));
  }

  @Test
  public void shouldProvideFirstFailureNoMessage() {
    TestResult testResult = SlimTestResult.fail();

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(AssertionError.class));
    assertNull(failure.getMessage());
  }

  @Test
  public void shouldProvideFirstException() {
    TestResult testResult = SlimTestResult.error("Message", "Actual");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertThat(failure.getMessage(), is("[Actual] Message"));
  }

  @Test
  public void shouldProvideFirstExceptionWithMessage() {
    listener.testExceptionOccurred(null, new SlimExceptionResult("ex",
      SlimServer.EXCEPTION_TAG + "message:<<Bad>>"));
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertThat(failure.getMessage(), is("Bad"));
  }

  @Test
  public void shouldProvideFirstExceptionWithoutMessage() {
    listener.testExceptionOccurred(null, new SlimExceptionResult("ex",
      SlimServer.EXCEPTION_TAG + "Bad"));
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertThat(failure.getMessage(), is("Bad"));
  }

  @Test
  public void shouldProvideFirstExceptionWithoutMessageTrimsStacktrace() {
    listener.testExceptionOccurred(null, new SlimExceptionResult("ex",
      SlimServer.EXCEPTION_TAG
        + "java.lang.NullPointerException\n" +
        "\tat fitnesse.fixtures.EchoFixture.nameContains(EchoFixture.java:17) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [rt.jar:1.8.0_192]\n" +
        "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) [rt.jar:1.8.0_192]\n" +
        "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [rt.jar:1.8.0_192]\n" +
        "\tat java.lang.reflect.Method.invoke(Method.java:498) [rt.jar:1.8.0_192]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.methodInvoke(SimpleInteraction.java:256) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.callMethod(SimpleInteraction.java:241) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.invokeMethod(SimpleInteraction.java:223) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.findAndInvoke(SimpleInteraction.java:185) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.MethodExecutor.findAndInvoke(MethodExecutor.java:18) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.FixtureMethodExecutor.execute(FixtureMethodExecutor.java:18) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.StatementExecutor.getMethodExecutionResult(StatementExecutor.java:139) [file:/Users/fitnesse/build/classes/java/main/]"));
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertEquals("java.lang.NullPointerException\n" +
        "\tat fitnesse.fixtures.EchoFixture.nameContains(EchoFixture.java:17) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) [rt.jar:1.8.0_192]\n" +
        "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) [rt.jar:1.8.0_192]\n" +
        "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) [rt.jar:1.8.0_192]\n" +
        "\tat java.lang.reflect.Method.invoke(Method.java:498) [rt.jar:1.8.0_192]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.methodInvoke(SimpleInteraction.java:256) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.callMethod(SimpleInteraction.java:241) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.invokeMethod(SimpleInteraction.java:223) [file:/Users/fitnesse/build/classes/java/main/]\n" +
        "\tat fitnesse.slim.fixtureInteraction.SimpleInteraction.findAndInvoke(SimpleInteraction.java:185) [file:/Users/fitnesse/build/classes/java/main/]",
      failure.getMessage());
  }

  static WikiTestPage mockWikiTestPage() {
    WikiPage root = mock(WikiPage.class);
    when(root.isRoot()).thenReturn(true);

    WikiPage test = mock(WikiPage.class);
    when(test.isRoot()).thenReturn(false);
    when(test.getParent()).thenReturn(root);
    when(test.getName()).thenReturn("WikiPage");
    when(test.getData()).thenReturn(new PageData("content", new WikiPageProperties()));
    when(test.getPageCrawler()).thenCallRealMethod();
    when(test.getFullPath()).thenCallRealMethod();
    return new WikiTestPage(test);
  }

  private TestSummary summary(String report) {
    int rights = 0;
    int wrongs = 0;
    int ignores = 0;
    int exceptions = 0;

    for (char c : report.toUpperCase().toCharArray())
      if (c == 'R') rights++;
      else if (c == 'W') wrongs++;
      else if (c == 'I') ignores++;
      else if (c == 'E') exceptions++;

    return new TestSummary(rights, wrongs, ignores, exceptions);
  }
}
