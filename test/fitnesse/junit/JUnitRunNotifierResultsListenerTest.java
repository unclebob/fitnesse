package fitnesse.junit;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.slim.results.SlimTestResult;
import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.WikiPage;
import org.junit.Test;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class JUnitRunNotifierResultsListenerTest {

  @Test
  public void shouldProvideFirstFailure() {
    RunNotifier notifier = mock(RunNotifier.class);
    JUnitRunNotifierResultsListener listener = new JUnitRunNotifierResultsListener(notifier, getClass());

    TestResult testResult = SlimTestResult.fail("Actual", "Expected");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), new TestSummary(0, 1, 0, 0));

    ArgumentCaptor<Failure> argument = ArgumentCaptor.forClass(Failure.class);
    verify(notifier).fireTestFailure(argument.capture());
    Failure failure = argument.getValue();

    assertThat(failure.getException(), instanceOf(AssertionError.class));
    assertThat(failure.getMessage(), is("[Actual] expected [Expected]"));
  }

  @Test
  public void shouldProvideFirstException() {
    RunNotifier notifier = mock(RunNotifier.class);
    JUnitRunNotifierResultsListener listener = new JUnitRunNotifierResultsListener(notifier, getClass());

    TestResult testResult = SlimTestResult.error("Message", "Actual");

    listener.testAssertionVerified(null, testResult);
    listener.testComplete(mockWikiTestPage(), new TestSummary(0, 0, 0, 1));

    ArgumentCaptor<Failure> argument = ArgumentCaptor.forClass(Failure.class);
    verify(notifier).fireTestFailure(argument.capture());
    Failure failure = argument.getValue();

    assertThat(failure.getException(), instanceOf(Exception.class));
    assertThat(failure.getMessage(), is("[Actual] Message"));
  }

  private WikiTestPage mockWikiTestPage() {
    WikiPage mock = mock(WikiPage.class);
    when(mock.isRoot()).thenReturn(true);
    when(mock.getName()).thenReturn("WikiPage");
    when(mock.getPageCrawler()).thenReturn(new PageCrawlerImpl(mock));
    return new WikiTestPage(mock);
  }
}