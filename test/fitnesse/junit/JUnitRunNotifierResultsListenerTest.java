package fitnesse.junit;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class JUnitRunNotifierResultsListenerTest {
  private RunNotifier notifier = mock(RunNotifier.class);
  private JUnitRunNotifierResultsListener listener = new JUnitRunNotifierResultsListener(notifier, getClass());
  private ArgumentCaptor<Failure> arguments = ArgumentCaptor.forClass(Failure.class);

  @Test
  public void shouldFinishSuccessfully() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFinished(any(Description.class));
  }

  @Test
  public void shouldStillFinishOnFailures() {
    TestResult testResult = SlimTestResult.fail("-", "-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(any(Failure.class));
    verify(notifier).fireTestFinished(any(Description.class));
  }

  @Test
  public void shouldStillFinishWhenResultsOkButSummaryReportsFailures() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("W"));

    verify(notifier).fireTestFailure(arguments.capture());
    verify(notifier).fireTestFinished(any(Description.class));

    Failure failure = arguments.getValue();
    assertThat(failure.getMessage(), is("Test failures occurred on page WikiPage"));
  }

  @Test
  public void shouldStillFinishOnErrors() {
    TestResult testResult = SlimTestResult.error("-", "-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(any(Failure.class));
    verify(notifier).fireTestFinished(any(Description.class));
  }

  @Test
  public void shouldStillFinishWhenResultsOkButSummaryReportsExceptions() {
    TestResult testResult = SlimTestResult.ok("-");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("E"));

    verify(notifier).fireTestFailure(arguments.capture());
    verify(notifier).fireTestFinished(any(Description.class));

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
  public void shouldProvideFirstException() {
    TestResult testResult = SlimTestResult.error("Message", "Actual");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), summary("-"));

    verify(notifier).fireTestFailure(arguments.capture());
    Failure failure = arguments.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertThat(failure.getMessage(), is("[Actual] Message"));
  }

  private WikiTestPage mockWikiTestPage() {
    WikiPage root = mock(WikiPage.class);
    when(root.isRoot()).thenReturn(true);

    WikiPage test = mock(WikiPage.class);
    when(test.isRoot()).thenReturn(false);
    when(test.getParent()).thenReturn(root);
    when(test.getName()).thenReturn("WikiPage");
    when(test.getPageCrawler()).thenReturn(new PageCrawlerImpl(test));
    return new WikiTestPage(test);
  }

  private TestSummary summary(String report) {
    int rights = 0;
    int wrongs = 0;
    int ignores = 0;
    int exceptions = 0;

    for (char c : report.toUpperCase().toCharArray())
      if(c == 'R') rights++;
      else if(c == 'W') wrongs++;
      else if(c == 'I') ignores++;
      else if(c == 'E') exceptions++;

    return new TestSummary(rights, wrongs, ignores, exceptions);
  }
}