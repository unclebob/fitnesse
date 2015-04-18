package fitnesse.reporting;

import static fitnesse.reporting.DecimalSeparatorUtil.getDecimalSeparator;
import static org.mockito.Mockito.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import fitnesse.testrunner.WikiTestPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import fitnesse.util.Clock;
import fitnesse.util.DateAlteringClock;

import fitnesse.http.ChunkedResponse;
import fitnesse.testsystems.TestSummary;
import fitnesse.wiki.WikiPageDummy;

public class TestTextFormatterTest {

  private static final String START_TIME = "11:12:13";

  private DateAlteringClock clock;

  @Before
  public void fixTime() throws ParseException {
    clock = new DateAlteringClock(new SimpleDateFormat("HH:mm:ss").parse(START_TIME)).freeze();
  }

  @After
  public void restoreDefaultClock() {
    Clock.restoreDefaultClock();
  }


  @Test
  public void testCompleteShouldAddPageAndSummaryAndTimingToResponse() throws Exception {
    WikiTestPage page = new WikiTestPage(new WikiPageDummy("page", "content", null));
    TestSummary summary = new TestSummary(1, 2, 3, 4);

    ChunkedResponse response = mock(ChunkedResponse.class);
    TestTextFormatter formatter = new TestTextFormatter(response);
    formatter.testStarted(page);
    clock.elapse(9800);
    formatter.testComplete(page, summary);
    formatter.close();
    verify(response).add("F " + START_TIME + " R:1    W:2    I:3    E:4    page\t()\t9" + getDecimalSeparator() + "800 seconds\n");
  }
  
  @Test
  public void allTestingCompleteShouldAddTotalsToResponse() throws Exception {

    ChunkedResponse response = mock(ChunkedResponse.class);
    TestTextFormatter formatter = new TestTextFormatter(response);
    clock.elapse(7600);

    formatter.close();
    verify(response).add("--------\n0 Tests,\t0 Failures\t7" + getDecimalSeparator() + "600 seconds.\n");
  }
}
