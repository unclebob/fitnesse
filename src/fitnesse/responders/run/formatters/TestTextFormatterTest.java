package fitnesse.responders.run.formatters;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import java.text.SimpleDateFormat;

import fitnesse.responders.run.TestPage;
import org.junit.Test;

import util.TimeMeasurement;

import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;

public class TestTextFormatterTest {

  @Test
  public void testCompleteShouldAddPageAndSummaryAndTimingToResponse() throws Exception {
    TestPage page = new TestPage(new WikiPageDummy("page", "content"));
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
    String startTime = "11:12:13";
    when(timeMeasurement.startedAtDate()).thenReturn(new SimpleDateFormat("HH:mm:ss").parse(startTime));
    when(timeMeasurement.elapsedSeconds()).thenReturn(9.8d);
    
    ChunkedResponse response = mock(ChunkedResponse.class);
    TestTextFormatter formatter = new TestTextFormatter(response);
    formatter.testComplete(page, summary, timeMeasurement);
    verify(response).add("F " + startTime + " R:1    W:2    I:3    E:4    page\t()\t9.800 seconds\n");
  }
  
  @Test
  public void allTestingCompleteShouldAddTotalsToResponse() throws Exception {
    TimeMeasurement mockTimeMeasurement = mock(TimeMeasurement.class);
    when(mockTimeMeasurement.elapsedSeconds()).thenReturn(7.6d);
    
    ChunkedResponse response = mock(ChunkedResponse.class);
    TestTextFormatter formatter = new TestTextFormatter(response);

    formatter.allTestingComplete(mockTimeMeasurement);
    verify(response).add("--------\n0 Tests,\t0 Failures\t7.600 seconds.\n");
  }
}
